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
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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

        // タイミング判定とメッセージ表示
        TimingResult timingResult = checkTiming(player);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean isOpenWater = FishingConditionChecker.isOpenWater(hookLocation);

        String weather = "clear";
        if (Objects.requireNonNull(hookLocation.getWorld()).isThundering()) {
            weather = "thunder";
        } else if (hookLocation.getWorld().hasStorm()) {
            weather = "rain";
        }

        boolean hasDolphinsGrace = player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);
        
        // LuckCalculatorで全ての幸運値を計算
        LuckCalculator luckCalc = new LuckCalculator(plugin);
        LuckResult luckResult = luckCalc.calculateTotalLuck(player, weather, timingResult);
        
        if (plugin.getConfig().getBoolean("debug", false)) {
            String timingInfo = timingResult.hasTiming() ? 
                timingResult.tier().name() + "(" + timingResult.reactionTimeMs() + "ms)" : "MISS";
            plugin.getLogger().info(String.format(
                    " Player %s - OpenWater:%s Weather:%s Dolphins:%s Timing:%s",
                    player.getName(), isOpenWater, weather, hasDolphinsGrace, timingInfo
            ));
        }
        String forcedCategory = null;
        if (DebugFishingRod.isDebugRod(plugin, mainHand)) {
            forcedCategory = DebugFishingRod.getForcedCategory(plugin, mainHand);
        }
        String category;
        if (forcedCategory != null) {
            category = forcedCategory;
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info(" Using debug rod - forced category: " + category);
            }
        } else {
            category = determineCategoryFromConfig(luckResult.totalLuck(), isOpenWater, weather, hasDolphinsGrace);
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info(" Selected category: " + category);
            }
        }

        String namespace = plugin.getConfig().getString("loot_tables.namespace", "customize_fishing");
        String path = plugin.getConfig().getString("loot_tables.path", "gameplay/fishing");
        NamespacedKey lootTableKey = NamespacedKey.fromString(namespace + ":" + path + "/" + category);
        LootTable lootTable = plugin.getServer().getLootTable(Objects.requireNonNull(lootTableKey));

        ItemStack originalItem = itemEntity.getItemStack().clone();

        if (lootTable != null) {
            LootContext.Builder contextBuilder = new LootContext.Builder(hookLocation)
                    .killer(player)
                    .lootedEntity(itemEntity)
                    .luck((float) luckResult.totalLuck());

            LootContext lootContext = contextBuilder.build();
            Collection<ItemStack> loot = lootTable.populateLoot(random, lootContext);

            if (!loot.isEmpty()) {
                ItemStack selectedItem = loot.iterator().next();

                // イルカの好意カテゴリでプレイヤーヘッドの場合、釣り人の顔に置換
                selectedItem = PlayerHeadProcessor.processPlayerHead(selectedItem, player, category);

                itemEntity.setItemStack(selectedItem);

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info(String.format(
                            " Replaced %s with %s from loot table %s",
                            getItemDisplayName(originalItem), getItemDisplayName(selectedItem), lootTableKey
                    ));
                }
            }
        } else {
            // ルートテーブルが見つからない場合はエラー出力
            plugin.getLogger().severe("[ERROR] Loot table not found: " + lootTableKey);
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info(" Loot table not found for player " + player.getName() + ": " + lootTableKey);
            }
        }

        // レア度別演出を実行
        catchEffects.playCatchEffects(player, category);
    }

    /**
     * カテゴリを設定ファイルから決定するメソッド
     * @param totalLuck 合計幸運値
     * @param openWater 開水域かどうか
     * @param weather 天気
     * @param dolphinsGrace イルカの好意エフェクトがあるか
     * @return 選択されたカテゴリ
     */
    private String determineCategoryFromConfig(double totalLuck, boolean openWater, String weather, boolean dolphinsGrace) {
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) {
            return "common";
        }

        List<CategoryData> categories = new ArrayList<>();

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);
            if (categorySection == null || !categorySection.getBoolean("enabled", true)) {
                continue;
            }

            ConfigurationSection conditionsSection = categorySection.getConfigurationSection("conditions");
            if (conditionsSection == null) {
                continue;
            }

            boolean requireOpenWater = conditionsSection.getBoolean("require_open_water", false);
            if (requireOpenWater && !openWater) {
                continue;
            }


            boolean requireDolphinsGrace = conditionsSection.getBoolean("require_dolphins_grace", false);
            if (requireDolphinsGrace && !dolphinsGrace) {
                continue;
            }

            List<String> allowedWeather = conditionsSection.getStringList("weather");
            if (!allowedWeather.isEmpty() && !allowedWeather.contains(weather)) {
                continue;
            }

            int priority = categorySection.getInt("priority", 999);
            double quality = categorySection.getDouble("quality", 0);
            double chance = categorySection.getDouble("chance", 100.0);

            categories.add(new CategoryData(categoryName, priority, quality, chance));
        }

        if (categories.isEmpty()) {
            return "common";
        }


        // qualityを使用して各カテゴリのchanceを補正
        List<CategoryData> adjustedCategories = new ArrayList<>();
        double totalWeight = 0;

        for (CategoryData category : categories) {
            // Minecraft方式: floor(chance + (quality × luck))
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

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info(String.format(
                        " Category: %s, Priority: %d, Quality: %.1f, Base Chance: %.2f%%, Adjusted Chance: %.0f, Total Luck: %.1f",
                        category.name(), category.priority(), category.quality(), category.chance(), adjustedChance, totalLuck
                ));
            }
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
                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info(String.format(
                            " Selected: %s (roll: %.2f < %.2f)",
                            category.name(), roll, currentWeight
                    ));
                }
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
     * タイミングをチェックし、メッセージを表示する
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
        TimingResult result = luckCalc.calculateTimingResult(reactionTime);
        
        // タイミング成功時にサブタイトルを表示
        if (result.hasTiming()) {
            String message = plugin.getConfig().getString(
                "effects.action_bar_messages." + result.tier().name(),
                result.tier().message()
            );
            // サブタイトルとして右側に表示（空のタイトルと一緒に）
            player.sendTitle("", message.replace("&", "§"), 10, 40, 10);
        }
        
        if (plugin.getConfig().getBoolean("debug", false)) {
            String tierName = result.hasTiming() ? result.tier().name() : "MISS";
            plugin.getLogger().info(String.format(
                    " Reaction time: %dms - Timing tier: %s",
                    reactionTime, tierName
            ));
        }
        
        return result;
    }

    private record CategoryData(String name, int priority, double quality, double chance) {
    }
        
}