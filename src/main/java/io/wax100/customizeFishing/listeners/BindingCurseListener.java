package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BindingCurseListener implements Listener {

    private static final long MESSAGE_COOLDOWN = 3000; // 3秒のクールダウン
    private final CustomizeFishing plugin;
    private final BindingCurseManager bindingCurseManager;
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    public BindingCurseListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.bindingCurseManager = new BindingCurseManager(plugin);
    }

    private boolean checkAndCancelIfNotOwner(ItemStack item, Player player) {
        if (item == null || !bindingCurseManager.hasBindingCurse(item) || !bindingCurseManager.hasOwner(item)) {
            return false;
        }

        if (!bindingCurseManager.isOwner(item, player)) {
            sendWarningMessage(player, item);
            return true;
        }

        return false;
    }

    private void sendWarningMessage(Player player, ItemStack item) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(playerId);

        if (lastTime == null || currentTime - lastTime > MESSAGE_COOLDOWN) {
            String ownerName = bindingCurseManager.getOwnerName(item);
            player.sendMessage(ChatColor.RED + "このアイテムは " + ChatColor.YELLOW + ownerName + ChatColor.RED + " にのみ束縛されています！");
            lastMessageTime.put(playerId, currentTime);
        }
    }

    private void dropBoundItemsNotOwnedBy(Player player) {
        PlayerInventory inventory = player.getInventory();
        Location playerLocation = player.getLocation();

        // インベントリ内の全スロットをチェック
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && bindingCurseManager.hasBindingCurse(item) &&
                    bindingCurseManager.hasOwner(item) && !bindingCurseManager.isOwner(item, player)) {

                // アイテムをドロップ
                Objects.requireNonNull(playerLocation.getWorld()).dropItem(playerLocation, item);
                inventory.setItem(i, null);

                String ownerName = bindingCurseManager.getOwnerName(item);
                player.sendMessage(ChatColor.YELLOW + ownerName + ChatColor.RED + " の束縛アイテムが自動的にドロップされました。");
            }
        }

        // 防具スロットもチェック
        ItemStack[] armorContents = inventory.getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack item = armorContents[i];
            if (item != null && bindingCurseManager.hasBindingCurse(item) &&
                    bindingCurseManager.hasOwner(item) && !bindingCurseManager.isOwner(item, player)) {

                // アイテムをドロップ
                Objects.requireNonNull(playerLocation.getWorld()).dropItem(playerLocation, item);
                armorContents[i] = null;

                String ownerName = bindingCurseManager.getOwnerName(item);
                player.sendMessage(ChatColor.YELLOW + ownerName + ChatColor.RED + " の束縛アイテムが自動的にドロップされました。");
            }
        }
        inventory.setArmorContents(armorContents);

        // オフハンドもチェック
        ItemStack offHand = inventory.getItemInOffHand();
        if (bindingCurseManager.hasBindingCurse(offHand) && bindingCurseManager.hasOwner(offHand) && !bindingCurseManager.isOwner(offHand, player)) {

            Objects.requireNonNull(playerLocation.getWorld()).dropItem(playerLocation, offHand);
            inventory.setItemInOffHand(null);

            String ownerName = bindingCurseManager.getOwnerName(offHand);
            player.sendMessage(ChatColor.YELLOW + ownerName + ChatColor.RED + " の束縛アイテムが自動的にドロップされました。");
        }
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // インベントリを閉じた後に遅延実行
        new BukkitRunnable() {
            @Override
            public void run() {
                dropBoundItemsNotOwnedBy(player);
            }
        }.runTaskLater(plugin, 1L);
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