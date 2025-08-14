package io.wax100.customizeFishing.debug;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * デバッグログを一元管理するユーティリティクラス
 */
public class DebugLogger {
    
    private final CustomizeFishing plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final File debugFile;
    
    public DebugLogger(CustomizeFishing plugin) {
        this.plugin = plugin;
        
        // デバッグログファイルの準備
        File pluginDataFolder = plugin.getDataFolder();
        if (!pluginDataFolder.exists()) {
            pluginDataFolder.mkdirs();
        }
        
        this.debugFile = new File(pluginDataFolder, "debug.log");
        
        // 起動時にログファイルをクリア（サイズ制限のため）
        if (debugFile.exists() && debugFile.length() > 10 * 1024 * 1024) { // 10MB超えたらクリア
            try {
                new FileWriter(debugFile, false).close();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to clear debug log file: " + e.getMessage());
            }
        }
    }
    
    /**
     * デバッグが有効かチェック
     */
    private boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }
    
    /**
     * デバッグログをファイルに書き込み
     */
    public void writeToFile(String message) {
        if (!isDebugEnabled()) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(debugFile, true))) {
            String timestamp = dateFormat.format(new Date());
            writer.write(String.format("[%s] %s%n", timestamp, message));
            writer.flush();
        } catch (IOException e) {
            // ファイル書き込みに失敗した場合のみコンソールに出力
            plugin.getLogger().severe("Failed to write debug log to file: " + e.getMessage());
        }
    }
    
    /**
     * 釣り開始時の情報をログ出力
     */
    public void logFishingStart(Player player, boolean isOpenWater, String weather, 
                               boolean hasDolphinsGrace, String forcedCategory) {
        writeToFile("=== FISHING EVENT START ===");
        writeToFile(String.format(
            " Player: %s | OpenWater: %s | Weather: %s | Dolphins: %s",
            player.getName(), isOpenWater, weather, hasDolphinsGrace
        ));
        
        if (forcedCategory != null) {
            writeToFile(" Using DEBUG ROD - Forced category: " + forcedCategory);
        }
    }
    
    /**
     * タイミング結果をログ出力
     */
    public void logTimingResult(long reactionTimeMs, TimingResult timingResult) {
        if (timingResult.hasTiming()) {
            writeToFile(String.format(
                " TIMING: %s (%dms) - Luck bonus: +%.1f",
                timingResult.tier().name().toUpperCase(), 
                reactionTimeMs, 
                timingResult.luckBonus()
            ));
        } else {
            writeToFile(String.format(
                " TIMING: MISS (%dms) - No bonus",
                reactionTimeMs
            ));
        }
    }
    
    /**
     * 幸運値の詳細をログ出力
     */
    public void logLuckBreakdown(LuckResult luckResult) {
        double potionValue = luckResult.luckPotionLevel() * 0.5;
        writeToFile(" LUCK BREAKDOWN:");
        writeToFile(String.format(
            "   LuckOfTheSea: %d | LuckPotion: %d(%.1f) | Equipment: %.1f",
            luckResult.luckOfTheSeaLevel(), luckResult.luckPotionLevel(), 
            potionValue, luckResult.equipmentLuck()
        ));
        writeToFile(String.format(
            "   Weather: %.1f | Timing: %.1f | TOTAL: %.1f",
            luckResult.weatherLuck(), luckResult.timingLuck(), luckResult.getTotalLuck()
        ));
    }
    
    /**
     * カテゴリ選択処理をログ出力
     */
    public void logCategorySelection(String selectedCategory, int totalCategories) {
        writeToFile(String.format(
            " CATEGORY SELECTION: %s (from %d eligible categories)",
            selectedCategory, totalCategories
        ));
    }
    
    /**
     * カテゴリの詳細情報をログ出力
     */
    public void logCategoryDetails(String categoryName, int priority, double quality, 
                                  double baseChance, double adjustedChance, double totalLuck) {
        writeToFile(String.format(
            "   [%s] Priority:%d Quality:%.1f Base:%.2f%% → Adjusted:%.0f (Luck:%.1f)",
            categoryName, priority, quality, baseChance, adjustedChance, totalLuck
        ));
    }
    
    /**
     * アイテム置換をログ出力
     */
    public void logItemReplacement(String originalItem, String newItem, String lootTable) {
        writeToFile(String.format(
            " ITEM REPLACEMENT: %s → %s (from %s)",
            originalItem, newItem, lootTable
        ));
    }
    
    /**
     * 装備の幸運値詳細をログ出力
     */
    public void logEquipmentLuck(double helmet, double chest, double legs, double boots, 
                                double mainHand, double offHand, double total) {
        writeToFile(" EQUIPMENT LUCK:");
        writeToFile(String.format(
            "   Helmet:%.1f Chest:%.1f Legs:%.1f Boots:%.1f",
            helmet, chest, legs, boots
        ));
        writeToFile(String.format(
            "   MainHand:%.1f OffHand:%.1f → TOTAL:%.1f (max 4.0)",
            mainHand, offHand, total
        ));
    }
    
    /**
     * エラーをログ出力
     */
    public void logError(String message) {
        writeToFile("[ERROR] " + message);
        // エラーのみコンソールにも出力
        plugin.getLogger().severe("[CustomizeFishing ERROR] " + message);
    }
    
    /**
     * 釣り終了をログ出力
     */
    public void logFishingEnd() {
        writeToFile("=== FISHING EVENT END ===");
        writeToFile(""); // 空行で区切り
    }
    
}