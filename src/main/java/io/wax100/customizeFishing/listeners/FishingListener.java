package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.debug.DebugFishingRod;
import io.wax100.customizeFishing.fishing.FishingConditionChecker;
import io.wax100.customizeFishing.fishing.PlayerHeadProcessor;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.utils.MessageDisplay;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingListener implements Listener {

    private final CustomizeFishing plugin;
    private final Random random = new Random();
    private final CatchEffects catchEffects;
    private final Map<Player, Long> biteTimestamps = new ConcurrentHashMap<>();
    private final DebugLogger debugLogger;

    public FishingListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.catchEffects = new CatchEffects(plugin);
        this.debugLogger = new DebugLogger(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean("enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        // 魚が食いついた時間を記録
        if (event.getState() == PlayerFishEvent.State.BITE) {
            biteTimestamps.put(player, System.currentTimeMillis());
            return;
        }

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        if (!(event.getCaught() instanceof Item itemEntity)) {
            return;
        }

        Location hookLocation = event.getHook().getLocation();
        
        // コンジットパワーチェック
        boolean hasConduitPower = player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
        
        // コンジットパワーがある場合、両方の結果を同時に処理
        if (hasConduitPower && plugin.getConfig().getBoolean("conduit_power.double_fishing", true)) {
            // 両方の結果を保存するための準備
            FishingResult firstResult = processFishingWithResult(player, itemEntity, hookLocation, false);
            
            // 新しいアイテムエンティティを作成
            ItemStack bonusItem = new ItemStack(Material.COD); // デフォルトアイテム
            Item bonusEntity = player.getWorld().dropItem(hookLocation, bonusItem);
            bonusEntity.setPickupDelay(Integer.MAX_VALUE); // 自動拾いを無効化
            
            FishingResult secondResult = processFishingWithResult(player, bonusEntity, hookLocation, true);
            
            // 両方の結果を同時に表示
            displayConduitPowerResults(player, firstResult, secondResult);
            
            // ボーナスアイテムをプレイヤーの位置に移動
            bonusEntity.teleport(player.getLocation());
            bonusEntity.setPickupDelay(0); // 拾えるように戻す
        } else {
            // 通常の1回の処理
            processFishing(player, itemEntity, hookLocation, false);
        }
    }
    
    /**
     * 釣り処理のメイン部分
     */
    private void processFishing(Player player, Item itemEntity, Location hookLocation, boolean isConduitBonus) {
        FishingResult result = processFishingCore(player, itemEntity, hookLocation, isConduitBonus);
        
        // 確率情報を計算
        String probabilityInfo = result.probabilityInfo();
        
        // コンジットパワーのメッセージを追加
        if (!isConduitBonus && player.hasPotionEffect(PotionEffectType.CONDUIT_POWER) && 
            plugin.getConfig().getBoolean("conduit_power.double_fishing", true)) {
            probabilityInfo = (probabilityInfo.isEmpty() ? "" : probabilityInfo + " ") + "&b&l⚡ コンジットパワー x2 ⚡";
        }
        
        // レア度別演出を実行
        catchEffects.playCatchEffects(player, result.category(), probabilityInfo);
        
        // デバッグ終了ログ
        debugLogger.logFishingEnd();
    }

    /**
     * カテゴリを設定ファイルから決定するメソッド
     * @param openWater 開水域かどうか
     * @param weather 天気
     * @param dolphinsGrace イルカの好意エフェクトがあるか
     * @return 選択されたカテゴリ
     */
    private String determineCategoryFromConfig(LuckResult luckResult, boolean openWater, String weather, boolean dolphinsGrace) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return "common";
        }

        List<CategoryData> categories = new ArrayList<>();

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);
            
            if (!checkCategoryConditions(categorySection, luckResult, openWater, weather, dolphinsGrace)) {
                continue;
            }

            int priority = categorySection.getInt("priority", 999);
            double quality = categorySection.getDouble("quality", 0);
            double chance = categorySection.getDouble("chance", 0);

            categories.add(new CategoryData(categoryName, priority, quality, chance));
        }

        if (categories.isEmpty()) {
            return "common";
        }

        double totalLuck = luckResult.getTotalLuck();

        // qualityを使用して各カテゴリのchanceを補正
        List<CategoryData> adjustedCategories = new ArrayList<>();
        double totalWeight = 0;

        for (CategoryData category : categories) {
            // Minecraft方式: floor(chance + (quality × luck)) での補正
            double adjustedChance = Math.floor(category.chance() + (category.quality() * totalLuck));
            
            // 補正後のchanceが0以下の場合はスキップ
            if (adjustedChance <= 0) {
                debugLogger.logCategoryDetails(
                    category.name() + " (SKIPPED)", category.priority(), category.quality(),
                    category.chance(), adjustedChance, totalLuck
                );
                continue;
            }

            adjustedCategories.add(new CategoryData(category.name(), category.priority(), category.quality(), adjustedChance));
            totalWeight += adjustedChance;

            debugLogger.logCategoryDetails(
                category.name(), category.priority(), category.quality(), 
                category.chance(), adjustedChance, totalLuck
            );
        }

        if (adjustedCategories.isEmpty() || totalWeight <= 0) {
            return "common";
        }

        // 重み付き抽選を実行
        double roll = random.nextDouble() * totalWeight;
        double currentWeight = 0;

        // priority順にソート
        adjustedCategories.sort(Comparator.comparingInt(CategoryData::priority));

        for (CategoryData category : adjustedCategories) {
            currentWeight += category.chance();  // 補正されたchanceを使用
            if (roll < currentWeight) {
                return category.name();
            }
        }

        return "common";
    }


    private String getItemDisplayName(ItemStack item) {
        if (item == null) return "null";
        String displayName = item.getType().name().toLowerCase().replace("_", " ");
        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName()) {
            displayName = item.getItemMeta().getDisplayName();
        }
        return displayName + " x" + item.getAmount();
    }
    
    /**
     * カテゴリの条件をチェックし、条件を満たすかを返す
     */
    private boolean checkCategoryConditions(ConfigurationSection categorySection, LuckResult luckResult, boolean openWater, String weather, boolean dolphinsGrace) {
        if (categorySection == null || !categorySection.getBoolean("enabled", true)) {
            return false;
        }

        ConfigurationSection conditionsSection = categorySection.getConfigurationSection("conditions");
        if (conditionsSection == null) {
            return false;
        }

        boolean requireOpenWater = conditionsSection.getBoolean("require_open_water", false);
        if (requireOpenWater && !openWater) {
            return false;
        }

        boolean requireDolphinsGrace = conditionsSection.getBoolean("require_dolphins_grace", false);
        if (requireDolphinsGrace && !dolphinsGrace) {
            return false;
        }

        // 宝釣りレベルチェック
        int minLuckOfTheSea = conditionsSection.getInt("min_luck_of_the_sea", 0);
        if (luckResult.luckOfTheSeaLevel() < minLuckOfTheSea) {
            return false;
        }
        
        // 宝釣り最大レベルチェック
        if (conditionsSection.contains("max_luck_of_the_sea")) {
            int maxLuckOfTheSea = conditionsSection.getInt("max_luck_of_the_sea");
            if (luckResult.luckOfTheSeaLevel() > maxLuckOfTheSea) {
                return false;
            }
        }

        // 幸運エフェクトレベルチェック
        int minLuckEffect = conditionsSection.getInt("min_luck_effect", 0);
        if (luckResult.luckPotionLevel() < minLuckEffect) {
            return false;
        }
        
        // 幸運エフェクト最大レベルチェック
        if (conditionsSection.contains("max_luck_effect")) {
            int maxLuckEffect = conditionsSection.getInt("max_luck_effect");
            if (luckResult.luckPotionLevel() > maxLuckEffect) {
                return false;
            }
        }

        List<String> allowedWeather = conditionsSection.getStringList("weather");
        return allowedWeather.isEmpty() || allowedWeather.contains(weather);
    }

    /**
     * 条件を満たすカテゴリ数を取得（デバッグ用）
     */
    private int getEligibleCategoryCount(LuckResult luckResult, boolean openWater, String weather, boolean dolphinsGrace) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return 0;
        }

        int count = 0;
        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);
            if (checkCategoryConditions(categorySection, luckResult, openWater, weather, dolphinsGrace)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 確率情報を計算してフォーマットした文字列を返す
     */
    private String calculateProbabilityInfo(String selectedCategory, LuckResult luckResult, String weather, TimingResult timingResult) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return "";
        }
        
        ConfigurationSection categorySection = categoriesSection.getConfigurationSection(selectedCategory);
        if (categorySection == null) {
            return "";
        }
        
        double baseChance = categorySection.getDouble("chance", 0.0);
        double quality = categorySection.getDouble("quality", 0.0);
        String probabilityText = getProbabilityText(luckResult, baseChance, quality);

        // ボーナス要因を表示
        StringBuilder bonusText = new StringBuilder();
        
        // 宝釣りエンチャント
        if (luckResult.luckOfTheSeaLevel() > 0) {
            double luckOfTheSeaBonus = Math.min(10, luckResult.luckOfTheSeaLevel()) * 0.08;
            bonusText.append(" &a宝釣り+").append(String.format("%.2f%%", luckOfTheSeaBonus));
        }
        
        // 幸運ポーション
        if (luckResult.luckPotionLevel() > 0) {
            double luckPotionBonus = Math.min(10, luckResult.luckPotionLevel()) * 0.05;
            bonusText.append(" &b幸運+").append(String.format("%.2f%%", luckPotionBonus));
        }
        
        // 装備幸運
        if (luckResult.equipmentLuck() > 0) {
            double equipmentBonus = Math.min(6, luckResult.equipmentLuck()) * 0.1;
            bonusText.append(" &d装備+").append(String.format("%.2f%%", equipmentBonus));
        }
        
        // 天気ボーナス
        if (luckResult.weatherLuck() > 0) {
            String weatherName = switch (weather) {
                case "rain" -> "雨";
                case "thunder" -> "雷雨";
                default -> weather;
            };
            bonusText.append(" &9").append(weatherName).append("+").append(String.format("%.2f%%", luckResult.weatherLuck()));
        }
        
        // タイミングボーナス
        if (timingResult != null && timingResult.hasTiming() && luckResult.timingLuck() > 0) {
            String timingName = switch (timingResult.tier().name().toLowerCase()) {
                case "just" -> "JUST";
                case "perfect" -> "PERFECT";
                case "great" -> "GREAT";
                case "good" -> "GOOD";
                default -> timingResult.tier().name().toUpperCase();
            };
            bonusText.append(" &6").append(timingName).append("+").append(String.format("%.2f%%", luckResult.timingLuck()));
        }
        
        return probabilityText + bonusText.toString();
    }

    private static String getProbabilityText(LuckResult luckResult, double baseChance, double quality) {
        double totalLuck = luckResult.getTotalLuck();
        double adjustedChance = Math.floor(baseChance + (quality * totalLuck));

        // 確率情報を日本語でフォーマット
        String probabilityText;
       if (adjustedChance > 0) {
            probabilityText = "&7確率: &e" + String.format("%.2f%%", adjustedChance) + " &7(基本: &f" + String.format("%.2f%%", baseChance) + "&7)";
        } else {
            probabilityText = "&7確率: &e" + String.format("%.4f%%", baseChance);
        }
        return probabilityText;
    }

    /**
     * タイミングをチェック
     * @param player プレイヤー
     * @return タイミング結果
     */
    private TimingResult checkTiming(Player player) {
        Long biteTime = biteTimestamps.remove(player);
        if (biteTime == null) {
            return TimingResult.miss();
        }
        long reactionTime = System.currentTimeMillis() - biteTime;
        
        // LuckCalculatorでタイミング結果を計算
        LuckCalculator luckCalc = new LuckCalculator(plugin);
        return luckCalc.calculateTimingResult(reactionTime);
    }

    /**
     * Twilight Forest の rainy_cloud ブロックが釣り針の上空32ブロック以内にあるかチェック
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

    private record CategoryData(String name, int priority, double quality, double chance) {
    }
    
    /**
     * 釣り結果を保存するレコード
     */
    public record FishingResult(String category, String probabilityInfo, ItemStack item) {
    }
    
    /**
     * 釣り処理のコア部分（共通処理）
     */
    private FishingResult processFishingCore(Player player, Item itemEntity, Location hookLocation, boolean isConduitBonus) {
        // タイミング判定とメッセージ表示
        TimingResult timingResult = checkTiming(player);

        boolean isOpenWater = FishingConditionChecker.isOpenWater(hookLocation);
        String weather = "clear";
        if (Objects.requireNonNull(hookLocation.getWorld()).isThundering()) {
            weather = "thunder";
        } else if (hookLocation.getWorld().hasStorm()) {
            weather = "rain";
        }
        
        // Twilight Forest rainy_cloud の検出（ウキの上空32ブロック以内）
        if (checkForTwilightRainyClouds(hookLocation)) {
            weather = "rain";
        }

        boolean hasDolphinsGrace = player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);
        
        // LuckCalculatorで全ての幸運値を計算
        LuckCalculator luckCalc = new LuckCalculator(plugin);
        LuckResult luckResult = luckCalc.calculateTotalLuck(player, weather, timingResult);
        
        // デバッグ情報の出力
        if (isConduitBonus) {
            debugLogger.logInfo("=== CONDUIT POWER: BONUS FISHING PROCESS ===");
        }
        debugLogger.logFishingStart(player, isOpenWater, weather, hasDolphinsGrace, null);
        debugLogger.logTimingResult(timingResult.reactionTimeMs(), timingResult);
        debugLogger.logLuckBreakdown(luckResult);
        String forcedCategory = null;
        // デバッグロッドのチェック
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (DebugFishingRod.isDebugRod(plugin, mainHand)) {
            forcedCategory = DebugFishingRod.getForcedCategory(plugin, mainHand);
            // デバッグ開始ログを再出力
            debugLogger.logFishingStart(player, isOpenWater, weather, hasDolphinsGrace, forcedCategory);
        }
        String category;
        if (forcedCategory != null) {
            category = forcedCategory;
        } else {
            category = determineCategoryFromConfig(luckResult, isOpenWater, weather, hasDolphinsGrace);
        }
        
        // カテゴリ選択結果をログ出力
        int eligibleCount = getEligibleCategoryCount(luckResult, isOpenWater, weather, hasDolphinsGrace);
        debugLogger.logCategorySelection(category, eligibleCount);

        String namespace = plugin.getConfig().getString("loot_tables.namespace", "customize_fishing");
        String path = plugin.getConfig().getString("loot_tables.path", "gameplay/fishing");
        NamespacedKey lootTableKey = NamespacedKey.fromString(namespace + ":" + path + "/" + category);
        LootTable lootTable = plugin.getServer().getLootTable(Objects.requireNonNull(lootTableKey));

        ItemStack originalItem = itemEntity.getItemStack().clone();
        ItemStack selectedItem = originalItem;

        if (lootTable != null) {
            LootContext.Builder contextBuilder = new LootContext.Builder(hookLocation)
                    .killer(player)
                    .lootedEntity(itemEntity)
                    .luck((float) luckResult.getTotalLuck());

            LootContext lootContext = contextBuilder.build();
            Collection<ItemStack> loot = lootTable.populateLoot(random, lootContext);

            if (!loot.isEmpty()) {
                selectedItem = loot.iterator().next();

                // イルカの好意カテゴリでプレイヤーヘッドの場合、釣り人の顔に置換
                selectedItem = PlayerHeadProcessor.processPlayerHead(selectedItem, player, category);

                itemEntity.setItemStack(selectedItem);

                debugLogger.logItemReplacement(
                    getItemDisplayName(originalItem), 
                    getItemDisplayName(selectedItem), 
                    lootTableKey.toString()
                );
            }
        } else {
            // ルートテーブルが見つからない場合はエラー出力
            debugLogger.logError("Loot table not found: " + lootTableKey);
        }

        // 確率情報を計算
        String probabilityInfo = calculateProbabilityInfo(category, luckResult, weather, timingResult);
        
        // デバッグ: タイミング情報をログ出力
        if (timingResult.hasTiming()) {
            debugLogger.logInfo(String.format(" Timing Bonus Debug: tier=%s, luckBonus=%.2f", 
                timingResult.tier().name(), timingResult.luckBonus()));
        }
        
        return new FishingResult(category, probabilityInfo, selectedItem);
    }
    
    /**
     * 釣り処理を実行し、結果を返す
     */
    private FishingResult processFishingWithResult(Player player, Item itemEntity, Location hookLocation, boolean isConduitBonus) {
        return processFishingCore(player, itemEntity, hookLocation, isConduitBonus);
    }
    
    /**
     * コンジットパワー時の両方の結果を同時に表示
     */
    private void displayConduitPowerResults(Player player, FishingResult first, FishingResult second) {
        // エフェクトは両方のカテゴリで最もレアな方を使用
        String primaryCategory = getHigherPriorityCategory(first.category(), second.category());
        
        // カスタムメッセージ表示とエフェクト実行
        catchEffects.playConduitPowerEffects(player, primaryCategory, first, second);
    }
    
    /**
     * より優先度の高いカテゴリを返す
     */
    private String getHigherPriorityCategory(String cat1, String cat2) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return cat1;
        }
        
        int priority1 = categoriesSection.getInt(cat1 + ".priority", 999);
        int priority2 = categoriesSection.getInt(cat2 + ".priority", 999);
        
        return priority1 <= priority2 ? cat1 : cat2;
    }
        
}