package io.wax100.customizeFishing.debug;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.entity.Player;

/**
 * デバッグログを一元管理するユーティリティクラス
 */
public class DebugLogger {
    
    private final CustomizeFishing plugin;
    
    public DebugLogger(CustomizeFishing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * デバッグが有効かチェック
     */
    private boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }
    
    /**
     * 釣り開始時の情報をログ出力
     */
    public void logFishingStart(Player player, boolean isOpenWater, String weather, 
                               boolean hasDolphinsGrace, String forcedCategory) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info("=== FISHING EVENT START ===");
        plugin.getLogger().info(String.format(
            " Player: %s | OpenWater: %s | Weather: %s | Dolphins: %s",
            player.getName(), isOpenWater, weather, hasDolphinsGrace
        ));
        
        if (forcedCategory != null) {
            plugin.getLogger().info(" Using DEBUG ROD - Forced category: " + forcedCategory);
        }
    }
    
    /**
     * タイミング結果をログ出力
     */
    public void logTimingResult(long reactionTimeMs, TimingResult timingResult) {
        if (!isDebugEnabled()) return;
        
        if (timingResult.hasTiming()) {
            plugin.getLogger().info(String.format(
                " TIMING: %s (%dms) - Luck bonus: +%.1f",
                timingResult.tier().name().toUpperCase(), 
                reactionTimeMs, 
                timingResult.luckBonus()
            ));
        } else {
            plugin.getLogger().info(String.format(
                " TIMING: MISS (%dms) - No bonus",
                reactionTimeMs
            ));
        }
    }
    
    /**
     * 幸運値の詳細をログ出力
     */
    public void logLuckBreakdown(LuckResult luckResult) {
        if (!isDebugEnabled()) return;
        
        double potionValue = luckResult.luckPotionLevel() * 0.5;
        plugin.getLogger().info(" LUCK BREAKDOWN:");
        plugin.getLogger().info(String.format(
            "   LuckOfTheSea: %d | LuckPotion: %d(%.1f) | Equipment: %.1f",
            luckResult.luckOfTheSeaLevel(), luckResult.luckPotionLevel(), 
            potionValue, luckResult.equipmentLuck()
        ));
        plugin.getLogger().info(String.format(
            "   Weather: %.1f | Timing: %.1f | TOTAL: %.1f",
            luckResult.weatherLuck(), luckResult.timingLuck(), luckResult.totalLuck()
        ));
    }
    
    /**
     * カテゴリ選択処理をログ出力
     */
    public void logCategorySelection(String selectedCategory, int totalCategories) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info(String.format(
            " CATEGORY SELECTION: %s (from %d eligible categories)",
            selectedCategory, totalCategories
        ));
    }
    
    /**
     * カテゴリの詳細情報をログ出力
     */
    public void logCategoryDetails(String categoryName, int priority, double quality, 
                                  double baseChance, double adjustedChance, double totalLuck) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info(String.format(
            "   [%s] Priority:%d Quality:%.1f Base:%.2f%% → Adjusted:%.0f (Luck:%.1f)",
            categoryName, priority, quality, baseChance, adjustedChance, totalLuck
        ));
    }
    
    /**
     * 重み付き抽選の結果をログ出力
     */
    public void logWeightedSelection(String selectedCategory, double roll, double threshold) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info(String.format(
            " SELECTION RESULT: %s (roll: %.2f < threshold: %.2f)",
            selectedCategory, roll, threshold
        ));
    }
    
    /**
     * アイテム置換をログ出力
     */
    public void logItemReplacement(String originalItem, String newItem, String lootTable) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info(String.format(
            " ITEM REPLACEMENT: %s → %s (from %s)",
            originalItem, newItem, lootTable
        ));
    }
    
    /**
     * 装備の幸運値詳細をログ出力
     */
    public void logEquipmentLuck(double helmet, double chest, double legs, double boots, 
                                double mainHand, double offHand, double total) {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info(" EQUIPMENT LUCK:");
        plugin.getLogger().info(String.format(
            "   Helmet:%.1f Chest:%.1f Legs:%.1f Boots:%.1f",
            helmet, chest, legs, boots
        ));
        plugin.getLogger().info(String.format(
            "   MainHand:%.1f OffHand:%.1f → TOTAL:%.1f (max 4.0)",
            mainHand, offHand, total
        ));
    }
    
    /**
     * エラーをログ出力
     */
    public void logError(String message) {
        plugin.getLogger().severe("[ERROR] " + message);
    }
    
    /**
     * 釣り終了をログ出力
     */
    public void logFishingEnd() {
        if (!isDebugEnabled()) return;
        
        plugin.getLogger().info("=== FISHING EVENT END ===");
        plugin.getLogger().info(""); // 空行で区切り
    }
}