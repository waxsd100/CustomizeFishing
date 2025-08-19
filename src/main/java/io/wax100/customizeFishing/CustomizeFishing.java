package io.wax100.customizeFishing;

import io.wax100.customizeFishing.commands.CustomizeFishingCommand;
import io.wax100.customizeFishing.listeners.BindingCurseListener;
import io.wax100.customizeFishing.listeners.FishingListener;
import io.wax100.customizeFishing.unique.UniqueItemManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomizeFishing extends JavaPlugin {

    private UniqueItemManager uniqueItemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        this.uniqueItemManager = new UniqueItemManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new BindingCurseListener(this), this);

        // Register commands
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

    public UniqueItemManager getUniqueItemManager() {
        return uniqueItemManager;
    }
}
