package io.wax100.customizeFishing.luck;

import io.wax100.customizeFishing.CustomizeFishing;

/**
 * 幸運値の計算結果を保持するレコード
 */
public record LuckResult(
        int luckOfTheSeaLevel,
        int luckPotionLevel,
        int unluckPotionLevel,
        int conduitLevel,
        double equipmentLuck,
        double weatherLuck,
        double timingLuck,
        int experienceLevel
) {

    /**
     * 宝釣りエンチャントボーナスを計算
     *
     * @param plugin プラグインインスタンス
     * @return 宝釣りボーナス（パーセンテージ）
     */
    public double getLuckOfTheSeaBonus(CustomizeFishing plugin) {
        // 基本設定値（プラグインがnullの場合のデフォルト）
        double perLevel = 0.10;
        int maxLevel = 10;
        int specialMinLevel = 127;
        double baseRandom = 0.1;
        boolean specialEnabled = true;
        boolean conduitMultiplierEnabled = true;

        // プラグインが利用可能な場合は設定値を取得
        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.luck_of_the_sea.per_level", 0.10);
            maxLevel = plugin.getConfig().getInt("luck_effects.luck_of_the_sea.max_level", 10);
            specialEnabled = plugin.getConfig().getBoolean("luck_effects.luck_of_the_sea.special_bonus.enabled", true);
            specialMinLevel = plugin.getConfig().getInt("luck_effects.luck_of_the_sea.special_bonus.min_level", 127);
            baseRandom = plugin.getConfig().getDouble("luck_effects.luck_of_the_sea.special_bonus.base_random", 0.1);
            conduitMultiplierEnabled = plugin.getConfig().getBoolean("luck_effects.luck_of_the_sea.special_bonus.conduit_multiplier", true);
        }

        // 基本ボーナス計算
        double bonus = Math.min(maxLevel, luckOfTheSeaLevel) * perLevel;

        // 特殊ボーナス（高レベル時）
        if (specialEnabled && luckOfTheSeaLevel >= specialMinLevel) {
            double randomBonus = baseRandom;

            // コンジットパワー倍率適用（最大値の半分を固定ボーナスとして）
            if (conduitMultiplierEnabled && conduitLevel > 0) {
                double conduitMultiplier = Math.min(3, conduitLevel);
                randomBonus += (conduitMultiplier * 0.5); // ランダムの代わりに固定値の50%
            }

            bonus += randomBonus;
        }

        return bonus;
    }

    /**
     * 幸運ポーションボーナスを計算
     *
     * @return 幸運ポーションボーナス（パーセンテージ）
     */
    public double getLuckPotionBonus() {
        return Math.min(10, luckPotionLevel) * 0.07;
    }

    /**
     * 不幸ポーションペナルティを計算
     *
     * @return 不幸ポーションペナルティ（パーセンテージ）
     */
    public double getUnluckPotionPenalty(CustomizeFishing plugin) {
        double perLevel = -0.44; // デフォルト値
        int maxLevel = 10;

        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.unluck_potion.per_level", -0.29);
            maxLevel = plugin.getConfig().getInt("luck_effects.unluck_potion.max_level", 10);
        }

        return Math.min(maxLevel, unluckPotionLevel) * perLevel;
    }

    /**
     * 装備幸運ボーナスを計算（負の値も考慮）
     *
     * @return 装備幸運ボーナス（パーセンテージ）
     */
    public double getEquipmentBonus() {
        double perPoint = 0.1;
        return equipmentLuck * perPoint;
    }

    /**
     * 経験値ボーナスを計算
     *
     * @return 経験値ボーナス（パーセンテージ）
     */
    public double getExperienceBonus() {
        return Math.min(100, experienceLevel) * 0.01;
    }

    /**
     * 総合幸運値を計算
     *
     * @param plugin プラグインインスタンス（nullの場合は相殺・制限なし）
     * @return 総合幸運値（パーセンテージ）
     */
    public double getTotalLuck(CustomizeFishing plugin) {
        // 基本的な幸運値計算
        double baseLuck = getLuckOfTheSeaBonus(plugin) + getEquipmentBonus() + weatherLuck + timingLuck + getExperienceBonus();

        // 幸運と不幸の相殺計算
        double potionLuck = getLuckPotionBonus() + getUnluckPotionPenalty(plugin);

        double totalLuck = baseLuck + potionLuck;

        // config.ymlの制限を適用（pluginがある場合のみ）
        if (plugin != null) {
            double minLuck = plugin.getConfig().getDouble("luck_calculation.min_total_luck", -10.0);
            double maxLuck = plugin.getConfig().getDouble("luck_calculation.max_total_luck", 10.0);
            totalLuck = Math.max(minLuck, Math.min(maxLuck, totalLuck));
        }

        return totalLuck;
    }
}