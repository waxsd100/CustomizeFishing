package io.wax100.customizeFishing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wax100.customizeFishing.commands.CustomizeFishingCommand;
import io.wax100.customizeFishing.listeners.BindingCurseListener;
import io.wax100.customizeFishing.listeners.FishingListener;
import io.wax100.customizeFishing.unique.UniqueItemManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CustomizeFishing extends JavaPlugin {

    private UniqueItemManager uniqueItemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        uniqueItemManager = new UniqueItemManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new BindingCurseListener(this), this);

        // Register commands
        CustomizeFishingCommand commandExecutor = new CustomizeFishingCommand(this);
        Objects.requireNonNull(getCommand("customizefishing")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("customizefishing")).setTabCompleter(commandExecutor);

        getLogger().info("CustomizeFishing has been enabled!");

        // Log unique items at startup
        logUniqueItems();
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomizeFishing has been disabled!");
    }

    public void reload() {
        reloadConfig();
        if (uniqueItemManager != null) {
            uniqueItemManager.reload();
        }
    }

    public UniqueItemManager getUniqueItemManager() {
        return uniqueItemManager;
    }

    private void logUniqueItems() {
        getLogger().info("========================================");
        getLogger().info("Unique Items List");
        getLogger().info("========================================");

        File dataFolder = new File(getDataFolder(), "data");
        File lootTablesDir = new File(dataFolder, "customize_fishing/loot_tables");

        if (!lootTablesDir.exists()) {
            getLogger().warning("Loot tables directory not found: " + lootTablesDir.getPath());
            return;
        }

        List<UniqueItemInfo> uniqueItems = new ArrayList<>();
        scanDirectoryForUniqueItems(lootTablesDir, uniqueItems);

        if (uniqueItems.isEmpty()) {
            getLogger().info("No unique items found.");
        } else {
            getLogger().info("Found " + uniqueItems.size() + " unique item(s):");
            getLogger().info("");

            for (int i = 0; i < uniqueItems.size(); i++) {
                UniqueItemInfo item = uniqueItems.get(i);
                getLogger().info(String.format("%d. ID: %s | Item: %s | Name: %s",
                        i + 1, item.uniqueId(), item.itemType(), item.displayName()));
            }
        }

        getLogger().info("========================================");
    }

    private void scanDirectoryForUniqueItems(File directory, List<UniqueItemInfo> uniqueItems) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryForUniqueItems(file, uniqueItems);
            } else if (file.getName().endsWith(".json")) {
                scanJsonFileForUniqueItems(file, uniqueItems);
            }
        }
    }

    private void scanJsonFileForUniqueItems(File file, List<UniqueItemInfo> uniqueItems) {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root.has("pools")) {
                JsonArray pools = root.getAsJsonArray("pools");
                for (JsonElement poolElement : pools) {
                    JsonObject pool = poolElement.getAsJsonObject();
                    if (pool.has("entries")) {
                        JsonArray entries = pool.getAsJsonArray("entries");
                        for (JsonElement entryElement : entries) {
                            JsonObject entry = entryElement.getAsJsonObject();
                            extractUniqueItemInfo(entry, file, uniqueItems);
                        }
                    }
                }
            }
        } catch (IOException e) {
            getLogger().warning("ファイルの読み込みに失敗しました: " + file.getName());
        }
    }

    private void extractUniqueItemInfo(JsonObject entry, File file, List<UniqueItemInfo> uniqueItems) {
        if (!entry.has("functions")) return;

        String itemType = entry.has("name") ? entry.get("name").getAsString() : "unknown";
        String displayName = null;
        String uniqueId = null;

        JsonArray functions = entry.getAsJsonArray("functions");
        for (JsonElement funcElement : functions) {
            JsonObject function = funcElement.getAsJsonObject();
            String functionType = function.has("function") ? function.get("function").getAsString() : "";

            if ("minecraft:set_name".equals(functionType) && function.has("name")) {
                JsonElement nameElement = function.get("name");
                if (nameElement.isJsonObject()) {
                    JsonObject nameObj = nameElement.getAsJsonObject();
                    displayName = nameObj.has("text") ? nameObj.get("text").getAsString() : null;
                } else {
                    displayName = nameElement.getAsString();
                }
            }

            if ("minecraft:set_nbt".equals(functionType) && function.has("tag")) {
                String nbtTag = function.get("tag").getAsString();
                // NBTタグからunique_idを抽出
                if (nbtTag.contains("unique_id:")) {
                    int startIdx = nbtTag.indexOf("unique_id:\"") + 11;
                    int endIdx = nbtTag.indexOf("\"", startIdx);
                    if (startIdx > 10 && endIdx > startIdx) {
                        uniqueId = nbtTag.substring(startIdx, endIdx);
                    }
                }
            }
        }

        if (uniqueId != null) {
            String relativePath = file.getPath().replace(getDataFolder().getPath() + File.separator, "");
            uniqueItems.add(new UniqueItemInfo(uniqueId, itemType, displayName, relativePath));
        }
    }

    private record UniqueItemInfo(String uniqueId, String itemType, String displayName, String filePath) {
        private UniqueItemInfo(String uniqueId, String itemType, String displayName, String filePath) {
            this.uniqueId = uniqueId;
            this.itemType = itemType;
            this.displayName = displayName != null ? displayName : "UNKNOWN";
            this.filePath = filePath;
        }
    }
}
