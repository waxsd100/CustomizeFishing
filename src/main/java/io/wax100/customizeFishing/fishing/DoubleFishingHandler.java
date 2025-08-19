package io.wax100.customizeFishing.fishing;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class DoubleFishingHandler {

    private final CustomizeFishing plugin;
    private final FishingProcessor fishingProcessor;
    private final CategorySelector categorySelector;
    private final CatchEffects catchEffects;

    public DoubleFishingHandler(CustomizeFishing plugin, FishingProcessor fishingProcessor, CategorySelector categorySelector, CatchEffects catchEffects) {
        this.plugin = plugin;
        this.fishingProcessor = fishingProcessor;
        this.categorySelector = categorySelector;
        this.catchEffects = catchEffects;
    }

    /**
     * ダブルフィッシング条件をチェック
     *
     * @param player プレイヤー
     * @return 宝釣りLv10以上かつコンジットパワーLv2以上の場合true
     */
    public boolean canDoubleFish(Player player) {
        boolean hasConduitPower = player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
        int conduitLevel = hasConduitPower ?
                Objects.requireNonNull(player.getPotionEffect(PotionEffectType.CONDUIT_POWER)).getAmplifier() + 1 : 0;

        ItemStack fishingRod = player.getInventory().getItemInMainHand();
        int luckOfSeaLevel = fishingRod.getEnchantmentLevel(Enchantment.LUCK);

        return luckOfSeaLevel >= 10 && conduitLevel >= 2;
    }

    /**
     * ダブルフィッシング処理を実行
     *
     * @param player             プレイヤー
     * @param originalItemEntity 元のアイテムエンティティ
     * @param hookLocation       釣り針の位置
     * @param timingResult       タイミング結果
     * @param luckResult         幸運計算結果
     * @param isOpenWater        開水域かどうか
     * @param weather            天気
     */
    public void handleDoubleFishing(Player player, Item originalItemEntity, Location hookLocation, TimingResult timingResult, LuckResult luckResult, boolean isOpenWater, Weather weather) {
        FishingProcessor.FishingResult firstResult = fishingProcessor.processFishing(player, originalItemEntity, hookLocation, false, timingResult, luckResult, isOpenWater, weather);

        ItemStack bonusItem = new ItemStack(Material.COD);
        Item bonusEntity = player.getWorld().dropItem(hookLocation, bonusItem);
        bonusEntity.setPickupDelay(Integer.MAX_VALUE);

        FishingProcessor.FishingResult secondResult = fishingProcessor.processFishing(player, bonusEntity, hookLocation, true, timingResult, luckResult, isOpenWater, weather);

        displayDoubleFishingResults(player, firstResult, secondResult);

        bonusEntity.teleport(player.getLocation());
        bonusEntity.setPickupDelay(0);
    }

    /**
     * ダブルフィッシング時の両方の結果を同時に表示
     *
     * @param player プレイヤー
     * @param first  最初の釣り結果
     * @param second 2回目の釣り結果
     */
    private void displayDoubleFishingResults(Player player, FishingProcessor.FishingResult first, FishingProcessor.FishingResult second) {
        String primaryCategory = categorySelector.getHigherPriorityCategory(first.category(), second.category());
        catchEffects.playDoubleFishingEffects(player, primaryCategory, first, second);
    }
}