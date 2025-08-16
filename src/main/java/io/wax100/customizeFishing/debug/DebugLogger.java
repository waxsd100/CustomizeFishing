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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * デバッグログを一元管理するユーティリティクラス
 */
public class DebugLogger {

    private final CustomizeFishing plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private File currentLogFile;
    private String currentDate;
    private final Map<UUID, List<String>> playerLogBuffers = new ConcurrentHashMap<>();

    public DebugLogger(CustomizeFishing plugin) {
        this.plugin = plugin;

        // logsフォルダの準備
        File pluginDataFolder = plugin.getDataFolder();
        if (!pluginDataFolder.exists()) {
            pluginDataFolder.mkdirs();
        }

        File logsFolder = new File(pluginDataFolder, "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        // 現在の日付でログファイルを初期化
        this.currentDate = LocalDateTime.now().format(fileNameFormat);
        this.currentLogFile = new File(logsFolder, currentDate + ".log");
    }

    /**
     * 日付が変わったかチェックし、必要に応じて新しいログファイルを作成
     */
    private void checkAndUpdateLogFile() {
        String newDate = LocalDateTime.now().format(fileNameFormat);
        if (!newDate.equals(currentDate)) {
            currentDate = newDate;
            File logsFolder = currentLogFile.getParentFile();
            currentLogFile = new File(logsFolder, currentDate + ".log");
        }
    }

    /**
     * プレイヤー用のログバッファを開始
     */
    public void startLogBuffer(Player player) {
        playerLogBuffers.put(player.getUniqueId(), new ArrayList<>());
    }
    
    /**
     * プレイヤー用のログバッファにメッセージを追加
     */
    private void addToBuffer(Player player, String message) {
        UUID playerId = player != null ? player.getUniqueId() : null;
        if (playerId != null && playerLogBuffers.containsKey(playerId)) {
            String timestamp = dateFormat.format(new Date());
            playerLogBuffers.get(playerId).add(String.format("[%s] %s", timestamp, message));
        } else {
            // バッファが存在しない場合は直接書き込み
            writeToFile(message);
        }
    }
    
    /**
     * プレイヤー用のログバッファをファイルに書き込んでクリア
     */
    public void flushAndClearBuffer(Player player) {
        UUID playerId = player.getUniqueId();
        List<String> buffer = playerLogBuffers.remove(playerId);
        if (buffer != null && !buffer.isEmpty()) {
            checkAndUpdateLogFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
                for (String line : buffer) {
                    writer.write(line + System.lineSeparator());
                }
                writer.flush();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to write debug log to file: " + e.getMessage());
            }
        }
    }
    
    /**
     * デバッグログをファイルに書き込み（バッファリングなし）
     */
    private void writeToFile(String message) {
        checkAndUpdateLogFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
            String timestamp = dateFormat.format(new Date());
            writer.write(String.format("[%s] %s%n", timestamp, message));
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write debug log to file: " + e.getMessage());
        }
    }

    /**
     * 釣り開始時の情報をログ出力
     */
    public void logFishingStart(Player player, boolean isOpenWater, String weather,
                                boolean hasDolphinsGrace, String forcedCategory) {

        logInfo(player, String.format(
                " Player: %s | OpenWater: %s | Weather: %s | Dolphins: %s",
                player.getName(), isOpenWater, weather, hasDolphinsGrace
        ));

        if (forcedCategory != null) {
            logInfo(player, " Using DEBUG ROD - Forced category: " + forcedCategory);
        }
    }

    /**
     * タイミング結果をログ出力
     */
    public void logTimingResult(Player player, long reactionTimeMs, TimingResult timingResult) {
        if (timingResult.hasTiming()) {
            logInfo(player, String.format(
                    " TIMING: %s (%dms) - Luck bonus: +%.1f",
                    timingResult.tier().name().toUpperCase(),
                    reactionTimeMs,
                    timingResult.luckBonus()
            ));
        } else {
            logInfo(player, String.format(
                    " TIMING: MISS (%dms) - No bonus",
                    reactionTimeMs
            ));
        }
    }

    /**
     * 幸運値の詳細をログ出力
     */
    public void logLuckBreakdown(Player player, LuckResult luckResult) {
        logInfo(player, " LUCK BREAKDOWN:");
        logInfo(player, String.format(
                "   LuckOfTheSea: %d | LuckPotion: %d | UnluckPotion: %d | Equipment: %.1f",
                luckResult.luckOfTheSeaLevel(), luckResult.luckPotionLevel(), luckResult.unluckPotionLevel(), luckResult.equipmentLuck()
        ));
        logInfo(player, String.format(
                "   Weather: %.1f | Timing: %.1f | TOTAL: %.1f",
                luckResult.weatherLuck(), luckResult.timingLuck(), luckResult.getTotalLuck(plugin)
        ));
    }

    /**
     * カテゴリ選択処理をログ出力
     */
    public void logCategorySelection(Player player, String selectedCategory, int totalCategories) {
        logInfo(player, String.format(
                " CATEGORY SELECTION: %s (from %d eligible categories)",
                selectedCategory, totalCategories
        ));
    }

    /**
     * カテゴリの詳細情報をログ出力
     */
    public void logCategoryDetails(Player player, String categoryName, int priority, double quality,
                                   double baseChance, double adjustedChance, double totalLuck) {
        logInfo(player, String.format(
                "   [%s] Priority:%d Quality:%.1f Base:%.2f%% → Adjusted:%.0f (Luck:%.1f)",
                categoryName, priority, quality, baseChance, adjustedChance, totalLuck
        ));
    }

    /**
     * アイテム置換をログ出力
     */
    public void logItemReplacement(Player player, String originalItem, String newItem, String lootTable) {
        logInfo(player, String.format(
                " ITEM REPLACEMENT: %s → %s (from %s)",
                originalItem, newItem, lootTable
        ));
    }

    /**
     * 装備の幸運値詳細をログ出力
     */
    public void logEquipmentLuck(Player player, double helmet, double chest, double legs, double boots,
                                 double mainHand, double offHand, double total) {
        logInfo(player, " EQUIPMENT LUCK:");
        logInfo(player, String.format(
                "   Helmet:%.1f Chest:%.1f Legs:%.1f Boots:%.1f",
                helmet, chest, legs, boots
        ));
        logInfo(player, String.format(
                "   MainHand:%.1f OffHand:%.1f → TOTAL:%.1f (max 4.0)",
                mainHand, offHand, total
        ));
    }

    /**
     * 一般的な情報をログ出力（プレイヤー指定あり）
     */
    public void logInfo(Player player, String message) {
        addToBuffer(player, message);
    }

    /**
     * 釣り開始をログ出力
     */
    public void logFishingStart(Player player) {
        startLogBuffer(player);
        logInfo(player, "=== FISHING EVENT START ===");
    }

    /**
     * 釣り終了をログ出力
     */
    public void logFishingEnd(Player player) {
        logInfo(player, "=== FISHING EVENT END ===");
        flushAndClearBuffer(player);
    }
}