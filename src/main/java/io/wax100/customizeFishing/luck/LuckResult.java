package io.wax100.customizeFishing.luck;

/**
 * 幸運値の計算結果を保持するレコード
 */
public record LuckResult(
    int luckOfTheSeaLevel,
    int luckPotionLevel, 
    double equipmentLuck,
    double weatherLuck,
    double timingLuck,
    double totalLuck
) {}