package io.wax100.customizeFishing.probability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Walker's Alias Method の実装
 * O(1) の時間計算量で重み付きランダム選択を行うアルゴリズム
 *
 * @param <T> 選択される要素の型
 */
public class WalkersAliasMethod<T> {

    private final List<T> items;
    private final double[] probability;
    private final int[] alias;
    private final Random random;
    private final double totalWeight;

    /**
     * Walker's Alias Method のテーブルを構築
     *
     * @param items   選択対象のアイテムリスト
     * @param weights 各アイテムの重み（確率）
     * @param random  乱数生成器
     */
    public WalkersAliasMethod(List<T> items, List<Double> weights, Random random) {
        if (items.size() != weights.size()) {
            throw new IllegalArgumentException("Items and weights must have the same size");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be empty");
        }

        this.items = new ArrayList<>(items);
        this.random = random;
        int n = items.size();

        this.probability = new double[n];
        this.alias = new int[n];

        // 重みの合計を計算
        double sum = 0;
        for (double weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("Weights must be non-negative");
            }
            sum += weight;
        }

        if (sum <= 0) {
            throw new IllegalArgumentException("Total weight must be positive");
        }

        this.totalWeight = sum;

        // 正規化された確率を計算（合計がnになるように）
        double[] normalizedProb = new double[n];
        for (int i = 0; i < n; i++) {
            normalizedProb[i] = weights.get(i) * n / sum;
        }

        // Vose's version of Alias Method のテーブル構築
        Deque<Integer> small = new ArrayDeque<>();
        Deque<Integer> large = new ArrayDeque<>();

        // 確率が1より小さいインデックスをsmallに、大きいインデックスをlargeに分類
        for (int i = 0; i < n; i++) {
            if (normalizedProb[i] < 1.0) {
                small.add(i);
            } else {
                large.add(i);
            }
        }

        // Alias テーブルの構築
        while (!small.isEmpty() && !large.isEmpty()) {
            int smallIdx = small.removeFirst();
            int largeIdx = large.removeFirst();

            probability[smallIdx] = normalizedProb[smallIdx];
            alias[smallIdx] = largeIdx;

            // largeIdx の確率を調整
            normalizedProb[largeIdx] = normalizedProb[largeIdx] + normalizedProb[smallIdx] - 1.0;

            if (normalizedProb[largeIdx] < 1.0) {
                small.add(largeIdx);
            } else {
                large.add(largeIdx);
            }
        }

        // 残りの要素を処理（浮動小数点誤差により残る可能性がある）
        while (!small.isEmpty()) {
            probability[small.removeFirst()] = 1.0;
        }
        while (!large.isEmpty()) {
            probability[large.removeFirst()] = 1.0;
        }
    }

    /**
     * O(1) でランダムにアイテムを選択
     *
     * @return 選択されたアイテム
     */
    public T sample() {
        // 0からn-1までのランダムな整数を選択
        int i = random.nextInt(items.size());

        // 0から1までのランダムな実数を生成
        double r = random.nextDouble();

        // probability[i] と比較して、元のインデックスかaliasを選択
        if (r < probability[i]) {
            return items.get(i);
        } else {
            return items.get(alias[i]);
        }
    }

    /**
     * 複数のサンプルを生成
     *
     * @param count 生成するサンプル数
     * @return 選択されたアイテムのリスト
     */
    public List<T> sampleMultiple(int count) {
        List<T> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(sample());
        }
        return results;
    }

    /**
     * 総重みを取得
     *
     * @return 全アイテムの重みの合計
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * アイテム数を取得
     *
     * @return アイテムの数
     */
    public int size() {
        return items.size();
    }

    /**
     * デバッグ情報を取得
     *
     * @return デバッグ用の文字列
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Walker's Alias Method Table:\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append(String.format("  [%d] %s: prob=%.4f, alias=%d\n",
                    i, items.get(i), probability[i], alias[i]));
        }
        sb.append(String.format("Total Weight: %.4f\n", totalWeight));
        return sb.toString();
    }
}