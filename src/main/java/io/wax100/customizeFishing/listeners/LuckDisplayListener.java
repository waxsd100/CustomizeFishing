package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.timing.TimingResult;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LuckDisplayListener implements Listener {

    private final CustomizeFishing plugin;
    private final LuckCalculator luckCalculator;

    public LuckDisplayListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        DebugLogger debugLogger = new DebugLogger(plugin);
        this.luckCalculator = new LuckCalculator(plugin, debugLogger);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!plugin.getConfig().getBoolean("luck_display.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (newItem != null && newItem.getType() == Material.FISHING_ROD) {
            // 1tick後に実行して、インベントリの更新を待つ
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                displayLuckValue(player, newItem);
            }, 1L);
        }
    }

    private void displayLuckValue(Player player) {
        try {
            Weather weather = Weather.fromBukkitWeather(player.getWorld().hasStorm(), player.getWorld().isThundering());
            TimingResult timingResult = TimingResult.miss();
            
            LuckResult luckResult = luckCalculator.calculateTotalLuck(player, weather, timingResult);
            double totalLuck = luckResult.getTotalLuck(plugin);

            String message = getMessage(totalLuck, luckResult);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to display luck value for player " + player.getName() + ": " + e.getMessage());
        }
    }

    private void displayLuckValue(Player player, ItemStack fishingRod) {
        try {
            Weather weather = Weather.fromBukkitWeather(player.getWorld().hasStorm(), player.getWorld().isThundering());
            TimingResult timingResult = TimingResult.miss();
            
            // 特定の釣り竿を使用して幸運値を計算
            LuckResult luckResult = luckCalculator.calculateTotalLuckWithSpecificRod(player, weather, timingResult, fishingRod);
            double totalLuck = luckResult.getTotalLuck(plugin);

            String message = getMessage(totalLuck, luckResult);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to display luck value for player " + player.getName() + ": " + e.getMessage());
        }
    }

    private static String getMessage(double totalLuck, LuckResult luckResult) {
        String message = ChatColor.GOLD + "幸運値: " + ChatColor.YELLOW + String.format("%.1f", totalLuck);

        message += ChatColor.GRAY + " (" +
                ChatColor.AQUA + "宝釣り:" + luckResult.luckOfTheSeaLevel() +
                ChatColor.GREEN + " ポーション:" + (luckResult.luckPotionLevel() - luckResult.unluckPotionLevel()) +
                ChatColor.LIGHT_PURPLE + " 装備:" + String.format("%.1f", luckResult.equipmentLuck()) +
                ChatColor.BLUE + " 天気:" + String.format("%.1f", luckResult.weatherLuck()) +
                ChatColor.GRAY + ")";
        return message;
    }

}