package io.wax100.customizeFishing.fishing;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TimingHandler {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    private final Map<Player, Long> biteTimestamps = new ConcurrentHashMap<>();

    public TimingHandler(CustomizeFishing plugin, DebugLogger debugLogger) {
        this.plugin = plugin;
        this.debugLogger = debugLogger;
    }

    /**
     * プレイヤーのバイトタイムスタンプを記録
     *
     * @param player プレイヤー
     */
    public void recordBiteTimestamp(Player player) {
        biteTimestamps.put(player, System.currentTimeMillis());
    }

    /**
     * タイミング結果を計算
     *
     * @param player プレイヤー
     * @return タイミング結果
     */
    public TimingResult calculateTimingResult(Player player) {
        Long biteTime = biteTimestamps.remove(player);
        if (biteTime == null) {
            return TimingResult.miss();
        }
        long reactionTime = System.currentTimeMillis() - biteTime;

        LuckCalculator luckCalc = new LuckCalculator(plugin, debugLogger);
        return luckCalc.calculateTimingResult(reactionTime);
    }

    /**
     * タイミング情報をウキの位置にテキストディスプレイとして表示
     *
     * @param hookLocation ウキの位置
     * @param timingResult タイミング結果
     */
    public void displayTimingAtHook(Location hookLocation, TimingResult timingResult) {
        if (!timingResult.hasTiming()) {
            return;
        }

        String timingText = formatTimingText(timingResult);

        TextDisplay textDisplay = Objects.requireNonNull(hookLocation.getWorld()).spawn(
                hookLocation.clone().add(0, 1, 0),
                TextDisplay.class
        );

        textDisplay.setText(timingText);
        textDisplay.setBillboard(TextDisplay.Billboard.CENTER);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!textDisplay.isDead()) {
                textDisplay.remove();
            }
        }, 60L);
    }

    /**
     * タイミング結果を表示用テキストにフォーマット
     *
     * @param timingResult タイミング結果
     * @return フォーマット済みテキスト
     */
    private String formatTimingText(TimingResult timingResult) {
        String tierName = switch (timingResult.tier().name().toLowerCase()) {
            case "just" -> "§6§lJUST!";
            case "perfect" -> "§d§lPERFECT!";
            case "great" -> "§a§lGREAT!";
            case "good" -> "§e§lGOOD!";
            default -> "§7" + timingResult.tier().name().toUpperCase();
        };

        return tierName + "\n§f" + timingResult.reactionTimeMs() + "ms";
    }
}