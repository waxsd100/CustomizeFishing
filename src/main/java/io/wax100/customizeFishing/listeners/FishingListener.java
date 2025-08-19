package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.fishing.CategorySelector;
import io.wax100.customizeFishing.fishing.DoubleFishingHandler;
import io.wax100.customizeFishing.fishing.FishingConditionChecker;
import io.wax100.customizeFishing.fishing.FishingProcessor;
import io.wax100.customizeFishing.fishing.ProbabilityCalculator;
import io.wax100.customizeFishing.fishing.TimingHandler;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Objects;

public class FishingListener implements Listener {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    private final CatchEffects catchEffects;
    private final CategorySelector categorySelector;
    private final ProbabilityCalculator probabilityCalculator;
    private final FishingProcessor fishingProcessor;
    private final DoubleFishingHandler doubleFishingHandler;
    private final TimingHandler timingHandler;

    public FishingListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.debugLogger = new DebugLogger(plugin);
        this.catchEffects = new CatchEffects(plugin);

        BindingCurseManager bindingCurseManager = new BindingCurseManager(plugin);

        this.categorySelector = new CategorySelector(plugin, debugLogger);
        this.probabilityCalculator = new ProbabilityCalculator(plugin);
        this.fishingProcessor = new FishingProcessor(plugin, debugLogger, bindingCurseManager, categorySelector, probabilityCalculator);
        this.doubleFishingHandler = new DoubleFishingHandler(plugin, fishingProcessor, categorySelector, catchEffects);
        this.timingHandler = new TimingHandler(plugin, debugLogger);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean("enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        PlayerFishEvent.State state = event.getState();
        Location hookLocation = event.getHook().getLocation();

        if (state == PlayerFishEvent.State.FISHING) {
            // 釣りを開始した時点でログ開始
            debugLogger.logFishingStart(player);
            return;
        }

        if (state == PlayerFishEvent.State.BITE) {
            Objects.requireNonNull(hookLocation.getWorld()).playSound(hookLocation, Sound.BLOCK_LEVER_CLICK, 1.0f, 2.0f);
            timingHandler.recordBiteTimestamp(player);
            return;
        }

        if (state != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        if (!(event.getCaught() instanceof Item itemEntity)) {
            return;
        }

        TimingResult timingResult = timingHandler.calculateTimingResult(player);

        // 幸運値を事前に計算（ダブルフィッシングでも1回だけ計算）
        boolean isOpenWater = FishingConditionChecker.isOpenWater(hookLocation);
        Weather weather = Weather.CLEAR;
        if (Objects.requireNonNull(hookLocation.getWorld()).isThundering()) {
            weather = Weather.THUNDER;
        } else if (hookLocation.getWorld().hasStorm()) {
            weather = Weather.RAIN;
        }

        // Twilight Forest rainy_cloud の検出（ウキの上空32ブロック以内）
        if (checkForTwilightRainyClouds(hookLocation)) {
            weather = Weather.RAIN;
        }

        LuckCalculator luckCalc = new LuckCalculator(plugin, debugLogger);
        LuckResult luckResult = luckCalc.calculateTotalLuck(player, weather, timingResult);

        boolean canDoubleFish = doubleFishingHandler.canDoubleFish(player);

        if (canDoubleFish && plugin.getConfig().getBoolean("double_fishing.enabled", true)) {
            doubleFishingHandler.handleDoubleFishing(player, itemEntity, hookLocation, timingResult, luckResult, isOpenWater, weather);
        } else {
            FishingProcessor.FishingResult result = fishingProcessor.processFishing(player, itemEntity, hookLocation, false, timingResult, luckResult, isOpenWater, weather);
            catchEffects.playCatchEffects(player, result.category(), result.probabilityInfo());
        }

        timingHandler.displayTimingAtHook(hookLocation, timingResult);
        debugLogger.logFishingEnd(player);
    }


    /**
     * Twilight Forest の rainy_cloud ブロックが釣り針の上空32ブロック以内にあるかチェック
     *
     * @param hookLocation 釣り針の位置
     * @return rainy_cloudが見つかった場合はtrue
     */
    private boolean checkForTwilightRainyClouds(Location hookLocation) {
        // ウキの真上のみをチェック（Y軸+1〜+32ブロック）
        for (int y = 1; y <= 32; y++) {
            Location checkLocation = hookLocation.clone().add(0, y, 0);
            Block block = checkLocation.getBlock();

            // Twilight Forest の rainy_cloud ブロックかチェック
            Material blockType = block.getType();
            String blockKey = blockType.getKey().toString();

            if ("twilightforest:rainy_cloud".equals(blockKey)) {
                return true;
            }
        }
        return false;
    }


}