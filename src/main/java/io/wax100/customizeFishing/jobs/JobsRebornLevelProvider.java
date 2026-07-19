package io.wax100.customizeFishing.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Jobs (Jobs Reborn) API を使う実装
 * このクラスは Jobs プラグインが存在する場合のみロード・インスタンス化される
 */
public class JobsRebornLevelProvider implements JobsLevelProvider {

    @Override
    public int getHighestLevel(Player player, Set<String> targetJobs) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer == null) {
            return 0;
        }
        List<JobProgression> progressions = jobsPlayer.getJobProgression();
        if (progressions == null) {
            return 0;
        }
        int highest = 0;
        for (JobProgression progression : progressions) {
            if (targetJobs.contains(progression.getJob().getName().toLowerCase(Locale.ROOT))) {
                highest = Math.max(highest, progression.getLevel());
            }
        }
        return highest;
    }
}
