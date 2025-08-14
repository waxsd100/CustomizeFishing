package io.wax100.customizeFishing.fishing;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerHeadProcessor {

    public static ItemStack processPlayerHead(ItemStack item, Player player, String category) {
        // イルカの好意カテゴリでプレイヤーヘッドの場合のみ処理
        if (!category.equals("dolphins_grace") || item.getType() != Material.PLAYER_HEAD) {
            return item;
        }

        ItemStack playerHead = item.clone();
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        if (skullMeta != null) {
            // プレイヤーの頭に設定
            skullMeta.setOwningPlayer(player);

            // 表示名を設定
            String displayName = "§b§l" + player.getName() + "の頭";
            skullMeta.setDisplayName(displayName);

            // 説明文を追加
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7イルカが好意であなたの頭を持ってきてくれました。");
            skullMeta.setLore(lore);

            playerHead.setItemMeta(skullMeta);
        }

        return playerHead;
    }
}