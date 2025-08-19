package io.wax100.customizeFishing.fishing;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import io.wax100.customizeFishing.debug.DebugFishingRod;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import io.wax100.customizeFishing.unique.UniqueItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

/**
 * 釣り条件のキャッシュ
 */
record FishingConditionsCache(
        LuckResult luckResult,
        boolean isOpenWater,
        Weather weather,
        boolean hasDolphinsGrace,
        Location hookLocation
) {}

/**
 * ユニークアイテム処理の結果
 */
record UniqueProcessingResult(
        ItemStack item,
        String category
) {}

public class FishingProcessor {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    private final BindingCurseManager bindingCurseManager;
    private final CategorySelector categorySelector;
    private final ProbabilityCalculator probabilityCalculator;
    private final Random random;

    public FishingProcessor(CustomizeFishing plugin, DebugLogger debugLogger, BindingCurseManager bindingCurseManager, CategorySelector categorySelector, ProbabilityCalculator probabilityCalculator) {
        this.plugin = plugin;
        this.debugLogger = debugLogger;
        this.bindingCurseManager = bindingCurseManager;
        this.categorySelector = categorySelector;
        this.probabilityCalculator = probabilityCalculator;
        this.random = new Random();
    }

    /**
     * 釣り処理を実行し、結果を返す
     *
     * @param player               プレイヤー
     * @param itemEntity           アイテムエンティティ
     * @param hookLocation         釣り針の位置
     * @param isDoubleFishingBonus ダブルフィッシングのボーナスかどうか
     * @param timingResult         タイミング結果
     * @param luckResult           幸運計算結果
     * @param isOpenWater          開水域かどうか
     * @param weather              天気
     * @return 釣り結果
     */
    public FishingResult processFishing(Player player, Item itemEntity, Location hookLocation, boolean isDoubleFishingBonus, TimingResult timingResult, LuckResult luckResult, boolean isOpenWater, Weather weather) {

        boolean hasDolphinsGrace = player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);

        // 釣り条件をキャッシュ
        FishingConditionsCache conditionsCache = new FishingConditionsCache(
                luckResult,
                isOpenWater,
                weather,
                hasDolphinsGrace,
                hookLocation
        );

        if (isDoubleFishingBonus) {
            debugLogger.logInfo(player, "=== DOUBLE FISHING: BONUS FISHING PROCESS ===");
        }
        String forcedCategory = null;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (DebugFishingRod.isDebugRod(plugin, mainHand)) {
            forcedCategory = DebugFishingRod.getForcedCategory(plugin, mainHand);
            debugLogger.logFishingStart(player, isOpenWater, weather, hasDolphinsGrace, forcedCategory);
        } else {
            debugLogger.logFishingStart(player, isOpenWater, weather, hasDolphinsGrace, null);
        }

        String category = Objects.requireNonNullElseGet(forcedCategory, () -> categorySelector.determineCategoryFromConfig(player, luckResult, isOpenWater, weather, hasDolphinsGrace));
        int eligibleCount = categorySelector.getEligibleCategoryCount(luckResult, isOpenWater, weather, hasDolphinsGrace);

        debugLogger.logTimingResult(player, timingResult.reactionTimeMs(), timingResult);
        debugLogger.logLuckBreakdown(player, luckResult);
        debugLogger.logCategorySelection(player, category, eligibleCount);

        String namespace = plugin.getConfig().getString("loot_tables.namespace", "customize_fishing");
        String path = plugin.getConfig().getString("loot_tables.path", "gameplay/fishing");
        NamespacedKey lootTableKey = NamespacedKey.fromString(namespace + ":" + path + "/" + category);
        LootTable lootTable = plugin.getServer().getLootTable(Objects.requireNonNull(lootTableKey));

        ItemStack originalItem = itemEntity.getItemStack().clone();
        ItemStack selectedItem = originalItem;

