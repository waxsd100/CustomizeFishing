package io.wax100.customizeFishing.debug;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * デバッグログを一元管理するユーティリティクラス
 */
public class DebugLogger {

    private final CustomizeFishing plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private final File logsFolder;
    private final File currentLogFile;

    public DebugLogger(CustomizeFishing plugin) {
        this.plugin = plugin;

        // logsフォルダの準備
        File pluginDataFolder = plugin.getDataFolder();
        if (!pluginDataFolder.exists()) {
            pluginDataFolder.mkdirs();
        }

        this.logsFolder = new File(pluginDataFolder, "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        // 現在のログファイルを初期化
        this.currentLogFile = new File(logsFolder, "debug.log");
        
        // 起動時にログファイルをローテーション
        rotateLogFile();
    }

    /**
     * ログファイルをローテーションする
     */
    private void rotateLogFile() {
        if (!currentLogFile.exists()) {
            return;
        }

        try {
            // ファイル作成時刻を取得してファイル名に使用
            String timestamp = LocalDateTime.now().format(fileNameFormat);
            File rotatedFile = new File(logsFolder, "debug-" + timestamp + ".log");
            
            // 既存のログファイルをリネーム
            Files.move(currentLogFile.toPath(), rotatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // 古いログファイルを削除
            cleanupOldLogs();
            
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to rotate log file: " + e.getMessage());
        }
    }

    /**
     * 古いログファイルを削除する
     */
    private void cleanupOldLogs() {
        File[] logFiles = logsFolder.listFiles((dir, name) -> 
            name.startsWith("debug-") && name.endsWith(".log"));

        // 最大保持ログファイル数
        int maxLogFiles = 60;
        if (logFiles == null || logFiles.length <= maxLogFiles) {
            return;
        }

        // ファイルを作成日時順にソート（古い順）
        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

        // 古いファイルを削除
        for (int i = 0; i < logFiles.length - maxLogFiles; i++) {
            if (logFiles[i].delete()) {
                plugin.getLogger().info("Deleted old log file: " + logFiles[i].getName());
            }
        }
    }

    /**
     * ファイルサイズをチェックしてローテーションが必要かどうか判定
     */
    private void checkAndRotateIfNeeded() {
        // 100MB
        long maxFileSize = 100 * 1024 * 1024;
        if (currentLogFile.exists() && currentLogFile.length() > maxFileSize) {
            rotateLogFile();
        }
    }

    /**
     * デバッグログをファイルに書き込み
     */
    private void writeToFile(String message) {
        // ファイルサイズをチェックしてローテーション
        checkAndRotateIfNeeded();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
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

        logInfo(String.format(
                " Player: %s | OpenWater: %s | Weather: %s | Dolphins: %s",
                player.getName(), isOpenWater, weather, hasDolphinsGrace
        ));

        if (forcedCategory != null) {
            logInfo(" Using DEBUG ROD - Forced category: " + forcedCategory);
        }
    }

    /**
     * タイミング結果をログ出力
     */
    public void logTimingResult(long reactionTimeMs, TimingResult timingResult) {
        if (timingResult.hasTiming()) {
            logInfo(String.format(
                    " TIMING: %s (%dms) - Luck bonus: +%.1f",
                    timingResult.tier().name().toUpperCase(),
                    reactionTimeMs,
                    timingResult.luckBonus()
            ));
        } else {
            logInfo(String.format(
                    " TIMING: MISS (%dms) - No bonus",
                    reactionTimeMs
            ));
        }
    }

    /**
     * 幸運値の詳細をログ出力
     */
    public void logLuckBreakdown(LuckResult luckResult) {
        logInfo(" LUCK BREAKDOWN:");
        logInfo(String.format(
                "   LuckOfTheSea: %d | LuckPotion: %d | Equipment: %.1f",
                luckResult.luckOfTheSeaLevel(), luckResult.luckPotionLevel(), luckResult.equipmentLuck()
        ));
        logInfo(String.format(
                "   Weather: %.1f | Timing: %.1f | TOTAL: %.1f",
                luckResult.weatherLuck(), luckResult.timingLuck(), luckResult.getTotalLuck()
        ));
    }

    /**
     * カテゴリ選択処理をログ出力
     */
    public void logCategorySelection(String selectedCategory, int totalCategories) {
        logInfo(String.format(
                " CATEGORY SELECTION: %s (from %d eligible categories)",
                selectedCategory, totalCategories
        ));
    }

    /**
     * カテゴリの詳細情報をログ出力
     */
    public void logCategoryDetails(String categoryName, int priority, double quality,
                                   double baseChance, double adjustedChance, double totalLuck) {
        logInfo(String.format(
                "   [%s] Priority:%d Quality:%.1f Base:%.2f%% → Adjusted:%.0f (Luck:%.1f)",
                categoryName, priority, quality, baseChance, adjustedChance, totalLuck
        ));
    }

    /**
     * アイテム置換をログ出力
     */
    public void logItemReplacement(String originalItem, String newItem, String lootTable) {
        logInfo(String.format(
                " ITEM REPLACEMENT: %s → %s (from %s)",
                originalItem, newItem, lootTable
        ));
    }

    /**
     * 装備の幸運値詳細をログ出力
     */
    public void logEquipmentLuck(double helmet, double chest, double legs, double boots,
                                 double mainHand, double offHand, double total) {
        logInfo(" EQUIPMENT LUCK:");
        logInfo(String.format(
                "   Helmet:%.1f Chest:%.1f Legs:%.1f Boots:%.1f",
                helmet, chest, legs, boots
        ));
        logInfo(String.format(
                "   MainHand:%.1f OffHand:%.1f → TOTAL:%.1f (max 4.0)",
                mainHand, offHand, total
        ));
    }

    /**
     * 一般的な情報をログ出力
     */
    public void logInfo(String message) {
        writeToFile(message);
    }

    /**
     * エラーをログ出力
     */
    public void logError(String message) {
        logInfo("[ERROR] " + message);
    }

    /**
     * 釣り開始をログ出力
     */
    public void logFishingStart() {
        logInfo("=== FISHING EVENT START ===");
    }

    /**
     * 釣り終了をログ出力
     */
    public void logFishingEnd() {
        logInfo("=== FISHING EVENT END ===");
    }
}