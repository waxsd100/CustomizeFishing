package io.wax100.customizeFishing.listeners;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.binding.BindingCurseManager;
import io.wax100.customizeFishing.debug.DebugFishingRod;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.effects.CatchEffects;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.fishing.FishingConditionChecker;
import io.wax100.customizeFishing.fishing.PlayerHeadProcessor;
import io.wax100.customizeFishing.luck.LuckCalculator;
import io.wax100.customizeFishing.luck.LuckResult;
import io.wax100.customizeFishing.timing.TimingResult;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
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
    private final BindingCurseManager bindingCurseManager;

    public FishingListener(CustomizeFishing plugin) {
        this.plugin = plugin;
        this.catchEffects = new CatchEffects(plugin);
        this.debugLogger = new DebugLogger(plugin);
        this.bindingCurseManager = new BindingCurseManager(plugin);
    }

    private String getProbabilityText(LuckResult luckResult, double baseChance, double quality) {
        double totalLuck = luckResult.getTotalLuck(plugin);

        // Minecraft方式での確率補正
        double adjustedChance = calculateAdjustedChance(baseChance, quality, totalLuck);

        // 確率情報を日本語でフォーマット
        String probabilityText;
        if (adjustedChance <= 0) {
            probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + "0%";
        } else if (adjustedChance >= 100) {
            probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + "100%";
        } else {
            // 非常に小さい確率の場合は科学的記法を使用
            String formattedChance = DebugLogger.formatProbabilityForDisplay(adjustedChance);

            if (adjustedChance == baseChance) {
                probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance;
            } else {
                double difference = adjustedChance - baseChance;
                String formattedDiff = DebugLogger.formatProbabilityForDisplay(Math.abs(difference));

                if (difference < 0) {
                    probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance +
                            " " + ChatColor.GRAY + "(補正値:" + ChatColor.RED + " -" + formattedDiff + ChatColor.GRAY + ")";
                } else {
                    probabilityText = ChatColor.GRAY + "確率: " + ChatColor.YELLOW + formattedChance +
                            " " + ChatColor.GRAY + "(補正値:" + ChatColor.GREEN + " +" + formattedDiff + ChatColor.GRAY + ")";
                }
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

        if (state == PlayerFishEvent.State.FISHING) {
            // 釣りを開始した時点でログ開始
            debugLogger.logFishingStart(player);
            return;
        }

        if (state == PlayerFishEvent.State.BITE) {
            // 釣れた瞬間の音効果を再生
            Objects.requireNonNull(hookLocation.getWorld()).playSound(hookLocation, Sound.BLOCK_LEVER_CLICK, 1.0f, 2.0f);
            biteTimestamps.put(player, System.currentTimeMillis());
            return;
        }

        if (state != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        if (!(event.getCaught() instanceof Item itemEntity)) {
            return;
        }

        TimingResult timingResult = checkTiming(player);

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

        // ダブルフィッシング条件をチェック
        boolean canDoubleFish = checkDoubleFishingConditions(player);

        // ダブルフィッシングが有効な場合、両方の結果を同時に処理
        if (canDoubleFish && plugin.getConfig().getBoolean("double_fishing.enabled", true)) {

            // 両方の結果を保存するための準備
            FishingResult firstResult = processFishing(player, itemEntity, hookLocation, false, timingResult, luckResult, isOpenWater, weather);

            // 新しいアイテムエンティティを作成
            ItemStack bonusItem = new ItemStack(Material.COD); // デフォルトアイテム
            Item bonusEntity = player.getWorld().dropItem(hookLocation, bonusItem);
            bonusEntity.setPickupDelay(Integer.MAX_VALUE); // 自動拾いを無効化

            FishingResult secondResult = processFishing(player, bonusEntity, hookLocation, true, timingResult, luckResult, isOpenWater, weather);

            // 両方の結果を同時に表示
            displayDoubleFishingResults(player, firstResult, secondResult);

            // ボーナスアイテムをプレイヤーの位置に移動
            bonusEntity.teleport(player.getLocation());
            bonusEntity.setPickupDelay(0); // 拾えるように戻す
        } else {
            FishingResult result = processFishing(player, itemEntity, hookLocation, false, timingResult, luckResult, isOpenWater, weather);

            // レア度別演出を実行
            catchEffects.playCatchEffects(player, result.category(), result.probabilityInfo());
        }

        // タイミング情報をウキに表示
        displayTimingAtHook(hookLocation, timingResult);

        // ログ終了は最後に実行
        debugLogger.logFishingEnd(player);
    }

    /**
     * カテゴリを設定ファイルから決定するメソッド
     *
     * @param openWater     開水域かどうか
     * @param weather       天気
     * @param dolphinsGrace イルカの好意エフェクトがあるか
     * @return 選択されたカテゴリ
     */
    private String determineCategoryFromConfig(Player player, LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return "common";
        }

        List<CategoryData> categories = new ArrayList<>();

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (!checkCategoryConditions(categorySection, luckResult, openWater, weather, dolphinsGrace)) {
                // 条件を満たさないカテゴリもログに出力（MISS）
                int priority = Objects.requireNonNull(categorySection).getInt("priority", 999);
                double quality = categorySection.getDouble("quality", 0);
                double chance = categorySection.getDouble("chance", 0);
                debugLogger.logCategoryDetails(
                        player, categoryName + " [MISS]", priority, quality,
                        chance, 0, luckResult.getTotalLuck(plugin)
                );
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

        double totalLuck = luckResult.getTotalLuck(plugin);

        // qualityを使用して各カテゴリのchanceを補正
        List<CategoryData> adjustedCategories = new ArrayList<>();

        for (CategoryData category : categories) {
            // Minecraft方式での確率補正
            double adjustedChance = calculateAdjustedChance(category.chance(), category.quality(), totalLuck);

            // 補正後のchanceが0以下の場合はスキップ
            if (adjustedChance <= 0) {
                debugLogger.logCategoryDetails(
                        player, category.name() + " [SKIP]", category.priority(), category.quality(),
                        category.chance(), adjustedChance, totalLuck
                );
                continue;
            }

            adjustedCategories.add(new CategoryData(category.name(), category.priority(), category.quality(), adjustedChance));

            debugLogger.logCategoryDetails(
                    player, category.name() + " [ELIGIBLE]", category.priority(), category.quality(),
                    category.chance(), adjustedChance, totalLuck
            );
        }

        if (adjustedCategories.isEmpty() || adjustedCategories.stream().allMatch(cat -> cat.chance() <= 0)) {
            return "common";
        }


        // priority順にソート（優先度の高い順）
        adjustedCategories.sort(Comparator.comparingInt(CategoryData::priority).reversed());

        // 総確率を計算
        double totalChance = adjustedCategories.stream()
                .mapToDouble(CategoryData::chance)
                .sum();

        // 一度だけ乱数を生成（0から総確率の範囲）
        double roll = random.nextDouble() * totalChance;

        // 選択結果をログ出力
        debugLogger.logInfo(player, String.format(" ROLL: %.2f / %.2f", roll, totalChance));

        // 累積確率で判定
        double cumulative = 0;
        String selectedCategory = null;
        for (CategoryData category : adjustedCategories) {
            cumulative += category.chance();
            if (selectedCategory == null && roll < cumulative) {
                selectedCategory = category.name();
                debugLogger.logInfo(player, String.format("   [HIT]  %s (%.2f - %.2f)",
                        category.name(), cumulative - category.chance(), cumulative));
            } else {
                debugLogger.logInfo(player, String.format("   [MISS] %s (%.2f - %.2f)",
                        category.name(), cumulative - category.chance(), cumulative));
            }
        }

        return selectedCategory != null ? selectedCategory : "common";
    }

    /**
     * 確率補正を計算（改良版：乗算型 + 対数減衰で極端な増加を防止）
     *
     * @param baseChance 基本確率
     * @param quality    品質値
     * @param totalLuck  総幸運値
     * @return 補正後の確率
     */
    private double calculateAdjustedChance(double baseChance, double quality, double totalLuck) {
        if (totalLuck == 0 || quality == 0) {
            return baseChance;
        }

        if (totalLuck > 0) {
            // 正の幸運時：乗算型 + 対数減衰
            if (quality > 0) {
                // config.ymlから設定を読み込み
                double maxMultiplier = plugin.getConfig().getDouble("luck_adjustment.max_multiplier", 3.0); // 最大3倍
                double luckScale = plugin.getConfig().getDouble("luck_adjustment.luck_scale", 0.1); // 運の影響度
                double qualityImpact = plugin.getConfig().getDouble("luck_adjustment.quality_impact", 0.5); // 品質の影響度

                // 対数減衰を使用して極端な増加を防止
                // log(1 + x)を使用することで、xが大きくなっても緩やかに増加
                double scaledLuck = Math.log1p(totalLuck * luckScale);
                double qualityFactor = Math.log1p(quality * qualityImpact);

                // 乗算倍率を計算（1.0 ～ maxMultiplier の範囲）
                double multiplier = 1.0 + (scaledLuck * qualityFactor);
                multiplier = Math.min(multiplier, maxMultiplier);

                return baseChance * multiplier;
            } else {
                // 負のqualityの場合は影響なし
                return baseChance;
            }
        } else {
            // 負の幸運時：確率を減少させる
            double penaltyScale = plugin.getConfig().getDouble("luck_adjustment.penalty_scale", 0.05);
            double penalty = Math.abs(totalLuck) * penaltyScale * quality;

            // 0%まで下がる可能性あり
            return Math.max(baseChance - penalty, 0.0);
        }
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
    private boolean checkCategoryConditions(ConfigurationSection categorySection, LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
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

        // 総合幸運値による最小値チェック
        double minTotalLuck = conditionsSection.getDouble("min_luck_effect", 0);
        if (luckResult.getTotalLuck(plugin) < minTotalLuck) {
            return false;
        }

        // 総合幸運値による最大値チェック
        if (conditionsSection.contains("max_luck_effect")) {
            double maxTotalLuck = conditionsSection.getDouble("max_luck_effect");
            if (luckResult.getTotalLuck(plugin) > maxTotalLuck) {
                return false;
            }
        }

        List<String> allowedWeather = conditionsSection.getStringList("weather");
        return allowedWeather.isEmpty() || allowedWeather.contains(weather.getConfigKey());
    }

    /**
     * 条件を満たすカテゴリ数を取得（デバッグ用）
     */
    private int getEligibleCategoryCount(LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
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
    private String calculateProbabilityInfo(String selectedCategory, LuckResult luckResult, Weather weather, TimingResult timingResult) {
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
        String bonusText = buildBonusText(luckResult, weather, timingResult);

        return probabilityText + bonusText;
    }

    /**
     * ボーナステキストを構築
     */
    private String buildBonusText(LuckResult luckResult, Weather weather, TimingResult timingResult) {
        return getLuckOfTheSeaBonusText(luckResult) +
                getLuckPotionBonusText(luckResult) +
                getEquipmentBonusText(luckResult) +
                getExperienceBonusText(luckResult) +
                getWeatherBonusText(luckResult, weather) +
                getTimingBonusText(luckResult, timingResult);
    }

    /**
     * 宝釣りエンチャントボーナステキストを取得
     */
    private String getLuckOfTheSeaBonusText(LuckResult luckResult) {
        if (luckResult.luckOfTheSeaLevel() <= 0) {
            return "";
        }
        double bonus = luckResult.getLuckOfTheSeaBonus(plugin);
        return " " + ChatColor.GREEN + "宝釣り+" + String.format("%.2f%%", bonus);
    }

    /**
     * 幸運ポーションボーナステキストを取得
     */
    private String getLuckPotionBonusText(LuckResult luckResult) {
        double bonus = luckResult.getLuckPotionBonus() + luckResult.getUnluckPotionPenalty(plugin);
        if (bonus == 0) {
            return "";
        }
        if (bonus > 0) {
            return " " + ChatColor.AQUA + "幸運+" + String.format("%.2f%%", bonus);
        } else {
            return " " + ChatColor.RED + "幸運" + String.format("%.2f%%", bonus);
        }
    }

    /**
     * 装備幸運ボーナステキストを取得
     */
    private String getEquipmentBonusText(LuckResult luckResult) {
        double bonus = luckResult.getEquipmentBonus(plugin);
        if (bonus == 0) {
            return "";
        }
        if (bonus > 0) {
            return " " + ChatColor.LIGHT_PURPLE + "装備+" + String.format("%.2f%%", bonus);
        } else {
            return " " + ChatColor.RED + "装備" + String.format("%.2f%%", bonus);
        }
    }

    /**
     * 経験値ボーナステキストを取得
     */
    private String getExperienceBonusText(LuckResult luckResult) {
        if (luckResult.experienceLevel() <= 0) {
            return "";
        }
        double bonus = luckResult.getExperienceBonus();
        return " " + ChatColor.YELLOW + "経験値+" + String.format("%.2f%%", bonus);
    }

    /**
     * 天気ボーナステキストを取得
     */
    private String getWeatherBonusText(LuckResult luckResult, Weather weather) {
        if (luckResult.weatherLuck() <= 0) {
            return "";
        }
        String weatherName = switch (weather) {
            case RAIN -> "雨";
            case THUNDER -> "雷雨";
            default -> weather.getConfigKey();
        };
        return " " + ChatColor.BLUE + weatherName + "+" + String.format("%.2f%%", luckResult.weatherLuck());
    }

    /**
     * タイミングボーナステキストを取得
     */
    private String getTimingBonusText(LuckResult luckResult, TimingResult timingResult) {
        if (timingResult == null || !timingResult.hasTiming() || luckResult.timingLuck() <= 0) {
            return "";
        }

        return " " + ChatColor.GOLD + "タイミング+" + String.format("%.2f%%", luckResult.timingLuck());
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
        LuckCalculator luckCalc = new LuckCalculator(plugin, debugLogger);
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
    private FishingResult processFishing(Player player, Item itemEntity, Location hookLocation, boolean isDoubleFishingBonus, TimingResult timingResult, LuckResult luckResult, boolean isOpenWater, Weather weather) {

        boolean hasDolphinsGrace = player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);

        if (isDoubleFishingBonus) {
            debugLogger.logInfo(player, "=== DOUBLE FISHING: BONUS FISHING PROCESS ===");
        }
        debugLogger.logFishingStart(player, isOpenWater, weather, hasDolphinsGrace, null);
        debugLogger.logTimingResult(player, timingResult.reactionTimeMs(), timingResult);
        debugLogger.logLuckBreakdown(player, luckResult);
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
            category = determineCategoryFromConfig(player, luckResult, isOpenWater, weather, hasDolphinsGrace);
        }

        // カテゴリ選択結果をログ出力
        int eligibleCount = getEligibleCategoryCount(luckResult, isOpenWater, weather, hasDolphinsGrace);
        debugLogger.logCategorySelection(player, category, eligibleCount);

        String namespace = plugin.getConfig().getString("loot_tables.namespace", "customize_fishing");
        String path = plugin.getConfig().getString("loot_tables.path", "gameplay/fishing");
        NamespacedKey lootTableKey = NamespacedKey.fromString(namespace + ":" + path + "/" + category);
        LootTable lootTable = plugin.getServer().getLootTable(Objects.requireNonNull(lootTableKey));

        ItemStack originalItem = itemEntity.getItemStack().clone();
        ItemStack selectedItem = originalItem;

        if (lootTable != null) {
            try {
                // LootContextの幸運値は-1024〜1024の範囲に制限し、適切にスケール
                float contextLuck = Math.max(-1024f, Math.min(1024f, (float) luckResult.getTotalLuck(plugin) * 10));

                LootContext.Builder contextBuilder = new LootContext.Builder(hookLocation)
                        .killer(player)
                        .lootedEntity(itemEntity)
                        .luck(contextLuck);

                LootContext lootContext = contextBuilder.build();
                Collection<ItemStack> loot = lootTable.populateLoot(random, lootContext);

                if (!loot.isEmpty()) {
                    selectedItem = loot.iterator().next();

                    // イルカの好意カテゴリでプレイヤーヘッドの場合、釣り人の顔に置換
                    selectedItem = PlayerHeadProcessor.processPlayerHead(selectedItem, player, category);

                    // アイテムが有効かチェック
                    if (selectedItem != null && selectedItem.getType() != Material.AIR && selectedItem.getAmount() > 0) {
                        // 束縛の呪いがある場合、所有者を設定
                        bindingCurseManager.setItemOwner(selectedItem, player);
                        itemEntity.setItemStack(selectedItem);

                        debugLogger.logItemReplacement(
                                player,
                                getItemDisplayName(originalItem),
                                getItemDisplayName(selectedItem),
                                lootTableKey.toString()
                        );
                    } else {
                        debugLogger.logInfo(player, "[ERROR] Selected item is invalid, keeping original item");
                        selectedItem = originalItem;
                    }
                } else {
                    debugLogger.logInfo(player, "[ERROR] Loot table returned empty results for category: " + category);
                }
            } catch (IllegalArgumentException e) {
                // LootContext に必要なパラメータが不足している場合は元のアイテムを使用
                debugLogger.logInfo(player, "[ERROR] Failed to populate loot table due to missing parameters: " + e.getMessage());
                debugLogger.logInfo(player, "Using original item instead");
            }
        } else {
            // ルートテーブルが見つからない場合はエラー出力
            debugLogger.logInfo(player, "[ERROR] Loot table not found: " + lootTableKey);
        }

        // 確率情報を計算
        String probabilityInfo = calculateProbabilityInfo(category, luckResult, weather, timingResult);

        // デバッグ: タイミング情報をログ出力
        if (timingResult.hasTiming()) {
            debugLogger.logInfo(player, String.format(" Timing Bonus Debug: tier=%s, luckBonus=%.2f",
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

    /**
     * タイミング情報をウキの位置にテキストディスプレイとして表示
     */
    private void displayTimingAtHook(Location hookLocation, TimingResult timingResult) {
        if (!timingResult.hasTiming()) {
            return;
        }

        String timingText = formatTimingText(timingResult);

        // テキストディスプレイを作成
        TextDisplay textDisplay = Objects.requireNonNull(hookLocation.getWorld()).spawn(
                hookLocation.clone().add(0, 1, 0),
                TextDisplay.class
        );

        textDisplay.setText(timingText);
        textDisplay.setBillboard(TextDisplay.Billboard.CENTER);

        // 3秒後に自動削除
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!textDisplay.isDead()) {
                textDisplay.remove();
            }
        }, 60L); // 3秒 = 60tick
    }

    /**
     * タイミング結果を表示用テキストにフォーマット
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

    private record CategoryData(String name, int priority, double quality, double chance) {
    }

    /**
     * 釣り結果を保存するレコード
     */
    public record FishingResult(String category, String probabilityInfo, ItemStack item) {
    }

}