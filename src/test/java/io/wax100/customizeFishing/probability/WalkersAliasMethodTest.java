package io.wax100.customizeFishing.probability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WalkersAliasMethodTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(42); // 固定シードで再現性を確保
    }

    @Test
    @DisplayName("単一要素のテスト")
    void testSingleElement() {
        List<String> items = Arrays.asList("A");
        List<Double> weights = Arrays.asList(1.0);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, random);

        assertEquals("A", alias.sample());
        assertEquals(1, alias.size());
        assertEquals(1.0, alias.getTotalWeight(), 0.001);
    }

    @Test
    @DisplayName("等確率の要素のテスト")
    void testEqualProbabilities() {
        List<String> items = Arrays.asList("A", "B", "C");
        List<Double> weights = Arrays.asList(1.0, 1.0, 1.0);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, new Random());

        Map<String, Integer> counts = new HashMap<>();
        int samples = 30000;

        for (int i = 0; i < samples; i++) {
            String result = alias.sample();
            counts.put(result, counts.getOrDefault(result, 0) + 1);
        }

        // 各要素が約33.3%の確率で選ばれることを確認（誤差5%以内）
        for (String item : items) {
            double ratio = (double) counts.get(item) / samples;
            assertEquals(0.333, ratio, 0.05,
                    "Item " + item + " should be selected approximately 33.3% of the time");
        }
    }

    @Test
    @DisplayName("異なる重みのテスト")
    void testDifferentWeights() {
        List<String> items = Arrays.asList("Common", "Rare", "Legendary");
        List<Double> weights = Arrays.asList(70.0, 25.0, 5.0);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, new Random());

        Map<String, Integer> counts = new HashMap<>();
        int samples = 50000;

        for (int i = 0; i < samples; i++) {
            String result = alias.sample();
            counts.put(result, counts.getOrDefault(result, 0) + 1);
        }

        // 期待される確率に近いことを確認（誤差5%以内）
        assertEquals(0.70, (double) counts.get("Common") / samples, 0.05);
        assertEquals(0.25, (double) counts.get("Rare") / samples, 0.05);
        assertEquals(0.05, (double) counts.get("Legendary") / samples, 0.05);
    }

    @Test
    @DisplayName("ゼロ重みを含むテスト")
    void testWithZeroWeights() {
        List<String> items = Arrays.asList("A", "B", "C");
        List<Double> weights = Arrays.asList(1.0, 0.0, 2.0);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, new Random());

        Map<String, Integer> counts = new HashMap<>();
        int samples = 30000;

        for (int i = 0; i < samples; i++) {
            String result = alias.sample();
            counts.put(result, counts.getOrDefault(result, 0) + 1);
        }

        // Bは選ばれないことを確認
        assertEquals(0, counts.getOrDefault("B", 0));

        // AとCの比率が1:2であることを確認
        double ratioA = (double) counts.get("A") / samples;
        double ratioC = (double) counts.get("C") / samples;
        assertEquals(0.333, ratioA, 0.05);
        assertEquals(0.667, ratioC, 0.05);
    }

    @Test
    @DisplayName("極端な重みの差のテスト")
    void testExtremeWeights() {
        List<String> items = Arrays.asList("VeryRare", "VeryCommon");
        List<Double> weights = Arrays.asList(0.01, 99.99);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, new Random());

        Map<String, Integer> counts = new HashMap<>();
        int samples = 100000;

        for (int i = 0; i < samples; i++) {
            String result = alias.sample();
            counts.put(result, counts.getOrDefault(result, 0) + 1);
        }

        // VeryRareは約0.01%、VeryCommonは約99.99%で選ばれることを確認
        assertEquals(0.0001, (double) counts.get("VeryRare") / samples, 0.0005);
        assertEquals(0.9999, (double) counts.get("VeryCommon") / samples, 0.0005);
    }

    @Test
    @DisplayName("複数サンプルのテスト")
    void testSampleMultiple() {
        List<String> items = Arrays.asList("A", "B");
        List<Double> weights = Arrays.asList(1.0, 1.0);

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, random);

        List<String> samples = alias.sampleMultiple(10);

        assertEquals(10, samples.size());
        for (String sample : samples) {
            assertTrue(items.contains(sample));
        }
    }

    @Test
    @DisplayName("不正な入力のテスト - サイズ不一致")
    void testInvalidInputSizeMismatch() {
        List<String> items = Arrays.asList("A", "B");
        List<Double> weights = Arrays.asList(1.0);

        assertThrows(IllegalArgumentException.class,
                () -> new WalkersAliasMethod<>(items, weights, random),
                "Should throw exception when items and weights have different sizes");
    }

    @Test
    @DisplayName("不正な入力のテスト - 空のリスト")
    void testInvalidInputEmptyList() {
        List<String> items = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        assertThrows(IllegalArgumentException.class,
                () -> new WalkersAliasMethod<>(items, weights, random),
                "Should throw exception for empty lists");
    }

    @Test
    @DisplayName("不正な入力のテスト - 負の重み")
    void testInvalidInputNegativeWeight() {
        List<String> items = Arrays.asList("A", "B");
        List<Double> weights = Arrays.asList(1.0, -1.0);

        assertThrows(IllegalArgumentException.class,
                () -> new WalkersAliasMethod<>(items, weights, random),
                "Should throw exception for negative weights");
    }

    @Test
    @DisplayName("不正な入力のテスト - 全てゼロの重み")
    void testInvalidInputAllZeroWeights() {
        List<String> items = Arrays.asList("A", "B");
        List<Double> weights = Arrays.asList(0.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> new WalkersAliasMethod<>(items, weights, random),
                "Should throw exception when all weights are zero");
    }

    @RepeatedTest(5)
    @DisplayName("パフォーマンステスト - O(1)選択の確認")
    void testPerformance() {
        // 大量の要素でテスト
        List<String> items = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            items.add("Item" + i);
            weights.add(Math.random() * 100);
        }

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(items, weights, new Random());

        // サンプリング時間を測定
        long startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            alias.sample();
        }
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000; // ミリ秒に変換

        // 100,000回のサンプリングが1秒以内に完了することを確認
        assertTrue(duration < 1000,
                "100,000 samples should complete within 1 second, but took " + duration + "ms");
    }

    @Test
    @DisplayName("釣りカテゴリのシミュレーション")
    void testFishingCategories() {
        // 実際の釣りカテゴリをシミュレート
        List<String> categories = Arrays.asList(
                "common", "uncommon", "rare", "epic", "legendary", "god"
        );
        List<Double> weights = Arrays.asList(
                50.0, 25.0, 15.0, 7.0, 2.5, 0.5
        );

        WalkersAliasMethod<String> alias = new WalkersAliasMethod<>(categories, weights, new Random());

        Map<String, Integer> counts = new HashMap<>();
        int samples = 100000;

        for (int i = 0; i < samples; i++) {
            String result = alias.sample();
            counts.put(result, counts.getOrDefault(result, 0) + 1);
        }

        // 実際の確率分布を確認
        System.out.println("Fishing Category Distribution:");
        for (String category : categories) {
            double percentage = (double) counts.getOrDefault(category, 0) / samples * 100;
            System.out.printf("  %s: %.2f%% (expected: %.2f%%)\n",
                    category, percentage,
                    weights.get(categories.indexOf(category)));
        }

        // 期待値との誤差が妥当な範囲内であることを確認
        assertEquals(0.50, (double) counts.get("common") / samples, 0.02);
        assertEquals(0.25, (double) counts.get("uncommon") / samples, 0.02);
        assertEquals(0.15, (double) counts.get("rare") / samples, 0.02);
        assertEquals(0.07, (double) counts.get("epic") / samples, 0.01);
        assertEquals(0.025, (double) counts.get("legendary") / samples, 0.01);
        assertEquals(0.005, (double) counts.get("god") / samples, 0.005);
    }
}