        if (lootTable != null) {
            try {
                float contextLuck = Math.max(-1024f, Math.min(1024f, (float) luckResult.getTotalLuck(plugin) * 10));

                LootContext.Builder contextBuilder = new LootContext.Builder(hookLocation)
                        .killer(player)
                        .lootedEntity(itemEntity)
                        .luck(contextLuck);

                LootContext lootContext = contextBuilder.build();
                Collection<ItemStack> loot = lootTable.populateLoot(random, lootContext);

                if (!loot.isEmpty()) {
                    selectedItem = loot.iterator().next();

                    if (selectedItem != null && selectedItem.getType() != Material.AIR && selectedItem.getAmount() > 0) {
                        selectedItem = PlayerHeadProcessor.processPlayerHead(selectedItem, player, category);

                        UniqueProcessingResult result = handleUniqueItemProcessing(selectedItem, player, category, conditionsCache);
                        selectedItem = result.item();
                        category = result.category();

                        bindingCurseManager.setItemOwner(selectedItem, player);
                        itemEntity.setItemStack(selectedItem);

                        debugLogger.logItemReplacement(
                                player,
                                getItemDisplayName(originalItem),
                                getItemDisplayName(selectedItem),
                                lootTableKey.toString()
                        );
                    } else {
                        debugLogger.logInfo(player, "[ERROR] Selected item is invalid, keeping original item");
                        selectedItem = originalItem;
                    }
                } else {
                    debugLogger.logInfo(player, "[ERROR] Loot table returned empty results for category: " + category);
                }
            } catch (IllegalArgumentException e) {
                debugLogger.logInfo(player, "[ERROR] Failed to populate loot table due to missing parameters: " + e.getMessage());
                debugLogger.logInfo(player, "Using original item instead");
            }
        } else {
            debugLogger.logInfo(player, "[ERROR] Loot table not found: " + lootTableKey);
        }

