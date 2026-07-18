package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.enchant.EnchantLimiter;
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
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class FishingListener implements Listener {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    private final CatchEffects catchEffects;
    private final FishingProcessor fishingProcessor;
    private final DoubleFishingHandler doubleFishingHandler;
    private final TimingHandler timingHandler;
    private final EnchantLimiter enchantLimiter;
    private final LuckCalculator luckCalculator;

    public FishingListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.debugLogger = new DebugLogger(plugin);
        this.catchEffects = new CatchEffects(plugin);

        BindingCurseManager bindingCurseManager = new BindingCurseManager(plugin);

        CategorySelector categorySelector = new CategorySelector(plugin, debugLogger);
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(plugin);
        this.enchantLimiter = new EnchantLimiter(plugin);
        this.fishingProcessor = new FishingProcessor(plugin, debugLogger, bindingCurseManager, categorySelector, probabilityCalculator, enchantLimiter);
        this.doubleFishingHandler = new DoubleFishingHandler(plugin, fishingProcessor, categorySelector, catchEffects);
        this.timingHandler = new TimingHandler(plugin, debugLogger);
        this.luckCalculator = new LuckCalculator(plugin, debugLogger);
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
            // 入れ食いLv6以上の竿でも浮きが沈むように待ち時間を補正
            enchantLimiter.applyLureBehaviorCap(event.getHook(), getFishingRod(player));
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

        // バイパスパーミッションを持つプレイヤーはバニラの釣り結果を使用
        if (player.hasPermission("customizefishing.bypass")) {
            debugLogger.logInfo(player, "[BYPASS] Player has customizefishing.bypass permission, using vanilla result");
            debugLogger.logFishingEnd(player);
            return;
        }

        // Modアイテムの場合はカスタム処理をスキップ（minecraft以外の名前空間を持つアイテム）
        String itemNamespace = itemEntity.getItemStack().getType().getKey().getNamespace();
        if (!"minecraft".equals(itemNamespace)) {
            debugLogger.logInfo(player, "[MOD] Mod item detected (" + itemEntity.getItemStack().getType().getKey() + "), keeping original item");
            debugLogger.logFishingEnd(player);
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

        LuckResult luckResult = luckCalculator.calculateTotalLuck(player, weather, timingResult);

        boolean canDoubleFish = doubleFishingHandler.canDoubleFish(player);

        if (canDoubleFish && plugin.getConfig().getBoolean("double_fishing.enabled", true)) {
            doubleFishingHandler.handleDoubleFishing(player, itemEntity, hookLocation, timingResult, luckResult, isOpenWater, weather);
        } else {
            FishingProcessor.FishingResult result = fishingProcessor.processFishing(player, itemEntity, hookLocation, false, timingResult, luckResult, isOpenWater, weather);
            if (result.category() != null) {
                catchEffects.playCatchEffects(player, result.category(), result.probabilityInfo());
            }
        }

        timingHandler.displayTimingAtHook(hookLocation, timingResult);
        debugLogger.logFishingEnd(player);
    }


    /**
     * プレイヤーが使用中の釣り竿を取得する（メインハンド優先、なければオフハンド）
     *
     * @param player プレイヤー
     * @return 釣り竿のItemStack。どちらの手にも無い場合はメインハンドのアイテム
     */
    private ItemStack getFishingRod(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.FISHING_ROD) {
            return mainHand;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.FISHING_ROD) {
            return offHand;
        }
        return mainHand;
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