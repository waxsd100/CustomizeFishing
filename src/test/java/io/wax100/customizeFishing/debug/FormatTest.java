package io.wax100.customizeFishing.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {
    
    @Test
    void testProbabilityFormatting() {
        System.out.println("===== Log Formatting (Decimal) =====");
        
        // テストケース
        double[] testValues = {
            0.0,        // 0%
            0.0001,     // god tier
            0.0023,     // 例の値
            0.005,      // 0.5%未満
            0.01,       // 1%未満
            0.125,      // 0.125%
            1.5,        // 1.5%
            5.7,        // 5.7%
            25.0,       // 25%
            100.0       // 100%
        };
        
        System.out.println("Log format (小数点表記):");
        for (double value : testValues) {
            String logFormatted = DebugLogger.formatProbabilityForLog(value);
            System.out.printf("%.6f%% → %s%n", value, logFormatted);
        }
        
        System.out.println("\nDisplay format (科学的記法):");
        for (double value : testValues) {
            String displayFormatted = DebugLogger.formatProbabilityForDisplay(value);
            System.out.printf("%.6f%% → %s%n", value, displayFormatted);
        }
        
        // ログ用フォーマットのテスト
        String godLogResult = DebugLogger.formatProbabilityForLog(0.0001);
        assertEquals("0.000100%", godLogResult, "God tier log should show 6 decimal places");
        
        String smallLogResult = DebugLogger.formatProbabilityForLog(0.0023);
        assertEquals("0.0023%", smallLogResult, "Small values log should show 4 decimal places");
        
        // 画面表示用フォーマットのテスト
        String godDisplayResult = DebugLogger.formatProbabilityForDisplay(0.0001);
        assertEquals("1.0×10⁻⁴%", godDisplayResult, "God tier display should show scientific notation");
        
        String smallDisplayResult = DebugLogger.formatProbabilityForDisplay(0.0023);
        assertEquals("2.3×10⁻³%", smallDisplayResult, "Small values display should show scientific notation");
        
        // 共通テスト（高い確率）
        assertEquals("1.5%", DebugLogger.formatProbabilityForLog(1.5), "Medium values log should show with 1 decimal");
        assertEquals("1.5%", DebugLogger.formatProbabilityForDisplay(1.5), "Medium values display should show with 1 decimal");
        
        assertEquals("25%", DebugLogger.formatProbabilityForLog(25.0), "Large values log should show as integer");
        assertEquals("25%", DebugLogger.formatProbabilityForDisplay(25.0), "Large values display should show as integer");
        
        // 境界値テスト
        assertEquals("0.010%", DebugLogger.formatProbabilityForLog(0.01), "0.01% log should show 3 decimal places");
        assertEquals("0.01%", DebugLogger.formatProbabilityForDisplay(0.01), "0.01% display should show 2 decimal places");
        
        // より多くの科学的記法テスト
        assertEquals("5.0×10⁻³%", DebugLogger.formatProbabilityForDisplay(0.005), "0.005% display should show scientific notation");
        assertEquals("9.9×10⁻³%", DebugLogger.formatProbabilityForDisplay(0.0099), "0.0099% display should show scientific notation");
    }
}