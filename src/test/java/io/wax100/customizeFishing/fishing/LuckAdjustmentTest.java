package io.wax100.customizeFishing.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckAdjustmentTest {

    // config.ymlのデフォルト値と同じ設定
    private final LuckAdjustment adjustment = new LuckAdjustment(3.0, 0.1, 0.5, 0.05, true, 8.0, 0.05);

    @Test
    void testRampFactorAtThreshold() {
        // 解禁直後は min_factor 倍まで抑制される
        assertEquals(0.05, adjustment.thresholdRampFactor(16.0, 16.0), 1e-9);
    }

    @Test
    void testRampFactorMidway() {
        // しきい値+range/2 では min_factor と 1.0 の中間
        assertEquals(0.05 + 0.95 * 0.5, adjustment.thresholdRampFactor(20.0, 16.0), 1e-9);
    }

    @Test
    void testRampFactorFullBeyondRange() {
        // しきい値+range 以上で満額
        assertEquals(1.0, adjustment.thresholdRampFactor(24.0, 16.0), 1e-9);
        assertEquals(1.0, adjustment.thresholdRampFactor(25.0, 16.0), 1e-9);
    }

    @Test
    void testRampNotAppliedWithoutThreshold() {
        // min_total_luck を持たないカテゴリ（common等）はランプの影響を受けない
        assertEquals(1.0, adjustment.thresholdRampFactor(25.0, 0.0), 1e-9);
    }

    @Test
    void testRampDisabled() {
        LuckAdjustment disabled = new LuckAdjustment(3.0, 0.1, 0.5, 0.05, false, 8.0, 0.05);
        assertEquals(1.0, disabled.thresholdRampFactor(16.0, 16.0), 1e-9);
    }

    @Test
    void testQualityZeroCategoryIsRamped() {
        // godバンド（quality 0）にもランプが適用される
        double atUnlock = adjustment.calculateAdjustedChance(0.05, 0.0, 16.0, 16.0);
        double atMax = adjustment.calculateAdjustedChance(0.05, 0.0, 25.0, 16.0);
        assertEquals(0.05 * 0.05, atUnlock, 1e-9);
        assertEquals(0.05, atMax, 1e-9);
        assertTrue(atUnlock < atMax, "Luck should increase the weight along the ramp");
    }

    @Test
    void testQualityMultiplierStillApplied() {
        // しきい値なしカテゴリは従来どおり品質倍率のみ
        double expectedMultiplier = 1.0 + Math.log1p(10.0 * 0.1) * Math.log1p(5.0 * 0.5);
        assertEquals(30.0 * expectedMultiplier, adjustment.calculateAdjustedChance(30.0, 5.0, 10.0, 0.0), 1e-9);
    }

    @Test
    void testQualityMultiplierCombinedWithRamp() {
        // immortal相当: quality 1, min_total_luck 10 → 解禁直後は倍率×min_factor
        double multiplier = 1.0 + Math.log1p(10.0 * 0.1) * Math.log1p(0.5);
        assertEquals(1.0 * multiplier * 0.05, adjustment.calculateAdjustedChance(1.0, 1.0, 10.0, 10.0), 1e-9);
    }

    @Test
    void testNegativeLuckUnchanged() {
        // 負の幸運のペナルティは従来どおり
        assertEquals(0.5, adjustment.calculateAdjustedChance(1.0, 1.0, -10.0, 0.0), 1e-9);
        assertEquals(0.0, adjustment.calculateAdjustedChance(1.0, 1.0, -25.0, 0.0), 1e-9);
    }

    @Test
    void testZeroLuckReturnsBaseChance() {
        assertEquals(30.0, adjustment.calculateAdjustedChance(30.0, 5.0, 0.0, 0.0), 1e-9);
    }
}
