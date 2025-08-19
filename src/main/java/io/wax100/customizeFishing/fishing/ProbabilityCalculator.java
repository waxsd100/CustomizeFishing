package io.wax100.customizeFishing.fishing;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ProbabilityCalculator {

    private final CustomizeFishing plugin;

    public ProbabilityCalculator(CustomizeFishing plugin) {
        this.plugin = plugin;
    }

    /**
     * 確率情報を計算してフォーマットした文字列を返す
     *
     * @param selectedCategory 選択されたカテゴリ
     * @param luckResult       幸運計算結果
     * @param weather          天気
     * @param timingResult     タイミング結果
     * @return フォーマットされた確率情報文字列
     */
    public String calculateProbabilityInfo(String selectedCategory, LuckResult luckResult, Weather weather, TimingResult timingResult) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return "";
        }

        ConfigurationSection categorySection = categoriesSection.getConfigurationSection(selectedCategory);
        if (categorySection == null) {
            return "";
        }

        double baseChance = categorySection.getDouble("chance", 0.0);
        double quality = categorySection.getDouble("quality", 0.0);
        String probabilityText = getProbabilityText(luckResult, baseChance, quality);

        String bonusText = buildBonusText(luckResult, weather, timingResult);

        return probabilityText + bonusText;
    }

    /**
     * 確率テキストを生成する
     *
     * @param luckResult 幸運計算結果
     * @param baseChance 基本確率
     * @param quality    品質値
     * @return 確率テキスト
     */
    private String getProbabilityText(LuckResult luckResult, double baseChance, double quality) {
        double totalLuck = luckResult.getTotalLuck(plugin);

        double adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);

        String probabilityText;
        if (adjustedChance <= 0) {
            probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + "0%";
        } else if (adjustedChance >= 100) {
            probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + "100%";
        } else {
            String formattedChance = DebugLogger.formatProbabilityForDisplay(adjustedChance);

            if (adjustedChance == baseChance) {
                probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance;
            } else {
                double difference = adjustedChance - baseChance;
                String formattedDiff = DebugLogger.formatProbabilityForDisplay(Math.abs(difference));

                if (difference < 0) {
                    probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance +
                            " " + ChatColor.GRAY + "(補正値:" + ChatColor.RED + " -" + formattedDiff + ChatColor.GRAY + ")";
                } else {
                    probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance +
                            " " + ChatColor.GRAY + "(補正値:" + ChatColor.GREEN + " +" + formattedDiff + ChatColor.GRAY + ")";
                }
            }
        }
        return probabilityText;
    }

    /**
     * ボーナステキストを構築する
     *
     * @param luckResult   幸運計算結果
     * @param weather      天気
     * @param timingResult タイミング結果
     * @return ボーナステキスト
     */
    private String buildBonusText(LuckResult luckResult, Weather weather, TimingResult timingResult) {
        return getLuckOfTheSeaBonusText(luckResult) +
                getLuckPotionBonusText(luckResult) +
                getEquipmentBonusText(luckResult) +
                getExperienceBonusText(luckResult) +
                getWeatherBonusText(luckResult, weather) +
                getTimingBonusText(luckResult, timingResult);
    }

    /**
     * 宝釣りエンチャントボーナステキストを取得
     *
     * @param luckResult 幸運計算結果
     * @return 宝釣りボーナステキスト
     */
    private String getLuckOfTheSeaBonusText(LuckResult luckResult) {
        if (luckResult.luckOfTheSeaLevel() <= 0) {
            return "";
        }
        double bonus = luckResult.getLuckOfTheSeaBonus(plugin);
        return " " + ChatColor.GREEN + "宝釣り+" + String.format("%.2f%%", bonus);
    }

    /**
     * 幸運ポーションボーナステキストを取得
     *
     * @param luckResult 幸運計算結果
     * @return 幸運ポーションボーナステキスト
     */
    private String getLuckPotionBonusText(LuckResult luckResult) {
        double bonus = luckResult.getLuckPotionBonus() + luckResult.getUnluckPotionPenalty(plugin);
        if (bonus == 0) {
            return "";
        }
        if (bonus > 0) {
            return " " + ChatColor.AQUA + "幸運+" + String.format("%.2f%%", bonus);
        } else {
            return " " + ChatColor.RED + "幸運" + String.format("%.2f%%", bonus);
        }
    }

    /**
     * 装備幸運ボーナステキストを取得
     *
     * @param luckResult 幸運計算結果
     * @return 装備ボーナステキスト
     */
    private String getEquipmentBonusText(LuckResult luckResult) {
        double bonus = luckResult.getEquipmentBonus(plugin);
        if (bonus == 0) {
            return "";
        }
        if (bonus > 0) {
            return " " + ChatColor.LIGHT_PURPLE + "装備+" + String.format("%.2f%%", bonus);
        } else {
            return " " + ChatColor.RED + "装備" + String.format("%.2f%%", bonus);
        }
    }

    /**
     * 経験値ボーナステキストを取得
     *
     * @param luckResult 幸運計算結果
     * @return 経験値ボーナステキスト
     */
    private String getExperienceBonusText(LuckResult luckResult) {
        if (luckResult.experienceLevel() <= 0) {
            return "";
        }
        double bonus = luckResult.getExperienceBonus();
        return " " + ChatColor.YELLOW + "経験値+" + String.format("%.2f%%", bonus);
    }

    /**
     * 天気ボーナステキストを取得
     *
     * @param luckResult 幸運計算結果
     * @param weather    天気
     * @return 天気ボーナステキスト
     */
    private String getWeatherBonusText(LuckResult luckResult, Weather weather) {
        if (luckResult.weatherLuck() <= 0) {
            return "";
        }
        String weatherName = switch (weather) {
            case RAIN -> "雨";
            case THUNDER -> "雷雨";
            default -> weather.getConfigKey();
        };
        return " " + ChatColor.BLUE + weatherName + "+" + String.format("%.2f%%", luckResult.weatherLuck());
    }

    /**
     * タイミングボーナステキストを取得
     *
     * @param luckResult   幸運計算結果
     * @param timingResult タイミング結果
     * @return タイミングボーナステキスト
     */
    private String getTimingBonusText(LuckResult luckResult, TimingResult timingResult) {
        if (timingResult == null || !timingResult.hasTiming() || luckResult.timingLuck() <= 0) {
            return "";
        }

        return " " + ChatColor.GOLD + "タイミング+" + String.format("%.2f%%", luckResult.timingLuck());
    }

    /**
     * 確率補正を計算
     *
     * @param baseChance 基本確率
     * @param quality    品質値
     * @param totalLuck  総幸運値
     * @return 補正後の確率
     */
    private double calculateAdjustedChance(double baseChance, double quality, double totalLuck) {
        if (totalLuck == 0 || quality == 0) {
            return baseChance;
        }

        if (totalLuck > 0) {
            if (quality > 0) {
                double maxMultiplier = plugin.getConfig().getDouble("luck_adjustment.max_multiplier", 3.0);
                double luckScale = plugin.getConfig().getDouble("luck_adjustment.luck_scale", 0.1);
                double qualityImpact = plugin.getConfig().getDouble("luck_adjustment.quality_impact", 0.5);

                double scaledLuck = Math.log1p(totalLuck * luckScale);
                double qualityFactor = Math.log1p(quality * qualityImpact);

                double multiplier = 1.0 + (scaledLuck * qualityFactor);
                multiplier = Math.min(multiplier, maxMultiplier);

                return baseChance * multiplier;
            } else {
                return baseChance;
            }
        } else {
            double penaltyScale = plugin.getConfig().getDouble("luck_adjustment.penalty_scale", 0.05);
            double penalty = Math.abs(totalLuck) * penaltyScale * quality;

            return Math.max(baseChance - penalty, 0.0);
        }
    }
}