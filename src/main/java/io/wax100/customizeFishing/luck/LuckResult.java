package io.wax100.customizeFishing.luck;

import java.util.Random;

/**
 * 幸運値の計算結果を保持するレコード
 */
public record LuckResult(
        int luckOfTheSeaLevel,
        int luckPotionLevel,
        int conduitLevel,
        double equipmentLuck,
        double weatherLuck,
        double timingLuck,
        int experienceLevel
) {
    /**
     * 宝釣りエンチャントボーナスを計算
     *
     * @return 宝釣りボーナス（パーセンテージ）
     */
    public double getLuckOfTheSeaBonus() {
        double bonus = Math.min(10, luckOfTheSeaLevel) * 0.10;
        // conduitLevel: 最大3、1増加で0.1%増加
        if (luckOfTheSeaLevel >= 127) {
            // 0.1から1.0の乱数を生成してボーナスに加える
            Random random = new Random();
            double randomBonus = 0.1 + (random.nextDouble() * Math.min(3, conduitLevel));
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
     * 装備幸運ボーナスを計算
     *
     * @return 装備幸運ボーナス（パーセンテージ）
     */
    public double getEquipmentBonus() {
        return Math.min(6, equipmentLuck) * 0.1;
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
     * @return 総合幸運値（パーセンテージ）
     */
    public double getTotalLuck() {
        return getLuckOfTheSeaBonus() + getLuckPotionBonus() + getEquipmentBonus() + weatherLuck + timingLuck + getExperienceBonus();
    }
}