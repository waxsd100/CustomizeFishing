package io.wax100.customizeFishing.fishing;

import io.wax100.customizeFishing.CustomizeFishing;

/**
 * 幸運値によるカテゴリ確率補正の設定と計算をまとめたレコード
 * <p>
 * 補正は2段階で適用される:
 * <ol>
 *   <li>品質倍率: 乗算型 + 対数減衰（quality > 0 のカテゴリのみ、正の幸運時）</li>
 *   <li>しきい値ランプ: min_total_luck を持つカテゴリは解禁直後は min_factor 倍から始まり、
 *       幸運が min_total_luck + range に達して初めて本来の重みになる（段差解禁の防止）</li>
 * </ol>
 *
 * @param maxMultiplier 品質倍率の最大値
 * @param luckScale     運の影響度（小さいほど緩やか）
 * @param qualityImpact 品質の影響度（小さいほど緩やか）
 * @param penaltyScale  負の運のペナルティ度
 * @param rampEnabled   しきい値ランプの有効/無効
 * @param rampRange     満額の重みに達するまでに必要な超過幸運値
 * @param rampMinFactor 解禁直後（総幸運値 = min_total_luck）の重み係数
 */
public record LuckAdjustment(
        double maxMultiplier,
        double luckScale,
        double qualityImpact,
        double penaltyScale,
        boolean rampEnabled,
        double rampRange,
        double rampMinFactor
) {

    public static LuckAdjustment fromConfig(CustomizeFishing plugin) {
        return new LuckAdjustment(
                plugin.getConfig().getDouble("luck_adjustment.max_multiplier", 3.0),
                plugin.getConfig().getDouble("luck_adjustment.luck_scale", 0.1),
                plugin.getConfig().getDouble("luck_adjustment.quality_impact", 0.5),
                plugin.getConfig().getDouble("luck_adjustment.penalty_scale", 0.05),
                plugin.getConfig().getBoolean("luck_adjustment.threshold_ramp.enabled", true),
                plugin.getConfig().getDouble("luck_adjustment.threshold_ramp.range", 8.0),
                plugin.getConfig().getDouble("luck_adjustment.threshold_ramp.min_factor", 0.05)
        );
    }

    /**
     * 確率補正を計算
     *
     * @param baseChance   基本確率（重み）
     * @param quality      品質値
     * @param totalLuck    総幸運値
     * @param minTotalLuck カテゴリの解禁しきい値（conditions.min_total_luck、無指定は0）
     * @return 補正後の確率（重み）
     */
    public double calculateAdjustedChance(double baseChance, double quality, double totalLuck, double minTotalLuck) {
        if (totalLuck > 0) {
            double adjusted = baseChance;
            if (quality > 0) {
                double scaledLuck = Math.log1p(totalLuck * luckScale);
                double qualityFactor = Math.log1p(quality * qualityImpact);
                double multiplier = 1.0 + (scaledLuck * qualityFactor);
                adjusted = baseChance * Math.min(multiplier, maxMultiplier);
            }
            return adjusted * thresholdRampFactor(totalLuck, minTotalLuck);
        }

        if (totalLuck < 0 && quality != 0) {
            double penalty = Math.abs(totalLuck) * penaltyScale * quality;
            return Math.max(baseChance - penalty, 0.0);
        }

        return baseChance;
    }

    /**
     * しきい値ランプ係数を計算
     * <p>
     * 係数 = min_factor + (1 - min_factor) × min(1, (総幸運値 - min_total_luck) / range)
     *
     * @param totalLuck    総幸運値
     * @param minTotalLuck カテゴリの解禁しきい値
     * @return 重み係数（min_factor 〜 1.0）。しきい値を持たないカテゴリは常に1.0
     */
    public double thresholdRampFactor(double totalLuck, double minTotalLuck) {
        if (!rampEnabled || minTotalLuck <= 0 || rampRange <= 0) {
            return 1.0;
        }
        double excess = totalLuck - minTotalLuck;
        if (excess <= 0) {
            // 条件チェック済みなら excess >= 0 のはずだが、境界値は解禁直後として扱う
            return rampMinFactor;
        }
        double progress = Math.min(1.0, excess / rampRange);
        return rampMinFactor + (1.0 - rampMinFactor) * progress;
    }
}
