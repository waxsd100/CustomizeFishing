package io.wax100.customizeFishing.timing;

/**
 * タイミングティアの情報を保持するレコード
 */
public record TimingTier(
    String name,
    double maxTimeMs,
    double bonusMultiplier
) {
    
    /**
     * 反応時間がこのティアに該当するかチェック
     * @param reactionTime 反応時間（ミリ秒）
     * @return このティアに該当するかどうか
     */
    public boolean matches(long reactionTime) {
        return reactionTime <= maxTimeMs;
    }
    
    /**
     * ベース幸運ボーナスに倍率を適用して計算
     * @param baseLuckBonus ベース幸運ボーナス
     * @return 調整された幸運ボーナス
     */
    public double calculateLuckBonus(double baseLuckBonus) {
        return baseLuckBonus * bonusMultiplier;
    }
}