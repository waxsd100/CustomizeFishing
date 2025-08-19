package io.wax100.customizeFishing.fishing;

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
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

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

                        selectedItem = handleUniqueItemProcessing(selectedItem, player, category, lootTable, lootContext);

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
     * @param selectedItem 選択されたアイテム
     * @param player       プレイヤー
     * @param category     カテゴリ
     * @param lootTable    ルートテーブル
     * @param lootContext  ルートコンテキスト
     * @return 処理後のアイテム
     */
    private ItemStack handleUniqueItemProcessing(ItemStack selectedItem, Player player, String category,
                                                 LootTable lootTable, LootContext lootContext) {
        UniqueItemManager uniqueItemManager = plugin.getUniqueItemManager();
        if (!uniqueItemManager.isUniqueItem(selectedItem)) {
            return selectedItem;
        }

        String uniqueId = uniqueItemManager.getUniqueId(selectedItem);
        if (uniqueId == null) {
            return selectedItem;
        }

        if (uniqueItemManager.isItemAlreadyCaught(player.getWorld(), uniqueId)) {
            return rollUniqueItem(uniqueId, player, category, lootTable, lootContext);
        } else {
            uniqueItemManager.markItemAsCaught(player.getWorld(), uniqueId, player);
            debugLogger.logInfo(player, "[UNIQUE] Marked item as caught: " + uniqueId);
            selectedItem = uniqueItemManager.addUniqueLore(selectedItem, player.getWorld(), player);
            return selectedItem;
        }
    }

    /**
     * 既に釣られたユニークアイテムの再抽選を行う
     *
     * @param originalUniqueId 元のユニークID
     * @param player           プレイヤー
     * @param category         カテゴリ
     * @param lootTable        ルートテーブル
     * @param lootContext      ルートコンテキスト
     * @return 再抽選後のアイテム
     */
    private ItemStack rollUniqueItem(String originalUniqueId, Player player,
                                     String category, LootTable lootTable, LootContext lootContext) {
        debugLogger.logInfo(player, "[UNIQUE] Item " + originalUniqueId + " already caught, re-rolling loot");

        Collection<ItemStack> rerolledLoot = lootTable.populateLoot(random, lootContext);
        if (rerolledLoot.isEmpty()) {
            debugLogger.logInfo(player, "[UNIQUE] Re-roll failed, replacing with fish");
            return new ItemStack(Material.COD);
        }

        ItemStack rerolledItem = rerolledLoot.iterator().next();
        rerolledItem = PlayerHeadProcessor.processPlayerHead(rerolledItem, player, category);

        return processRollChain(rerolledItem, player, category, lootTable, lootContext, 1);
    }

    /**
     * 再抽選の連鎖処理（無限ループ防止）
     *
     * @param currentItem 現在のアイテム
     * @param player      プレイヤー
     * @param category    カテゴリ
     * @param lootTable   ルートテーブル
     * @param lootContext ルートコンテキスト
     * @param rollCount   現在の再抽選回数
     * @return 最終的なアイテム
     */
    private ItemStack processRollChain(ItemStack currentItem, Player player, String category,
                                       LootTable lootTable, LootContext lootContext, int rollCount) {
        final int MAX_ROLLS = 3;
        UniqueItemManager uniqueItemManager = plugin.getUniqueItemManager();

        if (rollCount >= MAX_ROLLS) {
            debugLogger.logInfo(player, "[UNIQUE] Max re-roll attempts reached, keeping current item");
            if (uniqueItemManager.isUniqueItem(currentItem)) {
                currentItem = uniqueItemManager.addUniqueLore(currentItem, player.getWorld(), player);
            }
            return finalizeUniqueItem(currentItem, player);
        }

        if (!uniqueItemManager.isUniqueItem(currentItem)) {
            return currentItem;
        }

        String uniqueId = uniqueItemManager.getUniqueId(currentItem);
        if (uniqueId == null) {
            return currentItem;
        }

        if (uniqueItemManager.isItemAlreadyCaught(player.getWorld(), uniqueId)) {
            debugLogger.logInfo(player, "[UNIQUE] Re-roll " + rollCount + ": Item " + uniqueId +
                    " also already caught, re-rolling again");

            Collection<ItemStack> nextRoll = lootTable.populateLoot(random, lootContext);
            if (!nextRoll.isEmpty()) {
                ItemStack nextItem = nextRoll.iterator().next();
                nextItem = PlayerHeadProcessor.processPlayerHead(nextItem, player, category);
                return processRollChain(nextItem, player, category, lootTable, lootContext, rollCount + 1);
            } else {
                debugLogger.logInfo(player, "[UNIQUE] Re-roll failed, replacing with fish");
                return new ItemStack(Material.COD);
            }
        } else {
            return finalizeUniqueItem(currentItem, player);
        }
    }

    /**
     * ユニークアイテムの最終処理
     *
     * @param item   アイテム
     * @param player プレイヤー
     * @return 処理済みアイテム
     */
    private ItemStack finalizeUniqueItem(ItemStack item, Player player) {
        UniqueItemManager uniqueItemManager = plugin.getUniqueItemManager();
        if (uniqueItemManager.isUniqueItem(item)) {
            String uniqueId = uniqueItemManager.getUniqueId(item);
            if (uniqueId != null && !uniqueItemManager.isItemAlreadyCaught(player.getWorld(), uniqueId)) {
                uniqueItemManager.markItemAsCaught(player.getWorld(), uniqueId, player);
                debugLogger.logInfo(player, "[UNIQUE] Marked re-rolled item as caught: " + uniqueId);
                item = uniqueItemManager.addUniqueLore(item, player.getWorld(), player);
            }
        }
        return item;
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