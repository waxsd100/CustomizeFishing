package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class BindingCurseListener implements Listener {

    private final BindingCurseManager bindingCurseManager;

    public BindingCurseListener(CustomizeFishing plugin) {
        this.bindingCurseManager = new BindingCurseManager(plugin);
    }

    private boolean checkAndCancelIfNotOwner(ItemStack item, Player player) {
        if (item == null || !bindingCurseManager.hasBindingCurse(item) || !bindingCurseManager.hasOwner(item)) {
            return false;
        }

        if (!bindingCurseManager.isOwner(item, player)) {
            String ownerName = bindingCurseManager.getOwnerName(item);
            player.sendMessage(ChatColor.RED + "このアイテムは " + ChatColor.YELLOW + ownerName + ChatColor.RED + " にのみ束縛されています！");
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();

        if (checkAndCancelIfNotOwner(item, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (checkAndCancelIfNotOwner(currentItem, player)) {
            event.setCancelled(true);
            return;
        }

        if (cursorItem != null && cursorItem.getType() != Material.AIR && checkAndCancelIfNotOwner(cursorItem, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack oldCursor = event.getOldCursor();

        if (checkAndCancelIfNotOwner(oldCursor, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (checkAndCancelIfNotOwner(item, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();

        if (checkAndCancelIfNotOwner(mainHandItem, player) || checkAndCancelIfNotOwner(offHandItem, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        ItemStack playerItem = event.getPlayerItem();
        ItemStack armorStandItem = event.getArmorStandItem();

        if (checkAndCancelIfNotOwner(playerItem, player) || checkAndCancelIfNotOwner(armorStandItem, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (checkAndCancelIfNotOwner(item, player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (checkAndCancelIfNotOwner(newItem, player)) {
            event.setCancelled(true);
        }
    }
}