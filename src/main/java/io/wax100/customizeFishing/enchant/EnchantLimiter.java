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
import java.util.Map;

/**
 * エンチャントレベルの上限をプラグイン側で強制するクラス。
 * ルートテーブルの設定ミスで挙動が壊れるレベル（入れ食いLv6以上で浮きが沈まない等）を防ぐ。
 */
public class EnchantLimiter {

    // バニラの釣り待ち時間（tick）と入れ食い1Lvあたりの短縮量
    private static final int VANILLA_MAX_WAIT = 600;
    private static final int TICKS_PER_LURE_LEVEL = 100;


    // バニラの魚接近時間（Phase 2: timeUntilHooked）
    private static final int VANILLA_MIN_LURE_TIME = 20;
    private static final int VANILLA_MAX_LURE_TIME = 80;
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
     * <p>
     * さらに、魚の接近時間（Phase 2: lureTime、バニラでは20〜80tick）も
     * 超過レベルに比例して短縮し、Lv105以上では着水ほぼ即ヒットになる。
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

        // lure:127（GODの釣り竿）は全待ち時間を0にして着水即ヒット
        if (lureLevel == 127) {
            hook.setMinWaitTime(0);
            hook.setMaxWaitTime(0);
            hook.setMinLureTime(0);
            hook.setMaxLureTime(0);
            return;
        }

        int extraLevels = lureLevel - vanillaSafeMax;

        // === Phase 1: 待機時間（timeUntilLured）の補正 ===
        int baseMaxWait = Math.max(2, VANILLA_MAX_WAIT - vanillaSafeMax * TICKS_PER_LURE_LEVEL);
        int effectiveMaxWait = Math.max(2, baseMaxWait - extraLevels);

        // バニラは「待ち時間 − 入れ食いLv×100tick」で抽選するため、引かれる分を上乗せした
        // 待ち時間を設定する（差し引き後の実効待ち時間: 1〜effectiveMaxWait tick）。
        // applyLure等の抽選経路には一切触れないので、BITEイベント発火・タイミング判定・
        // 確率表示などはLv5以下の竿と完全に同じ挙動になる。
        int reduction = lureLevel * TICKS_PER_LURE_LEVEL;
        hook.setMaxWaitTime(reduction + effectiveMaxWait);
        hook.setMinWaitTime(reduction + 1);

        // === Phase 2: 魚の接近時間（lureTime / timeUntilHooked）の短縮 ===
        // バニラでは20〜80tickだが、超過レベルに応じて短縮する。
        // extraLevels が増えるにつれ接近時間を減らし、Lv105以上(extraLevels>=100)でほぼ即座。
        double lureTimeRatio = Math.max(0.0, 1.0 - extraLevels / 100.0);
        int effectiveMinLure = Math.max(1, (int) (VANILLA_MIN_LURE_TIME * lureTimeRatio));
        int effectiveMaxLure = Math.max(1, (int) (VANILLA_MAX_LURE_TIME * lureTimeRatio));
        hook.setMinLureTime(effectiveMinLure);
        hook.setMaxLureTime(effectiveMaxLure);
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
