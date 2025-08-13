package io.wax100.customizeFishing.commands;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugFishingRod;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomizeFishingCommand implements CommandExecutor, TabCompleter {
    
    private final CustomizeFishing plugin;
    
    public CustomizeFishingCommand(CustomizeFishing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("customizefishing.reload")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.no_permission", "&cYou don't have permission!")));
                    return true;
                }
                
                try {
                    plugin.reload();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.reload_success", "&aConfiguration reloaded!")));
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.reload_error", "&cError reloading: %error%")
                            .replace("%error%", e.getMessage())));
                }
                break;
                
            case "help":
                sendHelp(sender);
                break;
                
            case "debugrod":
                if (!sender.hasPermission("customizefishing.debugrod")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.no_permission", "&cYou don't have permission!")));
                    return true;
                }
                
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /customizefishing debugrod <category>");
                    sender.sendMessage(ChatColor.YELLOW + "Available categories: " + getAvailableCategories());
                    return true;
                }
                
                String category = args[1].toLowerCase();
                if (!isValidCategory(category)) {
                    sender.sendMessage(ChatColor.RED + "Invalid category: " + category);
                    sender.sendMessage(ChatColor.YELLOW + "Available categories: " + getAvailableCategories());
                    return true;
                }

                ItemStack debugRod = DebugFishingRod.createDebugRod(plugin, category);
                player.getInventory().addItem(debugRod);
                sender.sendMessage(ChatColor.GREEN + "Debug fishing rod for category '" + category + "' has been added to your inventory!");
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /customizefishing help");
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "========= CustomizeFishing Help =========");
        sender.sendMessage(ChatColor.YELLOW + "/customizefishing reload" + ChatColor.WHITE + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/customizefishing debugrod <category>" + ChatColor.WHITE + " - Get debug fishing rod");
        sender.sendMessage(ChatColor.YELLOW + "/customizefishing help" + ChatColor.WHITE + " - Show this help message");
        sender.sendMessage(ChatColor.GRAY + "Available categories: " + getAvailableCategories());
    }
    
    private boolean isValidCategory(String category) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) return false;
        
        return categoriesSection.getKeys(false).contains(category);
    }
    
    private String getAvailableCategories() {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) return "none";
        
        return String.join(", ", categoriesSection.getKeys(false));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> commands = Arrays.asList("reload", "debugrod", "help");
            
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debugrod")) {
            List<String> completions = new ArrayList<>();
            ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
            
            if (categoriesSection != null) {
                for (String category : categoriesSection.getKeys(false)) {
                    if (category.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(category);
                    }
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}