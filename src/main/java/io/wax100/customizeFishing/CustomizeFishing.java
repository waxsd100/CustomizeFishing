package io.wax100.customizeFishing;

import io.wax100.customizeFishing.commands.CustomizeFishingCommand;
import io.wax100.customizeFishing.listeners.BindingCurseListener;
import io.wax100.customizeFishing.listeners.FishingListener;
import io.wax100.customizeFishing.listeners.LuckDisplayListener;
import io.wax100.customizeFishing.unique.UniqueItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomizeFishing extends JavaPlugin {

    private UniqueItemManager uniqueItemManager;
    private LuckDisplayListener luckDisplayListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        this.uniqueItemManager = new UniqueItemManager(this);
        this.luckDisplayListener = new LuckDisplayListener(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new BindingCurseListener(this), this);
        getServer().getPluginManager().registerEvents(luckDisplayListener, this);

        // Register commands
        CustomizeFishingCommand commandExecutor = new CustomizeFishingCommand(this);
        Objects.requireNonNull(getCommand("customizefishing")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("customizefishing")).setTabCompleter(commandExecutor);

        validateCategoryLootTables();

        getLogger().info("CustomizeFishing has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomizeFishing has been disabled!");
    }

    public void reload() {
        reloadConfig();
        validateCategoryLootTables();
    }

    /**
     * 有効なカテゴリすべてに対応するルートテーブルが存在するか検証する。
     * config.yml とデータパックの不整合（旧ティア名の残存など）を起動・リロード時に検出する。
     */
    private void validateCategoryLootTables() {
        ConfigurationSection categories = getConfig().getConfigurationSection("categories");
        if (categories == null) {
            getLogger().warning("config.yml に categories セクションがありません。");
            return;
        }

        String namespace = getConfig().getString("loot_tables.namespace", "customize_fishing");
        String path = getConfig().getString("loot_tables.path", "gameplay/fishing");

        for (String name : categories.getKeys(false)) {
            if (!categories.getBoolean(name + ".enabled", true)) {
                continue;
            }
            NamespacedKey key = NamespacedKey.fromString(namespace + ":" + path + "/" + name);
            if (key == null || getServer().getLootTable(key) == null) {
                getLogger().warning("カテゴリ '" + name + "' のルートテーブルが見つかりません: "
                        + namespace + ":" + path + "/" + name
                        + " （config.yml とデータパックのティア名が一致していません）");
            }
        }
    }

    public UniqueItemManager getUniqueItemManager() {
        return uniqueItemManager;
    }
}
