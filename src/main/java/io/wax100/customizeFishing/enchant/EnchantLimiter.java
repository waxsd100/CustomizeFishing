package io.wax100.customizeFishing.enchant;

import io.wax100.customizeFishing.CustomizeFishing;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * エンチャントレベルの上限をプラグイン側で強制するクラス。
 * ルートテーブルの設定ミスで挙動が壊れるレベル（入れ食いLv6以上で浮きが沈まない等）を防ぐ。
 */
public class EnchantLimiter {

    // バニラの釣り待ち時間（tick）と入れ食い1Lvあたりの短縮量
    private static final int VANILLA_MAX_WAIT = 600;
    private static final int TICKS_PER_LURE_LEVEL = 100;

    // config に lure_exception_levels が無い場合のデフォルト例外
    // （旧configのまま新JARを配置しても伝説釣り竿?のLv127例外が機能するように）
    private static final List<Integer> DEFAULT_LURE_EXCEPTION_LEVELS = List.of(127);

    private final CustomizeFishing plugin;

    // item_caps のキャッシュ（config オブジェクトが変わったとき＝リロード時に再読込）
    private Map<Enchantment, Integer> cachedItemCaps;
    private Configuration cachedItemCapsSource;

    public EnchantLimiter(CustomizeFishing plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("enchant_limits.enabled", true);
    }

    /**
     * 釣果アイテムのエンチャントを config の enchant_limits.item_caps に従って切り詰める。
     * 通常エンチャントとエンチャント本の格納エンチャントの両方を対象とする。
     *
     * @param item 釣果アイテム
     * @return 切り詰め後のアイテム（引数と同一インスタンス）
     */
    public ItemStack clampItemEnchants(ItemStack item) {
        if (!isEnabled() || item == null) {
            return item;
        }

        for (Map.Entry<Enchantment, Integer> capEntry : getItemCaps().entrySet()) {
            Enchantment enchant = capEntry.getKey();
            int cap = capEntry.getValue();

            int level = item.getEnchantmentLevel(enchant);
            if (level > cap) {
                item.addUnsafeEnchantment(enchant, cap);
                plugin.getLogger().info("釣果アイテムの " + enchant.getKey() + " Lv" + level + " を上限 Lv" + cap + " に制限しました");
            }

            if (item.getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
                Integer stored = storageMeta.getStoredEnchants().get(enchant);
                if (stored != null && stored > cap) {
                    storageMeta.addStoredEnchant(enchant, cap, true);
                    item.setItemMeta(storageMeta);
                    plugin.getLogger().info("エンチャント本の " + enchant.getKey() + " Lv" + stored + " を上限 Lv" + cap + " に制限しました");
                }
            }
        }
        return item;
    }

    /**
     * 入れ食いLv6以上の竿の浮き待ち時間をプラグイン独自計算で補正する。
     * バニラではLv6以上は待ち時間が常に0以下になり浮きが一切沈まなくなるため、
     * Lv5相当（1〜100tick）を基準に、超過分は1レベルごとに最大待ち時間をさらに1tick短縮する。
     * 例: Lv6 → 1〜99tick、Lv50 → 1〜55tick、Lv105以上 → 1〜2tick（着水ほぼ即ヒット）
     *
     * @param hook 浮き
     * @param rod  使用中の釣り竿
     */
    public void applyLureBehaviorCap(FishHook hook, ItemStack rod) {
        if (!isEnabled() || rod == null) {
            return;
        }

        int vanillaSafeMax = plugin.getConfig().getInt("enchant_limits.lure_behavior_cap", 5);
        int lureLevel = rod.getEnchantmentLevel(Enchantment.LURE);
        if (lureLevel <= vanillaSafeMax) {
            return;
        }

        // 例外レベル（伝説釣り竿?などのジョーク竿）は補正せず、バニラの挙動のまま＝浮きが沈まない
        List<Integer> exceptionLevels = plugin.getConfig().isSet("enchant_limits.lure_exception_levels")
                ? plugin.getConfig().getIntegerList("enchant_limits.lure_exception_levels")
                : DEFAULT_LURE_EXCEPTION_LEVELS;
        if (exceptionLevels.contains(lureLevel)) {
            return;
        }

        int baseMaxWait = Math.max(2, VANILLA_MAX_WAIT - vanillaSafeMax * TICKS_PER_LURE_LEVEL);
        int extraLevels = lureLevel - vanillaSafeMax;
        int effectiveMaxWait = Math.max(2, baseMaxWait - extraLevels);

        // バニラは「待ち時間 − 入れ食いLv×100tick」で抽選するため、引かれる分を上乗せした
        // 待ち時間を設定する（差し引き後の実効待ち時間: 1〜effectiveMaxWait tick）。
        // applyLure等の抽選経路には一切触れないので、BITEイベント発火・タイミング判定・
        // 確率表示などはLv5以下の竿と完全に同じ挙動になる。
        int reduction = lureLevel * TICKS_PER_LURE_LEVEL;
        hook.setMaxWaitTime(reduction + effectiveMaxWait);
        hook.setMinWaitTime(reduction + 1);
    }

    /**
     * item_caps を取得する（configリロードまでキャッシュ）
     */
    private Map<Enchantment, Integer> getItemCaps() {
        Configuration config = plugin.getConfig();
        if (cachedItemCaps == null || cachedItemCapsSource != config) {
            cachedItemCaps = loadItemCaps(config);
            cachedItemCapsSource = config;
        }
        return cachedItemCaps;
    }

    /**
     * config の enchant_limits.item_caps を読み込む
     */
    private Map<Enchantment, Integer> loadItemCaps(Configuration config) {
        Map<Enchantment, Integer> caps = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("enchant_limits.item_caps");
        if (section == null) {
            return caps;
        }

        for (String key : section.getKeys(false)) {
            NamespacedKey enchantKey = NamespacedKey.fromString(key);
            Enchantment enchant = enchantKey != null ? Enchantment.getByKey(enchantKey) : null;
            if (enchant == null) {
                plugin.getLogger().warning("enchant_limits.item_caps に不明なエンチャントID: " + key);
                continue;
            }
            caps.put(enchant, section.getInt(key));
        }
        return caps;
    }
}
