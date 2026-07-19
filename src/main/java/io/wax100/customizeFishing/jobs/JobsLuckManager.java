package io.wax100.customizeFishing.jobs;

import io.wax100.customizeFishing.CustomizeFishing;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Jobs (Jobs Reborn) 連携: 指定職業に就いているプレイヤーを釣り抽選時のみ
 * 職業レベルに応じた幸運ポーションLv相当として扱う（エフェクトは付与しない）
 * <p>
 * Jobs API へのアクセスは {@link JobsRebornLevelProvider} に隔離されており、
 * Jobs 未導入サーバーでは一切ロードされない（常に幸運Lv0として動作する）
 */
public class JobsLuckManager {

    private final CustomizeFishing plugin;
    private final JobsLevelProvider levelProvider;

    public JobsLuckManager(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.levelProvider = createProvider(plugin);
    }

    private static JobsLevelProvider createProvider(CustomizeFishing plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Jobs") == null) {
            if (plugin.getConfig().getBoolean("jobs_luck.enabled", false)) {
                plugin.getLogger().info("Jobs プラグインが見つからないため jobs_luck 連携を無効化します。");
            }
            return null;
        }
        try {
            return new JobsRebornLevelProvider();
        } catch (Throwable t) {
            plugin.getLogger().warning("Jobs 連携の初期化に失敗したため jobs_luck を無効化します: " + t);
            return null;
        }
    }

    /**
     * 対象職業の職業レベルに応じた幸運レベルを返す
     * 幸運Lv = 職業レベル ÷ levels_per_luck（切り捨て、複数職業は最高レベルを使用）
     *
     * @param player プレイヤー
     * @return 幸運ポーション相当レベル（対象外・Jobs未導入・無効時は0）
     */
    public int getJobsLuckLevel(Player player) {
        if (levelProvider == null || !plugin.getConfig().getBoolean("jobs_luck.enabled", false)) {
            return 0;
        }
        Set<String> targetJobs = new HashSet<>();
        for (String name : plugin.getConfig().getStringList("jobs_luck.jobs")) {
            targetJobs.add(name.toLowerCase(Locale.ROOT));
        }
        if (targetJobs.isEmpty()) {
            return 0;
        }
        int levelsPerLuck = Math.max(1, plugin.getConfig().getInt("jobs_luck.levels_per_luck", 10));
        try {
            return levelProvider.getHighestLevel(player, targetJobs) / levelsPerLuck;
        } catch (Throwable t) {
            // Jobs のバージョン差異等でAPIが変わっても釣り処理自体は止めない
            plugin.getLogger().warning("Jobs 連携でエラーが発生しました: " + t);
            return 0;
        }
    }
}
