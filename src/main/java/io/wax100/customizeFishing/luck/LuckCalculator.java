package io.wax100.customizeFishing.luck;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.timing.TimingResult;
import io.wax100.customizeFishing.timing.TimingTier;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Objects;

/**
 * 幸運値の計算を一元管理するクラス
 */
public class LuckCalculator {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;

    public LuckCalculator(CustomizeFishing plugin, DebugLogger debugLogger) {
        this.plugin = plugin;
        this.debugLogger = debugLogger;
    }

    /**
     * 全ての幸運値を計算
     *
     * @param player       プレイヤー
     * @param weather      天気
     * @param timingResult タイミング結果
     * @return 幸運計算結果
     */
    public LuckResult calculateTotalLuck(Player player, Weather weather, TimingResult timingResult) {

        // 宝釣りエンチャント
        int luckOfTheSeaLevel = calculateLuckOfTheSea(player);

        // 幸運ポーション効果
        int luckPotionLevel = calculateLuckPotion(player);

        // 不幸ポーション効果
        int unluckPotionLevel = calculateUnluckPotion(player);

        // コンジットパワーボーナス
        int conduitLevel = getConduitLevel(player);

        // 装備の幸運属性
        double equipmentLuck = calculateEquipmentLuck(player);

        // 天気ボーナス
        double weatherLuck = calculateWeatherLuck(weather);

        // タイミングボーナス
        double timingLuck = timingResult.luckBonus();

        // プレイヤーの経験値レベル
        int experienceLevel = player.getLevel();

        return new LuckResult(
                luckOfTheSeaLevel,
                luckPotionLevel,
                unluckPotionLevel,
                conduitLevel,
                equipmentLuck,
                weatherLuck,
                timingLuck,
                experienceLevel
        );
    }

    /**
     * 特定の釣り竿を指定して全ての幸運値を計算
     *
     * @param player       プレイヤー
     * @param weather      天気
     * @param timingResult タイミング結果
     * @param fishingRod   使用する釣り竿
     * @return 幸運計算結果
     */
    public LuckResult calculateTotalLuckWithSpecificRod(Player player, Weather weather, TimingResult timingResult, ItemStack fishingRod) {

        // 特定の釣り竿の宝釣りエンチャント
        int luckOfTheSeaLevel = calculateLuckOfTheSeaForItem(fishingRod);

        // 幸運ポーション効果
        int luckPotionLevel = calculateLuckPotion(player);

        // 不幸ポーション効果
        int unluckPotionLevel = calculateUnluckPotion(player);

        // コンジットパワーボーナス
        int conduitLevel = getConduitLevel(player);

        // 装備の幸運属性
        double equipmentLuck = calculateEquipmentLuck(player);

        // 天気ボーナス
        double weatherLuck = calculateWeatherLuck(weather);

        // タイミングボーナス
        double timingLuck = timingResult.luckBonus();

        // プレイヤーの経験値レベル
        int experienceLevel = player.getLevel();

        return new LuckResult(
                luckOfTheSeaLevel,
                luckPotionLevel,
                unluckPotionLevel,
                conduitLevel,
                equipmentLuck,
                weatherLuck,
                timingLuck,
                experienceLevel
        );
    }

    /**
     * 宝釣りエンチャントレベルを計算
     */
    private int calculateLuckOfTheSea(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        int mainHandLuckOfTheSeaLevel = mainHand.getEnchantmentLevel(Enchantment.LUCK);
        int offHandLuckOfTheSeaLevel = offHand.getEnchantmentLevel(Enchantment.LUCK);
        return Math.max(mainHandLuckOfTheSeaLevel, offHandLuckOfTheSeaLevel);
    }

    /**
     * 特定のアイテムから宝釣りエンチャントレベルを取得
     */
    private int calculateLuckOfTheSeaForItem(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) {
            return 0;
        }
        return item.getEnchantmentLevel(Enchantment.LUCK);
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
     * 不幸ポーション効果を計算
     */
    private int calculateUnluckPotion(Player player) {
        if (player.hasPotionEffect(PotionEffectType.UNLUCK)) {
            return Objects.requireNonNull(player.getPotionEffect(PotionEffectType.UNLUCK)).getAmplifier() + 1;
        } else {
            return 0;
        }
    }

