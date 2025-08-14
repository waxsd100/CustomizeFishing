package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugFishingRod;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.fishing.FishingConditionChecker;
import io.wax100.customizeFishing.fishing.PlayerHeadProcessor;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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

    private static String getProbabilityText(LuckResult luckResult, double baseChance, double quality) {
        double totalLuck = luckResult.getTotalLuck();

        // Minecraft方式での確率補正
        double adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);

        // 確率情報を日本語でフォーマット
        String probabilityText;
        if (adjustedChance <= 0) {
            probabilityText = "&7確率: &c0.00%% &7(基本: &e" + String.format("%.2f%%", baseChance) + "&7)";
        } else if (adjustedChance == baseChance) {
            probabilityText = "&7確率: &e" + String.format("%.2f%%", baseChance);
        } else {
            double difference = adjustedChance - baseChance;
            if (difference < 0) {
                probabilityText = "&7確率: &e" + String.format("%.2f%%", adjustedChance) + " &7(補正値: " + String.format("%.2f%%", baseChance) + " &c-" + String.format("%.2f%%", difference) + "&7)";
            } else {
                probabilityText = "&7確率: &e" + String.format("%.2f%%", adjustedChance) + " &7(補正値: " + String.format("%.2f%%", baseChance) + " &a+" + String.format("%.2f%%", difference) + "&7)";
            }
        }
        return probabilityText;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfig().getBoolean("enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        PlayerFishEvent.State state = event.getState();
        Location hookLocation = event.getHook().getLocation();

        if (state == PlayerFishEvent.State.BITE)  {
            biteTimestamps.put(player, System.currentTimeMillis());
            // 釣れた瞬間の音効果を再生
            Objects.requireNonNull(hookLocation.getWorld()).playSound(hookLocation, Sound.BLOCK_LEVER_CLICK, 1.0f, 2.0f);
            return;
        }

        if (state != PlayerFishEvent.State.CAUGHT_FISH && state != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        if (!(event.getCaught() instanceof Item itemEntity)) {
            return;
        }

        debugLogger.logFishingStart();

        // ダブルフィッシング条件をチェック
        boolean canDoubleFish = checkDoubleFishingConditions(player);

        // ダブルフィッシングが有効な場合、両方の結果を同時に処理
        if (canDoubleFish && plugin.getConfig().getBoolean("double_fishing.enabled", true)) {
            // タイミング判定（1回だけ行う）
            TimingResult timingResult = checkTiming(player);

            // 両方の結果を保存するための準備
            FishingResult firstResult = processFishing(player, itemEntity, hookLocation, false, timingResult);

            // 新しいアイテムエンティティを作成
            ItemStack bonusItem = new ItemStack(Material.COD); // デフォルトアイテム
            Item bonusEntity = player.getWorld().dropItem(hookLocation, bonusItem);
            bonusEntity.setPickupDelay(Integer.MAX_VALUE); // 自動拾いを無効化

            FishingResult secondResult = processFishing(player, bonusEntity, hookLocation, true, timingResult);

            // 両方の結果を同時に表示
            displayDoubleFishingResults(player, firstResult, secondResult);

            // ボーナスアイテムをプレイヤーの位置に移動
            bonusEntity.teleport(player.getLocation());
            bonusEntity.setPickupDelay(0); // 拾えるように戻す
        } else {
            // 通常の1回の処理（タイミング判定込み）
            TimingResult timingResult = checkTiming(player);
            FishingResult result = processFishing(player, itemEntity, hookLocation, false, timingResult);

            // レア度別演出を実行
            catchEffects.playCatchEffects(player, result.category(), result.probabilityInfo());
        }
        debugLogger.logFishingEnd();
    }

    /**
     * カテゴリを設定ファイルから決定するメソッド
     *
     * @param openWater     開水域かどうか
     * @param weather       天気
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

        for (CategoryData category : categories) {
            // Minecraft方式での確率補正
            double adjustedChance = calculateAdjustedChance(category.chance(), category.quality(), totalLuck);

            // 補正後のchanceが0以下の場合はスキップ
            if (adjustedChance <= 0) {
                debugLogger.logCategoryDetails(
                        category.name() + " (SKIPPED)", category.priority(), category.quality(),
                        category.chance(), adjustedChance, totalLuck
                );
                continue;
            }

            adjustedCategories.add(new CategoryData(category.name(), category.priority(), category.quality(), adjustedChance));

            debugLogger.logCategoryDetails(
                    category.name(), category.priority(), category.quality(),
                    category.chance(), adjustedChance, totalLuck
            );
        }

        if (adjustedCategories.isEmpty() || adjustedCategories.stream().allMatch(cat -> cat.chance() <= 0)) {
            return "common";
        }


        // priority順にソート
        adjustedCategories.sort(Comparator.comparingInt(CategoryData::priority));

        for (CategoryData category : adjustedCategories) {
            double roll = random.nextDouble() * 100.0;  // 0.0から100.0の範囲で乱数を生成
              // 補正されたchanceを使用
            if (roll < category.chance()) {
                return category.name();
            }
        }

        return "common";
    }
    
    /**
     * 確率補正を計算（Minecraft方式）
     * @param baseChance 基本確率
     * @param quality 品質値
     * @param totalLuck 総幸運値
     * @return 補正後の確率
     */
    public static double calculateAdjustedChance(double baseChance, double quality, double totalLuck) {
        return Math.floor(baseChance + (quality * totalLuck));
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

        // 経験値ボーナス
        if (luckResult.experienceLevel() > 0) {
            double experienceBonus = Math.min(100, luckResult.experienceLevel()) * 0.01;
            bonusText.append(" &e経験値+").append(String.format("%.2f%%", experienceBonus));
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

        return probabilityText + bonusText;
    }

    /**
     * タイミングをチェック
     *
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

    /**
     * 釣り処理を実行し、結果を返す
     */
    private FishingResult processFishing(Player player, Item itemEntity, Location hookLocation, boolean isDoubleFishingBonus, TimingResult timingResult) {

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

        if (isDoubleFishingBonus) {
            debugLogger.logInfo("=== DOUBLE FISHING: BONUS FISHING PROCESS ===");
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
     * ダブルフィッシング時の両方の結果を同時に表示
     */
    private void displayDoubleFishingResults(Player player, FishingResult first, FishingResult second) {
        // エフェクトは両方のカテゴリで最もレアな方を使用
        String primaryCategory = getHigherPriorityCategory(first.category(), second.category());

        // カスタムメッセージ表示とエフェクト実行
        catchEffects.playDoubleFishingEffects(player, primaryCategory, first, second);
    }

    /**
     * ダブルフィッシング条件をチェック
     *
     * @return 宝釣りLv10以上かつコンジットパワーLv2以上の場合true
     */
    private boolean checkDoubleFishingConditions(Player player) {
        // コンジットパワーのレベルをチェック
        boolean hasConduitPower = player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
        int conduitLevel = hasConduitPower ?
                Objects.requireNonNull(player.getPotionEffect(PotionEffectType.CONDUIT_POWER)).getAmplifier() + 1 : 0;

        // 宝釣りエンチャントレベルを取得
        ItemStack fishingRod = player.getInventory().getItemInMainHand();
        int luckOfSeaLevel = fishingRod.getEnchantmentLevel(Enchantment.LUCK);

        // 両方の条件を満たしているかチェック
        return luckOfSeaLevel >= 10 && conduitLevel >= 2;
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

    private record CategoryData(String name, int priority, double quality, double chance) {
    }

    /**
     * 釣り結果を保存するレコード
     */
    public record FishingResult(String category, String probabilityInfo, ItemStack item) {
    }

}