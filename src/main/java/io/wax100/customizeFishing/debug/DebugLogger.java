package io.wax100.customizeFishing.debug;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * デバッグログを一元管理するユーティリティクラス
 */
public class DebugLogger {

    private final CustomizeFishing plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<UUID, List<String>> playerLogBuffers = new ConcurrentHashMap<>();
    private File currentLogFile;
    private String currentDate;

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
     * ログ用の確率フォーマット（小数点表記）
     * 例: 0.0001% → "0.000100%", 0.0023% → "0.0023%", 1.5% → "1.5%"
     */
    public static String formatProbabilityForLog(double chance) {
        if (chance == 0) {
            return "0%";
        } else if (chance < 0.001) {
            // 0.001%未満は小数点6桁（0.00にならないよう）
            return String.format("%.6f%%", chance);
        } else if (chance < 0.01) {
            // 0.001% ～ 0.01%は小数点4桁
            return String.format("%.4f%%", chance);
        } else if (chance < 0.1) {
            // 0.01% ～ 0.1%は小数点3桁
            return String.format("%.3f%%", chance);
        } else if (chance < 1.0) {
            // 0.1% ～ 1%は小数点2桁
            return String.format("%.2f%%", chance);
        } else if (chance < 10.0) {
            // 1% ～ 10%は小数点1桁
            return String.format("%.1f%%", chance);
        } else {
            // 10%以上は整数
            return String.format("%.0f%%", chance);
        }
    }

    /**
     * 画面表示用の確率フォーマット（科学的記法）
     * 例: 0.0001% → "1.0×10⁻⁴%", 0.0023% → "2.3×10⁻³%", 1.5% → "1.5%"
     */
    public static String formatProbabilityForDisplay(double chance) {
        if (chance == 0) {
            return "0%";
        } else if (chance < 0.01) {
            // 0.01%未満は科学的記法
            double coefficient = chance;
            int exponent = 0;

            // 係数を1.0以上10.0未満にする
            while (coefficient < 1.0) {
                coefficient *= 10;
                exponent--;
            }

            // 係数を適切な桁数で丸める
            if (coefficient >= 10.0) {
                coefficient /= 10;
                exponent++;
            }

            return String.format("%.1f×10%s%%", coefficient, formatExponent(exponent));
        } else if (chance < 10.0) {
            // 0.01% ～ 10%は小数点表記
            if (chance < 1.0) {
                return String.format("%.2f%%", chance);
            } else {
                return String.format("%.1f%%", chance);
            }
        } else {
            // 10%以上は整数
            return String.format("%.0f%%", chance);
        }
    }

    /**
     * 指数を上付き文字でフォーマット
     */
    private static String formatExponent(int exponent) {
        String exp = String.valueOf(Math.abs(exponent));
        StringBuilder result = new StringBuilder();

        if (exponent < 0) {
            result.append("⁻");
        }

        for (char c : exp.toCharArray()) {
            switch (c) {
                case '0' -> result.append("⁰");
                case '1' -> result.append("¹");
                case '2' -> result.append("²");
                case '3' -> result.append("³");
                case '4' -> result.append("⁴");
                case '5' -> result.append("⁵");
                case '6' -> result.append("⁶");
                case '7' -> result.append("⁷");
                case '8' -> result.append("⁸");
                case '9' -> result.append("⁹");
            }
        }

        return result.toString();
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
    public void logFishingStart(Player player, boolean isOpenWater, Weather weather,
                                boolean hasDolphinsGrace, String forcedCategory) {

        logInfo(player, String.format(
                " Player: %s | OpenWater: %s | Weather: %s | Dolphins: %s",
                player.getName(), isOpenWater, weather.getConfigKey(), hasDolphinsGrace
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
                "   [%s] Priority:%d Quality:%.1f Base:%s → Adjusted:%s (Luck:%.1f)",
                categoryName, priority, quality,
                formatProbability(baseChance),
                formatProbability(adjustedChance),
                totalLuck
        ));
    }

    /**
     * 確率を見やすい形式でフォーマット（ログ用・小数点表記）
     * 例: 0.0001% → "0.000100%", 0.0023% → "0.0023%", 1.5% → "1.5%"
     */
    private String formatProbability(double chance) {
        return formatProbabilityForLog(chance);
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
                                 double mainHand, double offHand, double totalLuck) {
        logInfo(player, String.format(
                "   Helmet:%.1f Chest:%.1f Legs:%.1f Boots:%.1f MainHand:%.1f OffHand:%.1f",
                helmet, chest, legs, boots, mainHand, offHand
        ));
        logInfo(player, String.format(
                "    → TOTAL:%.1f",
                totalLuck
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