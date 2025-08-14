package io.wax100.customizeFishing.debug;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebugFishingRod {

    private static final String DEBUG_ROD_KEY = "debug_fishing_rod";
    private static final String FORCE_CATEGORY_KEY = "force_category";

    public static ItemStack createDebugRod(Plugin plugin, String category) {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lデバッグ釣り竿 §7[" + category.toUpperCase() + "]");

            List<String> lore = new ArrayList<>();
            lore.add("§7このアイテムは管理者専用です");
            lore.add("§e強制カテゴリ: §f" + category);
            lore.add("§c注意: 通常の釣りルールを無視します");
            meta.setLore(lore);

            // エンチャント追加（見た目用）
            meta.addEnchant(Enchantment.LUCK, 10, true);
            meta.addEnchant(Enchantment.LURE, 5, true);
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);

            // NBTデータでマーキング
            NamespacedKey debugKey = new NamespacedKey(plugin, DEBUG_ROD_KEY);
            NamespacedKey categoryKey = new NamespacedKey(plugin, FORCE_CATEGORY_KEY);
            meta.getPersistentDataContainer().set(debugKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category);

            rod.setItemMeta(meta);
        }

        return rod;
    }

    public static boolean isDebugRod(Plugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        NamespacedKey debugKey = new NamespacedKey(plugin, DEBUG_ROD_KEY);
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().has(debugKey, PersistentDataType.BOOLEAN);
    }

    public static String getForcedCategory(Plugin plugin, ItemStack item) {
        if (!isDebugRod(plugin, item)) {
            return null;
        }

        NamespacedKey categoryKey = new NamespacedKey(plugin, FORCE_CATEGORY_KEY);
        return Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer().get(categoryKey, PersistentDataType.STRING);
    }
}