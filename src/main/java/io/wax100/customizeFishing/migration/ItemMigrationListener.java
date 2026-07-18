package io.wax100.customizeFishing.migration;

import io.wax100.customizeFishing.CustomizeFishing;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * 既存プレイヤーが所持する「伝説釣り竿?」の入れ食いレベルを
 * 旧値(127)から新値(-128)へ自動マイグレーションするリスナー。
 * <p>
 * 以下の2つのタイミングでチェックする:
 * <ul>
 *   <li>プレイヤーのログイン時 — インベントリ全スロットをスキャン</li>
 *   <li>釣り竿の使用時 — 手に持っている竿を即時チェック</li>
 * </ul>
 */
public class ItemMigrationListener implements Listener {


    private static final int OLD_LURE_LEVEL = 127;
    private static final int NEW_LURE_LEVEL = -128;

    private final CustomizeFishing plugin;

    public ItemMigrationListener(CustomizeFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        int migratedCount = 0;

        // メインインベントリ（0〜35）＋アーマー＋オフハンドを含む全スロットをスキャン
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (migrateIfLegendaryRod(item)) {
                migratedCount++;
            }
        }

        // オフハンドも明示的にチェック（getSize()に含まれない場合の安全策）
        ItemStack offHand = inventory.getItemInOffHand();
        if (migrateIfLegendaryRod(offHand)) {
            migratedCount++;
        }

        if (migratedCount > 0) {
            plugin.getLogger().info("[Migration] " + player.getName()
                    + " の伝説釣り竿? " + migratedCount + "本の入れ食いを "
                    + OLD_LURE_LEVEL + " → " + NEW_LURE_LEVEL + " に変換しました");
        }
    }

    /**
     * 釣り竿の使用時にもマイグレーションをチェックする。
     * エンダーチェストやシュルカーボックスから取り出した竿、
     * 他プレイヤーから受け取った竿などログイン時にカバーできないケースに対応。
     * <p>
     * FishingListener より先に実行されるよう LOWEST 優先度で処理する。
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack rod = getFishingRod(player);
        if (migrateIfLegendaryRod(rod)) {
            plugin.getLogger().info("[Migration] " + player.getName()
                    + " が使用した伝説釣り竿? の入れ食いを "
                    + OLD_LURE_LEVEL + " → " + NEW_LURE_LEVEL + " に変換しました");
        }
    }

    /**
     * プレイヤーが使用中の釣り竿を取得する（メインハンド優先）。
     */
    private ItemStack getFishingRod(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.FISHING_ROD) {
            return mainHand;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.FISHING_ROD) {
            return offHand;
        }
        return null;
    }

    /**
     * 旧仕様の伝説釣り竿?（lure:127）であれば lure:-128 に変換する。
     * <p>
     * 判定基準: 入れ食いLv127 かつ 修繕エンチャントなし。
     * GODの釣り竿（lure:127）は修繕Lv5を持つため誤変換されない。
     * アイテム名ではなくエンチャント構成で判定するので、金床リネーム済みの竿もすり抜けない。
     *
     * @param item 検査対象アイテム
     * @return マイグレーションを実行した場合 true
     */
    private boolean migrateIfLegendaryRod(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) {
            return false;
        }

        // 入れ食いが旧レベル(127)かチェック
        int currentLure = item.getEnchantmentLevel(Enchantment.LURE);
        if (currentLure != OLD_LURE_LEVEL) {
            return false;
        }

        // GODの釣り竿は修繕(mending)を持つので除外
        if (item.getEnchantmentLevel(Enchantment.MENDING) > 0) {
            return false;
        }

        // 入れ食いを新レベル(-128)に変更
        item.addUnsafeEnchantment(Enchantment.LURE, NEW_LURE_LEVEL);
        return true;
    }
}
