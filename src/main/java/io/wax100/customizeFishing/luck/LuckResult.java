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
    double timingLuck

) {
    /**
     * 総合幸運値を計算
     * @return 総合幸運値（パーセンテージ）
     */
    public double getTotalLuck() {
        // luckOfTheSeaLevel: 最大10、1増加で0.08%増加
        double luckOfTheSeaBonus = Math.min(10, luckOfTheSeaLevel) * 0.08;
        // conduitLevel: 最大3、1増加で0.1%増加
        if (luckOfTheSeaLevel >= 127) {
            // 0.1から1.0の乱数を生成してボーナスに加える
            Random random = new Random();
            double randomBonus = 0.1 + (random.nextDouble() * conduitLevel);
            luckOfTheSeaBonus += randomBonus;
        }
        // luckPotionLevel: 最大10、1増加で0.05%増加
        double luckPotionBonus = Math.min(10, luckPotionLevel) * 0.05;
        
        // equipmentLuck: 最大6、1増加で0.1%増加
        double equipmentBonus = Math.min(6, equipmentLuck) * 0.1;
        
        return luckOfTheSeaBonus + luckPotionBonus + equipmentBonus + weatherLuck + timingLuck;
    }
}