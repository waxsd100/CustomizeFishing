package io.wax100.customizeFishing.timing;

/**
 * タイミング判定結果を保持するレコード
 */
public record TimingResult(
        boolean hasTiming,
        TimingTier tier,
        long reactionTimeMs,
        double luckBonus
) {

    /**
     * タイミング失敗の場合のインスタンスを作成
     */
    public static TimingResult miss() {
        return new TimingResult(false, null, -1, 0);
    }

    /**
     * タイミング成功の場合のインスタンスを作成
     */
    public static TimingResult success(TimingTier tier, long reactionTimeMs, double luckBonus) {
        return new TimingResult(true, tier, reactionTimeMs, luckBonus);
    }
}