package io.wax100.customizeFishing.jobs;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * 職業レベルの取得を抽象化するインターフェース
 * Jobs プラグインのクラスへの依存を JobsRebornLevelProvider に隔離し、
 * Jobs 未導入環境でも本体クラスが安全にロードできるようにする
 */
public interface JobsLevelProvider {

    /**
     * 対象職業のうち最も高い職業レベルを返す
     *
     * @param player     プレイヤー
     * @param targetJobs 対象職業名（小文字）のセット
     * @return 最高職業レベル（未就職は0）
     */
    int getHighestLevel(Player player, Set<String> targetJobs);
}
