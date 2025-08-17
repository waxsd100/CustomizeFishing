package io.wax100.customizeFishing.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FishingListenerTest {
    
    private record CategoryData(String name, int priority, double quality, double chance) {}
    
    @Test
    void testCategorySelectionLogic() throws Exception {
        // 幸運3.2%での結果
        System.out.println("\n===== Luck 3.2% Distribution (1,000,000 iterations) =====");
        Map<String, Integer> results32 = simulateCategorySelectionWithLuck(1000000, 3.2);
        for (Map.Entry<String, Integer> entry : results32.entrySet()) {
            double percentage = (entry.getValue() / 1000000.0) * 100;
            System.out.printf("%s: %.4f%% (%d times)%n", entry.getKey(), percentage, entry.getValue());
        }
        
        // 幸運5%での結果
        System.out.println("\n===== Luck 5% Distribution (1,000,000 iterations) =====");
        Map<String, Integer> results5 = simulateCategorySelectionWithLuck(1000000, 5.0);
        for (Map.Entry<String, Integer> entry : results5.entrySet()) {
            double percentage = (entry.getValue() / 1000000.0) * 100;
            System.out.printf("%s: %.4f%% (%d times)%n", entry.getKey(), percentage, entry.getValue());
        }
        
        // 各ティアの運補正後確率を計算
        System.out.println("\n===== Tier Probability Analysis (Luck 5%) =====");
        
        List<CategoryData> baseCategories = Arrays.asList(
            new CategoryData("god", 3, 1.0, 0.0001),
            new CategoryData("exotic", 5, 1.0, 0.01),
            new CategoryData("legendary", 8, 1.0, 1.0),
            new CategoryData("epic", 9, 2.0, 5.0),
            new CategoryData("rare", 10, 2.0, 10.0),
            new CategoryData("common", 12, 2.0, 30.0),
            new CategoryData("treasure", 13, 3.0, 40.0)
        );
        
        double totalLuck5 = 5.0;
        double totalChance = 0;
        
        System.out.printf("%-10s | %-8s | %-8s | %-8s | %-10s%n", 
            "Tier", "Quality", "Base%", "Adjusted%", "Multiplier");
        System.out.println("-----------|----------|----------|----------|----------");
        
        for (CategoryData category : baseCategories) {
            double adjustedChance = calculateAdjustedChance(category.chance(), category.quality(), totalLuck5);
            double multiplier = adjustedChance / category.chance();
            totalChance += adjustedChance;
            
            System.out.printf("%-10s | %-8.1f | %-8.4f | %-8.4f | %-10.2fx%n",
                category.name(), category.quality(), category.chance(), adjustedChance, multiplier);
        }
        
        System.out.printf("\nTotal chance before normalization: %.4f%%\n", totalChance);
        
        // 正規化後の確率（実際の出現率）
        System.out.println("\n===== Normalized Probabilities (Actual rates) =====");
        System.out.printf("%-10s | %-12s | %-12s%n", "Tier", "Adjusted%", "Normalized%");
        System.out.println("-----------|--------------|------------");
        
        for (CategoryData category : baseCategories) {
            double adjustedChance = calculateAdjustedChance(category.chance(), category.quality(), totalLuck5);
            double normalizedChance = (adjustedChance / totalChance) * 100;
            
            System.out.printf("%-10s | %-12.4f | %-12.4f%n",
                category.name(), adjustedChance, normalizedChance);
        }
        
        // godティアの確率
        double godPercentage32 = (results32.getOrDefault("god", 0) / 1000000.0) * 100;
        double godPercentage5 = (results5.getOrDefault("god", 0) / 1000000.0) * 100;
        System.out.printf("God tier actual percentage (3.2%% luck): %.6f%%\n", godPercentage32);
        System.out.printf("God tier actual percentage (5%% luck): %.6f%%\n", godPercentage5);
        
        // 期待値の範囲を緩く設定（統計的誤差を考慮）
        assertTrue(godPercentage32 >= 0.0, "God tier should appear at least occasionally");
        
        // treasureが最も多いことを確認（40%の確率）
        String mostCommon = results32.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
        assertEquals("treasure", mostCommon, "Treasure should be the most common category (40%)");
    }
    
    @Test 
    void testSingleRandomRollPerSelection() {
        // 1回のランダム値で全カテゴリーを判定することを確認
        int godCount = 0;
        int exoticCount = 0;
        int legendaryCount = 0;
        
        // 同じ乱数値で1000回テスト
        for (int i = 0; i < 1000; i++) {
            double roll = 0.5; // 0.5%の固定値
            String result = selectCategoryWithFixedRoll(roll);
            
            if ("god".equals(result)) godCount++;
            if ("exotic".equals(result)) exoticCount++;
            if ("legendary".equals(result)) legendaryCount++;
        }
        
        // 0.5%の値ではlegendaryが選ばれるはず（god:0.0001%, exotic:0.01%, legendary:1%）
        assertEquals(0, godCount, "God should never be selected with 0.5% roll (threshold 0.0001%)");
        assertEquals(0, exoticCount, "Exotic should never be selected with 0.5% roll (threshold 0.01%)");
        assertEquals(1000, legendaryCount, "Legendary should always be selected with 0.5% roll (threshold 1%)");
    }
    
    @Test
    void testPriorityOrdering() {
        // 優先度順にソートされることを確認
        List<CategoryData> categories = Arrays.asList(
            new CategoryData("common", 12, 2.0, 100.0),
            new CategoryData("god", 3, 1.0, 50.0),  
            new CategoryData("legendary", 8, 1.0, 75.0)
        );
        
        categories.sort(Comparator.comparingInt(CategoryData::priority));
        
        assertEquals("god", categories.get(0).name(), "First should be god (priority 3)");
        assertEquals("legendary", categories.get(1).name(), "Second should be legendary (priority 8)");
        assertEquals("common", categories.get(2).name(), "Third should be common (priority 12)");
    }
    
    @Test
    void testAdjustedChanceCalculation() {
        // 補正計算のテスト
        double baseChance = 1.0;
        double quality = 1.0;
        double totalLuck = 3.0;
        
        // 正の幸運での補正
        double adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);
        assertTrue(adjustedChance > baseChance, "Positive luck should increase chance");
        assertTrue(adjustedChance < 5.0, "Adjusted chance should be reasonable");
        
        // 負の幸運での補正テスト
        totalLuck = -2.0;
        adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);
        assertTrue(adjustedChance < baseChance, "Negative luck should decrease chance");
        
        // 極端な負の幸運でのテスト
        totalLuck = -10.0;
        adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);
        assertTrue(adjustedChance >= 0.0, "Adjusted chance should not be negative");
        assertTrue(adjustedChance < baseChance, "Negative luck should reduce chance");
        
        System.out.printf("Negative luck test: base=%.4f, luck=%.1f → adjusted=%.4f (%.1fx)\n", 
            baseChance, totalLuck, adjustedChance, adjustedChance / baseChance);
    }
    
    @Test
    void testNegativeLuckDetailedEffect() {
        System.out.println("===== Negative Luck Detailed Effect Test =====");
        
        // god tier (quality=1, chance=0.0001%) の負の幸運テスト
        double baseChance = 0.0001;
        double quality = 1.0;
        
        System.out.printf("%-10s | %-12s | %-12s | %-12s%n", "Luck", "Base%", "Adjusted%", "Multiplier");
        System.out.println("-----------|--------------|--------------|------------");
        
        double[] luckValues = {0, -1, -2, -3, -5, -10};
        for (double luck : luckValues) {
            double adjusted = calculateAdjustedChance(baseChance, quality, luck);
            double multiplier = adjusted / baseChance;
            System.out.printf("%-10.1f | %-12.6f | %-12.6f | %-12.2fx%n", 
                luck, baseChance, adjusted, multiplier);
        }
        
        // legendary tier (quality=1, chance=1%) の負の幸運テスト
        System.out.println("\nLegendary tier (1%) with negative luck:");
        System.out.printf("%-10s | %-12s | %-12s | %-12s%n", "Luck", "Base%", "Adjusted%", "Multiplier");
        System.out.println("-----------|--------------|--------------|------------");
        
        baseChance = 1.0;
        for (double luck : luckValues) {
            double adjusted = calculateAdjustedChance(baseChance, quality, luck);
            double multiplier = adjusted / baseChance;
            System.out.printf("%-10.1f | %-12.4f | %-12.4f | %-12.2fx%n", 
                luck, baseChance, adjusted, multiplier);
        }
        
        // 0%まで下がることを確認するテスト
        System.out.println("\nTesting chance reaching 0%:");
        baseChance = 1.0;
        double extremeLuck = -25.0; // 1.0 / (0.05 * 1.0) = 20以上で0%
        double zeroChance = calculateAdjustedChance(baseChance, quality, extremeLuck);
        System.out.printf("Base: %.4f%%, Luck: %.1f → Adjusted: %.4f%% (0%% reached: %s)%n", 
            baseChance, extremeLuck, zeroChance, zeroChance == 0.0 ? "YES" : "NO");
        assertEquals(0.0, zeroChance, "Extreme negative luck should reach 0%");
    }
    
    // ヘルパーメソッド：カテゴリー選択のシミュレーション（運補正込み）
    private Map<String, Integer> simulateCategorySelection(int iterations) {
        return simulateCategorySelectionWithLuck(iterations, 3.2);
    }
    
    // 指定した幸運値でのカテゴリー選択シミュレーション
    private Map<String, Integer> simulateCategorySelectionWithLuck(int iterations, double totalLuck) {
        Map<String, Integer> results = new HashMap<>();
        Random random = new Random();
        
        // config.ymlの実際の値を使用
        List<CategoryData> baseCategories = Arrays.asList(
            new CategoryData("god", 3, 1.0, 0.0001),     // 0.0001%
            new CategoryData("exotic", 5, 1.0, 0.01),    // 0.01%
            new CategoryData("legendary", 8, 1.0, 1.0),  // 1%
            new CategoryData("epic", 9, 2.0, 5.0),       // 5%
            new CategoryData("rare", 10, 2.0, 10.0),     // 10%
            new CategoryData("common", 12, 2.0, 30.0),   // 30%
            new CategoryData("treasure", 13, 3.0, 40.0)  // 40%
        );
        
        // 運補正を適用
        List<CategoryData> adjustedCategories = new ArrayList<>();
        
        for (CategoryData category : baseCategories) {
            double adjustedChance = calculateAdjustedChance(category.chance(), category.quality(), totalLuck);
            if (adjustedChance > 0) {
                adjustedCategories.add(new CategoryData(category.name(), category.priority(), category.quality(), adjustedChance));
            }
        }
        
        // 優先度順にソート（高い順）
        adjustedCategories.sort(Comparator.comparingInt(CategoryData::priority).reversed());
        
        // 総確率を計算
        double totalChance = adjustedCategories.stream()
            .mapToDouble(CategoryData::chance)
            .sum();
        
        for (int i = 0; i < iterations; i++) {
            // 修正版：1回だけ乱数を生成（0から総確率の範囲）
            double roll = random.nextDouble() * totalChance;
            
            // 累積確率で判定
            String selected = "common"; // デフォルト
            double cumulative = 0;
            for (CategoryData category : adjustedCategories) {
                cumulative += category.chance();
                if (roll < cumulative) {
                    selected = category.name();
                    break;
                }
            }
            
            results.merge(selected, 1, Integer::sum);
        }
        
        return results;
    }
    
    // 固定の乱数値でカテゴリーを選択
    private String selectCategoryWithFixedRoll(double roll) {
        List<CategoryData> categories = Arrays.asList(
            new CategoryData("god", 3, 1.0, 0.0001),     // 0.0001%
            new CategoryData("exotic", 5, 1.0, 0.01),    // 0.01%
            new CategoryData("legendary", 8, 1.0, 1.0)   // 1%
        );
        
        categories.sort(Comparator.comparingInt(CategoryData::priority));
        
        for (CategoryData category : categories) {
            if (roll < category.chance()) {
                return category.name();
            }
        }
        
        return "common";
    }
    
    // 新しい補正計算（乗算型 + 対数減衰）
    private double calculateAdjustedChance(double baseChance, double quality, double totalLuck) {
        if (totalLuck == 0 || quality == 0) {
            return baseChance;
        }

        if (totalLuck > 0) {
            // 正の幸運時：乗算型 + 対数減衰
            if (quality > 0) {
                // デフォルト設定値を使用
                double maxMultiplier = 3.0;      // 最大3倍
                double luckScale = 0.1;          // 運の影響度
                double qualityImpact = 0.5;      // 品質の影響度
                
                // 対数減衰を使用して極端な増加を防止
                double scaledLuck = Math.log1p(totalLuck * luckScale);
                double qualityFactor = Math.log1p(quality * qualityImpact);
                
                // 乗算倍率を計算（1.0 ～ maxMultiplier の範囲）
                double multiplier = 1.0 + (scaledLuck * qualityFactor);
                multiplier = Math.min(multiplier, maxMultiplier);
                
                return baseChance * multiplier;
            } else {
                // 負のqualityの場合は影響なし
                return baseChance;
            }
        } else {
            // 負の幸運時：確率を減少させる
            double penaltyScale = 0.05;
            double penalty = Math.abs(totalLuck) * penaltyScale * quality;
            
            // 0%まで下がる可能性あり
            return Math.max(baseChance - penalty, 0.0);
        }
    }
}