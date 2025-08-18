package io.wax100.customizeFishing.binding;

import io.wax100.customizeFishing.CustomizeFishing;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BindingCurseManager {

    private final NamespacedKey ownerKey;
    private final NamespacedKey ownerNameKey;

    public BindingCurseManager(CustomizeFishing plugin) {
        this.ownerKey = new NamespacedKey(plugin, "binding_owner");
        this.ownerNameKey = new NamespacedKey(plugin, "binding_owner_name");
    }

    public void setItemOwner(ItemStack item, Player owner) {
        if (item == null || !hasBindingCurse(item)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
        container.set(ownerNameKey, PersistentDataType.STRING, owner.getName());

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // 束縛の呪いの説明を複数行で追加
        lore.add(0, "");
        lore.add(0, "§c§l束縛: §f" + owner.getName() + " §7のみ所持・使用可能");
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    public boolean isOwner(ItemStack item, Player player) {
        UUID ownerUUID = getOwnerUUID(item);
        if (ownerUUID == null) {
            return true; // 所有者が設定されていない、または束縛の呪いが無い場合は誰でも使用可能
        }

        return player.getUniqueId().equals(ownerUUID);
    }

    public UUID getOwnerUUID(ItemStack item) {
        if (item == null || !hasBindingCurse(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ownerKey, PersistentDataType.STRING)) {
            return null;
        }

        String ownerUuid = container.get(ownerKey, PersistentDataType.STRING);
        if (ownerUuid == null) {
            return null;
        }

        try {
            return UUID.fromString(ownerUuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getOwnerName(ItemStack item) {
        if (item == null || !hasBindingCurse(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(ownerNameKey, PersistentDataType.STRING);
    }

    public boolean hasBindingCurse(ItemStack item) {
        if (item == null) {
            return false;
        }

        return item.containsEnchantment(Enchantment.BINDING_CURSE);
    }

    public boolean hasOwner(ItemStack item) {
        if (item == null || !hasBindingCurse(item)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(ownerKey, PersistentDataType.STRING);
    }
}