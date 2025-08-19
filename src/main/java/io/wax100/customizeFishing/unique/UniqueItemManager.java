package io.wax100.customizeFishing.unique;

import io.wax100.customizeFishing.CustomizeFishing;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UniqueItemManager {

    private final CustomizeFishing plugin;
    private final File uniqueDataFile;
    private final NamespacedKey uniqueKey;
    private final NamespacedKey uniqueIdKey;
    private FileConfiguration uniqueData;

    public UniqueItemManager(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.uniqueDataFile = new File(plugin.getDataFolder(), "unique_items.yml");
        this.uniqueKey = new NamespacedKey(plugin, "unique");
        this.uniqueIdKey = new NamespacedKey(plugin, "unique_id");
        loadUniqueData();
    }

    /**
     * unique_items.ymlファイルをロード
     */
    private void loadUniqueData() {
        if (!uniqueDataFile.exists()) {
            plugin.saveResource("unique_items.yml", false);
        }
        uniqueData = YamlConfiguration.loadConfiguration(uniqueDataFile);
    }

    /**
     * unique_items.ymlファイルを保存
     */
    private void saveUniqueData() {
        try {
            uniqueData.save(uniqueDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save unique items data: " + e.getMessage());
        }
    }

    /**
     * アイテムがユニークアイテムかどうかをチェック
     *
     * @param item チェックするアイテム
     * @return ユニークアイテムの場合true
     */
    public boolean isUniqueItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();
        return container.has(uniqueKey, PersistentDataType.BYTE) &&
                container.get(uniqueKey, PersistentDataType.BYTE) == 1;
    }

    /**
     * ユニークアイテムのIDを取得
     *
     * @param item ユニークアイテム
     * @return ユニークID、存在しない場合はnull
     */
    public String getUniqueId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();
        return container.get(uniqueIdKey, PersistentDataType.STRING);
    }

    /**
     * 指定されたワールドでユニークアイテムが既に釣られているかチェック
     *
     * @param world    ワールド
     * @param uniqueId ユニークアイテムのID
     * @return 既に釣られている場合true
     */
    public boolean isItemAlreadyCaught(World world, String uniqueId) {
        String worldName = world.getName();
        List<String> caughtItems = uniqueData.getStringList("worlds." + worldName + ".caught_items");
        return caughtItems.contains(uniqueId);
    }

    /**
     * ユニークアイテムを釣った記録を追加
     *
     * @param world    ワールド
     * @param uniqueId ユニークアイテムのID
     * @param player   釣ったプレイヤー
     */
    public void markItemAsCaught(World world, String uniqueId, Player player) {
        String worldName = world.getName();
        String path = "worlds." + worldName;

        // 釣られたアイテムリストに追加
        List<String> caughtItems = uniqueData.getStringList(path + ".caught_items");
        if (!caughtItems.contains(uniqueId)) {
            caughtItems.add(uniqueId);
            uniqueData.set(path + ".caught_items", caughtItems);
        }

        // 釣った人の記録を保存
        uniqueData.set(path + ".items." + uniqueId + ".caught_by", player.getUniqueId().toString());
        uniqueData.set(path + ".items." + uniqueId + ".caught_by_name", player.getName());
        uniqueData.set(path + ".items." + uniqueId + ".caught_at", System.currentTimeMillis());

        saveUniqueData();

        plugin.getLogger().info(String.format("Unique item '%s' was caught by %s in world '%s'",
                uniqueId, player.getName(), worldName));
    }


    /**
     * uniqueデータをリロード
     */
    public void reload() {
        loadUniqueData();
    }


    /**
     * ユニークアイテムにLoreを追加
     *
     * @param item   対象のアイテム
     * @param world  ワールド
     * @param player 釣ったプレイヤー
     * @return Lore追加済みのアイテム
     */
    public ItemStack addUniqueLore(ItemStack item, World world, Player player) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        String uniqueId = getUniqueId(item);
        if (uniqueId == null) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = Objects.requireNonNull(meta).getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add(0, "");
        // 既に釣られているかチェック
        if (isItemAlreadyCaught(world, uniqueId)) {
            String worldName = world.getName();
            String path = "worlds." + worldName + ".items." + uniqueId + ".caught_by_name";
            String firstCatcher = uniqueData.getString(path);
            lore.add(0, "§7先駆者: §f" + firstCatcher);
        } else {
            lore.add(0, "§7先駆者: §f" + player.getName());
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}