package io.wax100.customizeFishing.luck;

/**
 * 幸運値の計算結果を保持するレコード
 */
public record LuckResult(
    int luckOfTheSeaLevel,
    int luckPotionLevel, 
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
        
        // luckPotionLevel: 最大10、1増加で0.05%増加
        double luckPotionBonus = Math.min(10, luckPotionLevel) * 0.05;
        
        // equipmentLuck: 最大6、1増加で0.1%増加
        double equipmentBonus = Math.min(6, equipmentLuck) * 0.1;
        
        return luckOfTheSeaBonus + luckPotionBonus + equipmentBonus + weatherLuck + timingLuck;
    }
}