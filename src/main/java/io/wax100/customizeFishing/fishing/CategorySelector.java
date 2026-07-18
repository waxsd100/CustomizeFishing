package io.wax100.customizeFishing.fishing;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.debug.DebugLogger;
import io.wax100.customizeFishing.enums.Weather;
import io.wax100.customizeFishing.luck.LuckResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CategorySelector {

    private final CustomizeFishing plugin;
    private final DebugLogger debugLogger;
    private final Random random;

    public CategorySelector(CustomizeFishing plugin, DebugLogger debugLogger) {
        this.plugin = plugin;
        this.debugLogger = debugLogger;
        this.random = new Random();
    }

    /**
     * 設定ファイルからカテゴリを決定する
     *
     * @param player        プレイヤー
     * @param luckResult    幸運計算結果
     * @param openWater     開水域かどうか
     * @param weather       天気
     * @param dolphinsGrace イルカの好意エフェクトがあるか
     * @return 選択されたカテゴリ
     */
    public String determineCategoryFromConfig(Player player, LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return null;
        }

        List<CategoryData> categories = new ArrayList<>();

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);

            if (!checkCategoryConditions(categorySection, luckResult, openWater, weather, dolphinsGrace)) {
                int priority = Objects.requireNonNull(categorySection).getInt("priority", 999);
                double quality = categorySection.getDouble("quality", 0);
                double chance = categorySection.getDouble("chance", 0);
                debugLogger.logCategoryDetails(
                        player, categoryName + " ✘", priority, quality,
                        chance, 0, luckResult.getTotalLuck(plugin)
                );
                continue;
            }

            int priority = categorySection.getInt("priority", 999);
            double quality = categorySection.getDouble("quality", 0);
            double chance = categorySection.getDouble("chance", 0);
            double minTotalLuck = categorySection.getDouble("conditions.min_total_luck", 0);

            categories.add(new CategoryData(categoryName, priority, quality, chance, minTotalLuck));
        }

        if (categories.isEmpty()) {
            return null;
        }

        double totalLuck = luckResult.getTotalLuck(plugin);
        LuckAdjustment luckAdjustment = LuckAdjustment.fromConfig(plugin);

        List<CategoryData> adjustedCategories = new ArrayList<>();

        for (CategoryData category : categories) {
            double adjustedChance = luckAdjustment.calculateAdjustedChance(
                    category.chance(), category.quality(), totalLuck, category.minTotalLuck());

            if (adjustedChance <= 0) {
                debugLogger.logCategoryDetails(
                        player, category.name() + " ✘", category.priority(), category.quality(),
                        category.chance(), adjustedChance, totalLuck
                );
                continue;
            }

            adjustedCategories.add(new CategoryData(category.name(), category.priority(), category.quality(), adjustedChance, category.minTotalLuck()));

            debugLogger.logCategoryDetails(
                    player, category.name() + " ✓", category.priority(), category.quality(),
                    category.chance(), adjustedChance, totalLuck
            );
        }

        if (adjustedCategories.isEmpty() || adjustedCategories.stream().allMatch(cat -> cat.chance() <= 0)) {
            return null;
        }

        adjustedCategories.sort(Comparator.comparingInt(CategoryData::priority).reversed());

        double totalChance = adjustedCategories.stream()
                .mapToDouble(CategoryData::chance)
                .sum();

        double roll = random.nextDouble() * totalChance;

        debugLogger.logInfo(player, String.format(" ROLL: %.2f / %.2f", roll, totalChance));

        double cumulative = 0;
        String selectedCategory = null;
        boolean hitFound = false;
        for (CategoryData category : adjustedCategories) {
            cumulative += category.chance();
            if (!hitFound && roll < cumulative) {
                selectedCategory = category.name();
                hitFound = true;
                debugLogger.logInfo(player, String.format("   [HIT]  %s (%.2f - %.2f)",
                        category.name(), cumulative - category.chance(), cumulative));
            } else if (!hitFound) {
                debugLogger.logInfo(player, String.format("   [MISS] %s (%.2f - %.2f)",
                        category.name(), cumulative - category.chance(), cumulative));
            } else {
                debugLogger.logInfo(player, String.format("   [SKIP] %s (%.2f - %.2f)",
                        category.name(), cumulative - category.chance(), cumulative));
            }
        }

        return selectedCategory;
    }

    /**
     * カテゴリの条件をチェックし、条件を満たすかを返す
     *
     * @param categorySection カテゴリの設定セクション
     * @param luckResult      幸運計算結果
     * @param openWater       開水域かどうか
     * @param weather         天気
     * @param dolphinsGrace   イルカの好意エフェクトがあるか
     * @return 条件を満たす場合true
     */
    public boolean checkCategoryConditions(ConfigurationSection categorySection, LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
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

        int minLuckOfTheSea = conditionsSection.getInt("min_luck_of_the_sea", 0);
        if (luckResult.luckOfTheSeaLevel() < minLuckOfTheSea) {
            return false;
        }

        if (conditionsSection.contains("max_luck_of_the_sea")) {
            int maxLuckOfTheSea = conditionsSection.getInt("max_luck_of_the_sea");
            if (luckResult.luckOfTheSeaLevel() > maxLuckOfTheSea) {
                return false;
            }
        }

        double minTotalLuck = conditionsSection.getDouble("min_total_luck", 0);
        if (luckResult.getTotalLuck(plugin) < minTotalLuck) {
            return false;
        }

        if (conditionsSection.contains("max_total_luck")) {
            double maxTotalLuck = conditionsSection.getDouble("max_total_luck");
            if (luckResult.getTotalLuck(plugin) > maxTotalLuck) {
                return false;
            }
        }

        List<String> allowedWeather = conditionsSection.getStringList("weather");
        return allowedWeather.isEmpty() || allowedWeather.contains(weather.getConfigKey());
    }

    /**
     * 条件を満たすカテゴリ数を取得（デバッグ用）
     *
     * @param luckResult    幸運計算結果
     * @param openWater     開水域かどうか
     * @param weather       天気
     * @param dolphinsGrace イルカの好意エフェクトがあるか
     * @return 条件を満たすカテゴリ数
     */
    public int getEligibleCategoryCount(LuckResult luckResult, boolean openWater, Weather weather, boolean dolphinsGrace) {
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
     * より優先度の高いカテゴリを返す
     *
     * @param cat1 カテゴリ1
     * @param cat2 カテゴリ2
     * @return 優先度の高いカテゴリ
     */
    public String getHigherPriorityCategory(String cat1, String cat2) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return cat1;
        }

        int priority1 = categoriesSection.getInt(cat1 + ".priority", 999);
        int priority2 = categoriesSection.getInt(cat2 + ".priority", 999);

        return priority1 <= priority2 ? cat1 : cat2;
    }

    /**
     * カテゴリ情報を保持するレコード
     *
     * @param name         カテゴリ名
     * @param priority     優先度
     * @param quality      品質値
     * @param chance       確率
     * @param minTotalLuck 解禁に必要な総幸運値（conditions.min_total_luck）
     */
    public record CategoryData(String name, int priority, double quality, double chance, double minTotalLuck) {
    }
}