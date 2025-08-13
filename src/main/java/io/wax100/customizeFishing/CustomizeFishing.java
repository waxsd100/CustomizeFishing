package io.wax100.customizeFishing;

import org.bukkit.plugin.java.JavaPlugin;
import io.wax100.customizeFishing.listeners.FishingListener;
import io.wax100.customizeFishing.commands.CustomizeFishingCommand;

public final class CustomizeFishing extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        
        CustomizeFishingCommand commandExecutor = new CustomizeFishingCommand(this);
        getCommand("customizefishing").setExecutor(commandExecutor);
        getCommand("customizefishing").setTabCompleter(commandExecutor);
        
        getLogger().info("CustomizeFishing has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomizeFishing has been disabled!");
    }


    public void reload() {
        reloadConfig();
    }
}