    /**
     * 装備の幸運属性値を計算
     */
    private double calculateEquipmentLuck(Player player) {
        debugLogger.logInfo(player, " EQUIPMENT LUCK:");
        // ヘルメット
        ItemStack helmet = player.getInventory().getHelmet();
        double helmetLuck = 0;
        if (helmet != null && helmet.getType() != Material.AIR) {
            helmetLuck = getItemLuck(player, helmet, EquipmentSlot.HEAD);
        }

        // チェストプレート
        ItemStack chestplate = player.getInventory().getChestplate();
        double chestLuck = 0;
        if (chestplate != null) {
            chestLuck = getItemLuck(player, chestplate, EquipmentSlot.CHEST);
        }

        // レギンス
        ItemStack leggings = player.getInventory().getLeggings();
        double legsLuck = 0;
        if (leggings != null) {
            legsLuck = getItemLuck(player, leggings, EquipmentSlot.LEGS);
        }

        // ブーツ
        ItemStack boots = player.getInventory().getBoots();
        double bootsLuck = 0;
        if (boots != null) {
            bootsLuck = getItemLuck(player, boots, EquipmentSlot.FEET);
        }

        // メインハンド
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        double mainHandLuck = 0;
        if (!mainHand.getType().isAir()) {
            mainHandLuck = getItemLuck(player, mainHand, EquipmentSlot.HAND);
        }

        // オフハンド
        ItemStack offHand = player.getInventory().getItemInOffHand();
        double offHandLuck = 0;
        if (!offHand.getType().isAir()) {
            offHandLuck = getItemLuck(player, offHand, EquipmentSlot.OFF_HAND);
        }
        // config.ymlの設定に基づいて制限を適用
        double minEquipmentLuck = plugin.getConfig().getDouble("luck_effects.equipment_luck.min_value", -6.0);
        double maxEquipmentLuck = plugin.getConfig().getDouble("luck_effects.equipment_luck.max_value", 6.0);


        double finalEquipmentLuck = helmetLuck + chestLuck + legsLuck + bootsLuck + mainHandLuck + offHandLuck;
        debugLogger.logEquipmentLuck(player,
                helmetLuck, chestLuck, legsLuck, bootsLuck,
                mainHandLuck, offHandLuck, finalEquipmentLuck
        );

        // 各装備スロットの幸運値を制限内にクランプ
        helmetLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, helmetLuck));
        chestLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, chestLuck));
        legsLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, legsLuck));
        bootsLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, bootsLuck));
        mainHandLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, mainHandLuck));
        offHandLuck = Math.max(minEquipmentLuck, Math.min(maxEquipmentLuck, offHandLuck));
        finalEquipmentLuck = helmetLuck + chestLuck + legsLuck + bootsLuck + mainHandLuck + offHandLuck;

        // 合計値も制限内にクランプ
        double totalMinLuck = minEquipmentLuck * 6; // 6スロット分
        double totalMaxLuck = maxEquipmentLuck * 6;
        finalEquipmentLuck = Math.max(totalMinLuck, Math.min(totalMaxLuck, finalEquipmentLuck));

        debugLogger.logInfo(player, " FINAL EQUIPMENT LUCK:");
        debugLogger.logEquipmentLuck(player,
                helmetLuck, chestLuck, legsLuck, bootsLuck,
                mainHandLuck, offHandLuck, finalEquipmentLuck
        );
        return finalEquipmentLuck;
    }

    /**
     * アイテムから幸運属性値を取得
     */
    private double getItemLuck(Player player, ItemStack item, EquipmentSlot slot) {
        if (item == null || !item.hasItemMeta()) {
            debugLogger.logInfo(player, "     Item is null or has no meta for slot: " + slot);
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            debugLogger.logInfo(player, "     ItemMeta is null for slot: " + slot);
            return 0;
        }

        // アイテム名をログ出力
        String itemName = item.getType().name();
        if (meta.hasDisplayName()) {
            itemName = meta.getDisplayName();
        }
        debugLogger.logInfo(player, "     Checking item: " + itemName + " at slot: " + slot);

        if (!meta.hasAttributeModifiers()) {
            debugLogger.logInfo(player, "     Item has no attribute modifiers for slot: " + slot);
            return 0;
        }

        // 全属性修飾子をログ出力
        var allModifiers = meta.getAttributeModifiers();
        if (allModifiers != null && !allModifiers.isEmpty()) {
            debugLogger.logInfo(player, "     All attribute modifiers:");
            for (Map.Entry<Attribute, AttributeModifier> entry : allModifiers.entries()) {
                debugLogger.logInfo(player, String.format(
                        "       Attribute: %s, Modifier: %s = %.3f (Operation: %s, Slot: %s)",
                        entry.getKey().name(), entry.getValue().getName(),
                        entry.getValue().getAmount(), entry.getValue().getOperation(),
                        entry.getValue().getSlot()
                ));
            }
        } else {
            debugLogger.logInfo(player, "     No attribute modifiers found");
        }

        // Minecraftの属性計算仕様に従って計算
        // 参考: https://minecraft.wiki/w/Attribute#Modifiers
        // 1. ベース値（GENERIC_LUCKのデフォルトは0）
        double baseLuck = 0;
        // 2. ADD_NUMBER の合計値
        double addNumber = 0;
        // 3. ADD_SCALAR の合計値（パーセンテージ加算）
        double addScalar = 0;
        // 4. MULTIPLY_SCALAR_1 の乗算値（複数ある場合は全て乗算）
        double multiplyScalar = 1.0;

        var modifiers = meta.getAttributeModifiers(Attribute.GENERIC_LUCK);
        if (modifiers != null && !modifiers.isEmpty()) {
            debugLogger.logInfo(player, "     Found " + modifiers.size() + " luck modifiers");

            // まず ADD_NUMBER を処理
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getSlot() == null || modifier.getSlot() == slot) {
                    double modifierValue = modifier.getAmount();

                    if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                        debugLogger.logInfo(player, String.format(
                                "     ADD_NUMBER modifier: %s = %.3f",
                                modifier.getName(), modifierValue
                        ));
                        addNumber += modifierValue;
                    }
                }
            }

            // 次に ADD_SCALAR を処理（ベース値にパーセント加算）
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getSlot() == null || modifier.getSlot() == slot) {
                    double modifierValue = modifier.getAmount();

                    if (modifier.getOperation() == AttributeModifier.Operation.ADD_SCALAR) {
                        debugLogger.logInfo(player, String.format(
                                "     ADD_SCALAR modifier: %s = %.3f (%.0f%%)",
                                modifier.getName(), modifierValue, modifierValue * 100
                        ));
                        // ADD_SCALARは「元の値に対するパーセント」として扱う
                        // 例: value=1.0 なら +100%
                        addScalar += modifierValue;
                    }
                }
            }

            // 最後に MULTIPLY_SCALAR_1 を処理
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getSlot() == null || modifier.getSlot() == slot) {
                    double modifierValue = modifier.getAmount();

                    if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_SCALAR_1) {
                        debugLogger.logInfo(player, String.format(
                                "     MULTIPLY_SCALAR_1 modifier: %s = %.3f (×%.1f)",
                                modifier.getName(), modifierValue, (1 + modifierValue)
                        ));
                        // MULTIPLY_SCALAR_1: (1 + value) を乗算
                        // 例: value=2.0 なら ×3.0
                        multiplyScalar *= (1 + modifierValue);
                    }
                }
            }
        } else {
            debugLogger.logInfo(player, "     No GENERIC_LUCK modifiers found for slot: " + slot);
        }

        // Minecraft式の計算順序:
        // 1. (base + ADD_NUMBER)
        // 2. 結果に ADD_SCALAR を適用: result * (1 + ADD_SCALAR合計)
        // 3. 最後に MULTIPLY_SCALAR_1 を適用
        double step1 = baseLuck + addNumber;
        double step2 = step1 * (1 + addScalar);  // ADD_SCALARはベース値に対するパーセント
        double finalLuck = step2 * multiplyScalar;

        debugLogger.logInfo(player, "     Calculation steps:");
        debugLogger.logInfo(player, String.format(
                "       Step 1: base(%.1f) + ADD_NUMBER(%.3f) = %.3f",
                baseLuck, addNumber, step1
        ));
        debugLogger.logInfo(player, String.format(
                "       Step 2: %.3f × (1 + ADD_SCALAR(%.3f)) = %.3f",
                step1, addScalar, step2
        ));
        debugLogger.logInfo(player, String.format(
                "       Step 3: %.3f × MULTIPLY_SCALAR_1(%.3f) = %.3f",
                step2, multiplyScalar, finalLuck
        ));

        return finalLuck;
    }

    /**
     * 天気による幸運ボーナスを計算
     */
    private double calculateWeatherLuck(Weather weather) {
        return plugin.getConfig().getDouble("weather_luck." + weather.getConfigKey(), 0.0);
    }

    /**
     * コンジットパワーによる幸運ボーナスを計算
     */
    private int getConduitLevel(Player player) {
        int conduitLevel = 0;
        if (player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) {
            conduitLevel = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.CONDUIT_POWER)).getAmplifier() + 1;
        }
        return conduitLevel;
    }

    /**
     * タイミング結果を計算
     *
     * @param reactionTimeMs 反応時間（ミリ秒）
     * @return タイミング結果
     */
    public TimingResult calculateTimingResult(long reactionTimeMs) {
        if (!plugin.getConfig().getBoolean("timing_system.enabled", true)) {
            return TimingResult.miss();
        }

        double baseLuckBonus = plugin.getConfig().getDouble("timing_system.base_luck_bonus", 1.5);

        // タイミングティアを素早い順にチェック
        String[] tierNames = {"just", "perfect", "great", "good"};

        for (String tierName : tierNames) {
            String configPath = "timing_system.tiers." + tierName;
            double maxTimeMs = plugin.getConfig().getDouble(configPath + ".max_time_ms", Double.MAX_VALUE);
            double bonusMultiplier = plugin.getConfig().getDouble(configPath + ".bonus_multiplier", 0);
            TimingTier tier = new TimingTier(tierName, maxTimeMs, bonusMultiplier);
            if (tier.matches(reactionTimeMs)) {
                double luckBonus = tier.calculateLuckBonus(baseLuckBonus);
                return TimingResult.success(tier, reactionTimeMs, luckBonus);
            }
        }
        return TimingResult.miss();
    }

}