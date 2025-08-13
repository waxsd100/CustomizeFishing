package io.wax100.customizeFishing.luck;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.timing.TimingResult;
import io.wax100.customizeFishing.timing.TimingTier;
import io.wax100.customizeFishing.debug.DebugLogger;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * 幸運値の計算を一元管理するクラス
 */
public class LuckCalculator {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    
    public LuckCalculator(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.debugLogger = new DebugLogger(plugin);
    }
    
    /**
     * 全ての幸運値を計算
     * @param player プレイヤー
     * @param weather 天気
     * @param timingResult タイミング結果
     * @return 幸運計算結果
     */
    public LuckResult calculateTotalLuck(Player player, String weather, TimingResult timingResult) {
        // 宝釣りエンチャント
        int luckOfTheSeaLevel = calculateLuckOfTheSea(player);

        
        // 幸運ポーション効果
        int luckPotionLevel = calculateLuckPotion(player);
        double luckPotionValue = luckPotionLevel * 0.5; // Lv1 = 0.5, Lv2 = 1.0 Lv3 = 1.5, Lv4 = 2.0... Lv 10 = 5.0

        // 装備の幸運属性
        double equipmentLuck = calculateEquipmentLuck(player);
        
        // 天気ボーナス
        double weatherLuck = calculateWeatherLuck(weather);
        
        // タイミングボーナス
        double timingLuck = timingResult.luckBonus();
        
        // 合計を計算

        double totalLuck = luckOfTheSeaLevel + luckPotionValue + equipmentLuck + weatherLuck + timingLuck;

        return new LuckResult(
            luckOfTheSeaLevel,
            luckPotionLevel,
            equipmentLuck,
            weatherLuck,
            timingLuck,
            totalLuck
        );
    }
    
    /**
     * 宝釣りエンチャントレベルを計算
     */
    private int calculateLuckOfTheSea(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        int luckOfTheSeaLevel = mainHand.getEnchantmentLevel(Enchantment.LUCK);
        if (luckOfTheSeaLevel == 0) {
            luckOfTheSeaLevel = offHand.getEnchantmentLevel(Enchantment.LUCK);
        }
        return luckOfTheSeaLevel;
    }
    
    /**
     * 幸運ポーション効果を計算
     */
    private int calculateLuckPotion(Player player) {
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            return Objects.requireNonNull(player.getPotionEffect(PotionEffectType.LUCK)).getAmplifier() + 1;
        } else {
            return 0;
        }
    }
    
    /**
     * 装備の幸運属性値を計算
     */
    private double calculateEquipmentLuck(Player player) {
        double totalLuck = 0;
        
        // ヘルメット（最大1）
        ItemStack helmet = player.getInventory().getHelmet();
        double helmetLuck = 0;
        if (helmet != null) {
            helmetLuck = Math.min(1.0, getItemLuck(helmet, EquipmentSlot.HEAD));
            totalLuck += helmetLuck;
        }
        
        // チェストプレート（最大1）
        ItemStack chestplate = player.getInventory().getChestplate();
        double chestLuck = 0;
        if (chestplate != null) {
            chestLuck = Math.min(1.0, getItemLuck(chestplate, EquipmentSlot.CHEST));
            totalLuck += chestLuck;
        }
        
        // レギンス（最大1）
        ItemStack leggings = player.getInventory().getLeggings();
        double legsLuck = 0;
        if (leggings != null) {
            legsLuck = Math.min(1.0, getItemLuck(leggings, EquipmentSlot.LEGS));
            totalLuck += legsLuck;
        }
        
        // ブーツ（最大1）
        ItemStack boots = player.getInventory().getBoots();
        double bootsLuck = 0;
        if (boots != null) {
            bootsLuck = Math.min(1.0, getItemLuck(boots, EquipmentSlot.FEET));
            totalLuck += bootsLuck;
        }
        
        // メインハンド（最大1）
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        double mainHandLuck = 0;
        if (!mainHand.getType().isAir()) {
            mainHandLuck = Math.min(1.0, getItemLuck(mainHand, EquipmentSlot.HAND));
            totalLuck += mainHandLuck;
        }
        
        // オフハンド（最大1）
        ItemStack offHand = player.getInventory().getItemInOffHand();
        double offHandLuck = 0;
        if (!offHand.getType().isAir()) {
            offHandLuck = Math.min(1.0, getItemLuck(offHand, EquipmentSlot.OFF_HAND));
            totalLuck += offHandLuck;
        }
        
        // 全装備の合計幸運値を最大4に制限
        double finalEquipmentLuck = Math.min(4.0, totalLuck);
        
        debugLogger.logEquipmentLuck(
            helmetLuck, chestLuck, legsLuck, bootsLuck, 
            mainHandLuck, offHandLuck, finalEquipmentLuck
        );
        
        return finalEquipmentLuck;
    }
    
    /**
     * アイテムから幸運属性値を取得
     */
    private double getItemLuck(ItemStack item, EquipmentSlot slot) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasAttributeModifiers()) {
            return 0;
        }
        
        double luck = 0;
        var modifiers = meta.getAttributeModifiers(Attribute.GENERIC_LUCK);
        if (modifiers != null) {
            for (AttributeModifier modifier : modifiers) {
                // スロットが一致するか、スロットが指定されていない場合
                if (modifier.getSlot() == null || modifier.getSlot() == slot) {
                    switch (modifier.getOperation()) {
                        case ADD_NUMBER:
                            luck += modifier.getAmount();
                            break;
                        case ADD_SCALAR:
                            luck += modifier.getAmount();
                            break;
                        case MULTIPLY_SCALAR_1:
                            luck *= (1 + modifier.getAmount());
                            break;
                    }
                }
            }
        }
        
        return luck;
    }
    
    /**
     * 天気による幸運ボーナスを計算
     */
    private double calculateWeatherLuck(String weather) {
        return plugin.getConfig().getDouble("weather_luck." + weather, 0.0);
    }
    
    /**
     * タイミング結果を計算
     * @param reactionTimeMs 反応時間（ミリ秒）
     * @return タイミング結果
     */
    public TimingResult calculateTimingResult(long reactionTimeMs) {
        if (!plugin.getConfig().getBoolean("timing_system.enabled", true)) {
            return TimingResult.miss();
        }
        
        double baseLuckBonus = plugin.getConfig().getDouble("timing_system.base_luck_bonus", 1.5);
        
        // タイミングティアを素早い順にチェック
        String[] tierNames = {"critical_perfect", "perfect", "great", "good"};
        
        for (String tierName : tierNames) {
            String configPath = "timing_system.tiers." + tierName;
            double maxTimeMs = plugin.getConfig().getDouble(configPath + ".max_time_ms", Double.MAX_VALUE);
            double bonusMultiplier = plugin.getConfig().getDouble(configPath + ".bonus_multiplier", 0);
            String message = plugin.getConfig().getString(configPath + ".message", "");
            
            if (reactionTimeMs <= maxTimeMs) {
                TimingTier tier = new TimingTier(tierName, maxTimeMs, bonusMultiplier, message);
                double luckBonus = tier.calculateLuckBonus(baseLuckBonus);
                return TimingResult.success(tier, reactionTimeMs, luckBonus);
            }
        }
        
        return TimingResult.miss();
    }
    
}