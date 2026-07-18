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
        int baseLevels = 10;
        double extendedLogScale = 0.0;
        int specialMinLevel = 127;
        boolean specialEnabled = true;
        boolean conduitMultiplierEnabled = true;

        // プラグインが利用可能な場合は設定値を取得
        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.luck_of_the_sea.per_level", 0.10);
            maxLevel = plugin.getConfig().getInt("luck_effects.luck_of_the_sea.max_level", 10);
            baseLevels = plugin.getConfig().getInt("luck_effects.luck_of_the_sea.base_levels", Math.min(10, maxLevel));
            extendedLogScale = plugin.getConfig().getDouble("luck_effects.luck_of_the_sea.extended_log_scale", 0.0);
            specialEnabled = plugin.getConfig().getBoolean("luck_effects.luck_of_the_sea.special_bonus.enabled", true);
            specialMinLevel = plugin.getConfig().getInt("luck_effects.luck_of_the_sea.special_bonus.min_level", 127);
            conduitMultiplierEnabled = plugin.getConfig().getBoolean("luck_effects.luck_of_the_sea.special_bonus.conduit_multiplier", true);
        }

        // 基本ボーナス計算: base_levels までは線形、超過分は対数カーブで max_level まで緩やかに伸びる
        // マイナス宝釣り（呪い竿）はプラス側と対称のカーブでペナルティになる
        int effectiveLevel = Math.min(maxLevel, Math.abs(luckOfTheSeaLevel));
        double bonus = Math.min(baseLevels, effectiveLevel) * perLevel;
        if (effectiveLevel > baseLevels && extendedLogScale > 0) {
            bonus += extendedLogScale * Math.log1p(effectiveLevel - baseLevels);
        }
        if (luckOfTheSeaLevel < 0) {
            bonus = -bonus;
        }

        // 特殊ボーナス（高レベル時）
        if (specialEnabled && luckOfTheSeaLevel >= specialMinLevel) {
            // コンジットパワー倍率適用（最大値の半分を固定ボーナスとして）
            if (conduitMultiplierEnabled && conduitLevel > 0) {
                double conduitMultiplier = Math.min(10, conduitLevel);
                double conduitBonus = conduitMultiplier * 0.5;
                bonus += conduitBonus;
            }
        }

        return bonus;
    }

    /**
     * 幸運ポーションボーナスを計算
     *
     * @param plugin プラグインインスタンス
     * @return 幸運ポーションボーナス（パーセンテージ）
     */
    public double getLuckPotionBonus(CustomizeFishing plugin) {
        double perLevel = 0.07; // デフォルト値
        int maxLevel = 10;
        double extendedLogScale = 0.0;

        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.luck_potion.per_level", 0.07);
            maxLevel = plugin.getConfig().getInt("luck_effects.luck_potion.max_level", 10);
            extendedLogScale = plugin.getConfig().getDouble("luck_effects.luck_potion.extended_log_scale", 0.0);
        }

        double bonus = Math.min(maxLevel, luckPotionLevel) * perLevel;
        if (luckPotionLevel > maxLevel && extendedLogScale > 0) {
            bonus += extendedLogScale * Math.log1p(luckPotionLevel - maxLevel);
        }
        return bonus;
    }

    /**
     * 不幸ポーションペナルティを計算
     *
     * @return 不幸ポーションペナルティ（パーセンテージ）
     */
    public double getUnluckPotionPenalty(CustomizeFishing plugin) {
        double perLevel = -0.44; // デフォルト値
        int maxLevel = 10;
        double extendedLogScale = 0.0;

        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.unluck_potion.per_level", -0.29);
            maxLevel = plugin.getConfig().getInt("luck_effects.unluck_potion.max_level", 10);
            extendedLogScale = plugin.getConfig().getDouble("luck_effects.unluck_potion.extended_log_scale", 0.0);
        }

        double penalty = Math.min(maxLevel, unluckPotionLevel) * perLevel;
        if (unluckPotionLevel > maxLevel && extendedLogScale > 0) {
            penalty -= extendedLogScale * Math.log1p(unluckPotionLevel - maxLevel);
        }
        return penalty;
    }

    /**
     * 装備幸運ボーナスを計算（負の値も考慮）
     *
     * @param plugin プラグインインスタンス
     * @return 装備幸運ボーナス（パーセンテージ）
     */
    public double getEquipmentBonus(CustomizeFishing plugin) {
        double perPoint = 0.1; // デフォルト値

        if (plugin != null) {
            perPoint = plugin.getConfig().getDouble("luck_effects.equipment_luck.per_point", 0.1);
        }

        return equipmentLuck * perPoint;
    }

    /**
     * 経験値ボーナスを計算
     *
     * @param plugin プラグインインスタンス
     * @return 経験値ボーナス（パーセンテージ）
     */
    public double getExperienceBonus(CustomizeFishing plugin) {
        double perLevel = 0.01; // デフォルト値
        int maxLevel = 100;
        double extendedLogScale = 0.0;

        if (plugin != null) {
            perLevel = plugin.getConfig().getDouble("luck_effects.experience_level.per_level", 0.01);
            maxLevel = plugin.getConfig().getInt("luck_effects.experience_level.max_level", 100);
            extendedLogScale = plugin.getConfig().getDouble("luck_effects.experience_level.extended_log_scale", 0.0);
        }

        double bonus = Math.min(maxLevel, experienceLevel) * perLevel;
        if (experienceLevel > maxLevel && extendedLogScale > 0) {
            bonus += extendedLogScale * Math.log1p(experienceLevel - maxLevel);
        }
        return bonus;
    }

    /**
     * 総合幸運値を計算
     *
     * @param plugin プラグインインスタンス（nullの場合は相殺・制限なし）
     * @return 総合幸運値（パーセンテージ）
     */
    public double getTotalLuck(CustomizeFishing plugin) {
        // 基本的な幸運値計算
        double baseLuck = getLuckOfTheSeaBonus(plugin) + getEquipmentBonus(plugin) + weatherLuck + timingLuck + getExperienceBonus(plugin);

        // 幸運と不幸の相殺計算
        double potionLuck = getLuckPotionBonus(plugin) + getUnluckPotionPenalty(plugin);

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