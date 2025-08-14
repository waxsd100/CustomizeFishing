package io.wax100.customizeFishing;

import io.wax100.customizeFishing.commands.CustomizeFishingCommand;
import io.wax100.customizeFishing.listeners.FishingListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomizeFishing extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);

        CustomizeFishingCommand commandExecutor = new CustomizeFishingCommand(this);
        Objects.requireNonNull(getCommand("customizefishing")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("customizefishing")).setTabCompleter(commandExecutor);

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