        String probabilityInfo = probabilityCalculator.calculateProbabilityInfo(category, luckResult, weather, timingResult);
        return new FishingResult(category, probabilityInfo, selectedItem);
    }

    /**
     * アイテムの表示名を取得する
     *
     * @param item アイテム
     * @return 表示名
     */
    private String getItemDisplayName(ItemStack item) {
        if (item == null) return "null";
        String displayName = item.getType().name().toLowerCase().replace("_", " ");
        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName()) {
            displayName = item.getItemMeta().getDisplayName();
        }
        return displayName + " x" + item.getAmount();
    }

    /**
     * ユニークアイテムの処理を行う
     *
     * @param selectedItem    選択されたアイテム
     * @param player          プレイヤー
     * @param originalCategory 元のカテゴリ
     * @param conditionsCache キャッシュされた釣り条件
     * @return 処理後のアイテムとカテゴリ
     */
    private UniqueProcessingResult handleUniqueItemProcessing(ItemStack selectedItem, Player player, String originalCategory, FishingConditionsCache conditionsCache) {
        // NBTタグからPersistentDataContainerに変換
        selectedItem = convertNbtToPersistentData(selectedItem);

        UniqueItemManager uniqueItemManager = plugin.getUniqueItemManager();
        debugLogger.logInfo(player, "[UNIQUE-DEBUG] Processing item, isUnique: " + uniqueItemManager.isUniqueItem(selectedItem));

        if (!uniqueItemManager.isUniqueItem(selectedItem)) {
            return new UniqueProcessingResult(selectedItem, originalCategory);
        }

        String uniqueId = uniqueItemManager.getUniqueId(selectedItem);
        debugLogger.logInfo(player, "[UNIQUE-DEBUG] Unique ID: " + uniqueId);

        if (uniqueId == null) {
            debugLogger.logInfo(player, "[UNIQUE-DEBUG] Unique ID is null, returning item");
            return new UniqueProcessingResult(selectedItem, originalCategory);
        }

        boolean alreadyCaught = uniqueItemManager.isItemAlreadyCaught(player.getWorld(), uniqueId);
        debugLogger.logInfo(player, "[UNIQUE-DEBUG] Item " + uniqueId + " already caught: " + alreadyCaught);

        if (alreadyCaught) {
            debugLogger.logInfo(player, "[UNIQUE-DEBUG] Starting re-roll for " + uniqueId);
            return performFullReFishing(player, conditionsCache, 1);
        } else {
            uniqueItemManager.markItemAsCaught(player.getWorld(), uniqueId, player);
            debugLogger.logInfo(player, "[UNIQUE-DEBUG] Successfully marked item as caught: " + uniqueId);
            selectedItem = uniqueItemManager.addUniqueLore(selectedItem, player.getWorld(), player);
            return new UniqueProcessingResult(selectedItem, originalCategory);
        }
    }


    /**
     * NBTタグからPersistentDataContainerにユニーク情報を変換する
     *
     * @param item 変換対象のアイテム
     * @return 変換後のアイテム
     */
    private ItemStack convertNbtToPersistentData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        // PersistentDataContainerに既にデータがある場合はスキップ
        var meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey uniqueKey = new NamespacedKey(plugin, "unique");
            if (container.has(uniqueKey, PersistentDataType.BYTE)) {
                return item; // 既に変換済み
            }
        }

        try {
            // NBT-APIを使用してNBTデータに直接アクセス
            NBTItem nbtItem = new NBTItem(item);
            debugLogger.logInfo(null, "[NBT-CONVERT] Item type: " + item.getType() + ", NBT keys: " + nbtItem.getKeys());

            // customizefishingタグが存在するかチェック
            if (nbtItem.getKeys().contains("customizefishing")) {
                NBTCompound customizeFishingTag = nbtItem.getCompound("customizefishing");
                debugLogger.logInfo(null, "[NBT-CONVERT] Found customizefishing tag with keys: " + customizeFishingTag.getKeys());

                // uniqueタグとunique_idタグをチェック
                if (customizeFishingTag.getKeys().contains("unique") && customizeFishingTag.getKeys().contains("unique_id")) {
                    byte uniqueFlag = customizeFishingTag.getByte("unique");
                    String uniqueId = customizeFishingTag.getString("unique_id");

                    debugLogger.logInfo(null, "[NBT-API] Found unique flag: " + uniqueFlag + ", unique_id: " + uniqueId);

                    if (uniqueFlag == 1 && uniqueId != null && !uniqueId.isEmpty()) {
                        debugLogger.logInfo(null, "[NBT-API] Processing unique item with ID: " + uniqueId);

                        // PersistentDataContainerに設定
                        if (meta != null) {
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            NamespacedKey uniqueKey = new NamespacedKey(plugin, "unique");
                            NamespacedKey uniqueIdKey = new NamespacedKey(plugin, "unique_id");

                            container.set(uniqueKey, PersistentDataType.BYTE, uniqueFlag);
                            container.set(uniqueIdKey, PersistentDataType.STRING, uniqueId);

                            // NBTタグを削除（PersistentDataContainerに移行したため）
                            nbtItem.removeKey("customizefishing");

                            // 変更を適用
                            item = nbtItem.getItem();
                            item.setItemMeta(meta);

                            debugLogger.logInfo(null, "[NBT-CONVERT] Successfully converted NBT to PersistentData for unique_id: " + uniqueId);
                        }
                    }
                } else {
                    debugLogger.logInfo(null, "[NBT-CONVERT] Missing required keys. Available: " + customizeFishingTag.getKeys());
                }
            } else {
                debugLogger.logInfo(null, "[NBT-CONVERT] No customizefishing key found in NBT");
            }
        } catch (Exception e) {
            debugLogger.logInfo(null, "[NBT-API] Error during NBT conversion: " + e.getMessage());
            e.printStackTrace();
        }

        return item;
    }

    /**
     * 完全な釣り処理を再実行（キャッシュされた条件を使用）
     *
     * @param player          プレイヤー
     * @param conditionsCache キャッシュされた釣り条件
     * @param rerollCount     再抽選回数
     * @return 釣り結果のアイテムとカテゴリ
     */
    private UniqueProcessingResult performFullReFishing(Player player, FishingConditionsCache conditionsCache, int rerollCount) {
        final int MAX_REROLLS = 3;

        if (rerollCount > MAX_REROLLS) {
            debugLogger.logInfo(player, "[CACHED-REROLL] Max re-roll attempts reached, forcing common fallback");
            // LootContextを再作成してcommonから取得
            LootContext commonContext = createLootContext(player, conditionsCache);
            ItemStack fallbackItem = getItemFromCategory("common", player, commonContext);
            return new UniqueProcessingResult(fallbackItem, "common");
        }

        debugLogger.logInfo(player, "[CACHED-REROLL] Attempt " + rerollCount + " - Using cached fishing conditions");

        try {
            // 1. キャッシュされた条件を使用（再計算不要）
            debugLogger.logInfo(player, "[CACHED-REROLL] Using cached conditions: openWater=" +
                    conditionsCache.isOpenWater() + ", weather=" + conditionsCache.weather() +
                    ", dolphins=" + conditionsCache.hasDolphinsGrace());

            // 2. ティア選択を再実行（キャッシュされたLuckResultを使用）
            String newCategory = categorySelector.determineCategoryFromConfig(
                    player,
                    conditionsCache.luckResult(),
                    conditionsCache.isOpenWater(),
                    conditionsCache.weather(),
                    conditionsCache.hasDolphinsGrace()
            );
            debugLogger.logInfo(player, "[CACHED-REROLL] Re-selected category: " + newCategory);

            // 3. 新しいloot_tableから抽選
            LootContext lootContext = createLootContext(player, conditionsCache);
            ItemStack newItem = getItemFromCategory(newCategory, player, lootContext);

            // 4. NBT変換とユニーク判定
            newItem = convertNbtToPersistentData(newItem);
            newItem = PlayerHeadProcessor.processPlayerHead(newItem, player, newCategory);

            UniqueItemManager uniqueItemManager = plugin.getUniqueItemManager();
            if (uniqueItemManager.isUniqueItem(newItem)) {
                String uniqueId = uniqueItemManager.getUniqueId(newItem);
                if (uniqueId != null && uniqueItemManager.isItemAlreadyCaught(player.getWorld(), uniqueId)) {
                    debugLogger.logInfo(player, "[CACHED-REROLL] Re-rolled item " + uniqueId + " also already caught, re-rolling again");
                    return performFullReFishing(player, conditionsCache, rerollCount + 1);
                } else if (uniqueId != null) {
                    // 新しいユニークアイテム
                    uniqueItemManager.markItemAsCaught(player.getWorld(), uniqueId, player);
                    newItem = uniqueItemManager.addUniqueLore(newItem, player.getWorld(), player);
                    debugLogger.logInfo(player, "[CACHED-REROLL] Successfully got new unique item: " + uniqueId);
                }
            } else {
                debugLogger.logInfo(player, "[CACHED-REROLL] Successfully got non-unique item: " + newItem.getType());
            }

            return new UniqueProcessingResult(newItem, newCategory);

        } catch (Exception e) {
            debugLogger.logInfo(player, "[CACHED-REROLL] Error during cached re-fishing: " + e.getMessage());
            return performFullReFishing(player, conditionsCache, rerollCount + 1);
        }
    }

    /**
     * キャッシュされた条件からLootContextを作成
     *
     * @param player          プレイヤー
     * @param conditionsCache キャッシュされた条件
     * @return LootContext
     */
    private LootContext createLootContext(Player player, FishingConditionsCache conditionsCache) {
        // 元の幸運値を再計算
        float contextLuck = Math.max(-1024f, Math.min(1024f, (float) conditionsCache.luckResult().getTotalLuck(plugin) * 10));

        return new LootContext.Builder(conditionsCache.hookLocation())
                .killer(player)
                .luck(contextLuck)
                .build();
    }


    /**
     * 指定されたカテゴリからアイテムを取得
     *
     * @param category    カテゴリ名
     * @param player      プレイヤー
     * @param lootContext ルートコンテキスト
     * @return 取得したアイテム
     */
    private ItemStack getItemFromCategory(String category, Player player, LootContext lootContext) {
        try {
            NamespacedKey lootTableKey = new NamespacedKey(plugin, "customize_fishing/gameplay/fishing/" + category);
            LootTable lootTable = plugin.getServer().getLootTable(lootTableKey);

            if (lootTable != null) {
                Collection<ItemStack> loot = lootTable.populateLoot(random, lootContext);
                if (!loot.isEmpty()) {
                    ItemStack item = loot.iterator().next();
                    debugLogger.logInfo(player, "[TIER-RETRY] Got item from " + category + ": " + item.getType());
                    return item;
                }
            }
        } catch (Exception e) {
            debugLogger.logInfo(player, "[TIER-RETRY] Error getting item from category " + category + ": " + e.getMessage());
        }

        // フォールバック
        debugLogger.logInfo(player, "[TIER-RETRY] Fallback to COD for category: " + category);
        return new ItemStack(Material.COD);
    }


    /**
     * 釣り結果を保存するレコード
     *
     * @param category        カテゴリ
     * @param probabilityInfo 確率情報
     * @param item            アイテム
     */
    public record FishingResult(String category, String probabilityInfo, ItemStack item) {
    }
}