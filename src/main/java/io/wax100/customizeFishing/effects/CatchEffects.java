package io.wax100.customizeFishing.effects;

import io.wax100.customizeFishing.CustomizeFishing;
import io.wax100.customizeFishing.listeners.FishingListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Objects;

public class CatchEffects {

    private final CustomizeFishing plugin;

    public CatchEffects(CustomizeFishing plugin) {
        this.plugin = plugin;
    }

    /**
     * 確率情報を中央から広がるアニメーションで表示
     *
     * @return アニメーション完了までの総tick数
     */
    private long animateProbabilityInfo(Player player, String fullText, long initialDelay) {
        String cleanText = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', fullText));
        int textLength = cleanText.length();
        int center = textLength / 2;

        // アニメーションのステップ数
        int steps = Math.min(15, center); // 最大15ステップ

        for (int i = 0; i <= steps; i++) {
            final int step = i;
            long delay = initialDelay + (i * 2L); // 各ステップ2tick間隔

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String displayText = buildAnimatedText(fullText, cleanText, center, step, steps);
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', displayText)));
            }, delay);
        }

        // アニメーション完了までの総tick数を返す
        return initialDelay + (steps * 2L);
    }

    /**
     * アニメーションの各ステップで表示するテキストを構築
     */
    private String buildAnimatedText(String fullText, String cleanText, int center, int step, int maxSteps) {
        if (step == maxSteps) {
            return fullText; // 最後のステップでは完全なテキストを表示
        }

        // 中央からの距離を計算
        float progress = (float) step / maxSteps;
        int revealRadius = (int) (center * progress);

        // 表示範囲を計算
        int startIndex = Math.max(0, center - revealRadius);
        int endIndex = Math.min(cleanText.length(), center + revealRadius);

        // 部分的に表示するテキストを構築
        StringBuilder result = new StringBuilder();

        // 左側の空白
        result.append(" ".repeat(startIndex));

        // 中央部分のテキスト（フェードイン効果付き）
        if (endIndex > startIndex) {
            String visiblePart = fullText.substring(
                    getOriginalIndex(fullText, startIndex),
                    getOriginalIndex(fullText, endIndex)
            );

            // エッジ部分にフェード効果を追加
            if (step < maxSteps - 1) {
                result.append("&7").append(visiblePart).append("&f");
            } else {
                result.append(visiblePart);
            }
        }

        // 右側の空白
        result.append(" ".repeat(Math.max(0, cleanText.length() - endIndex)));

        return result.toString();
    }

    /**
     * 色コードを含む文字列での実際のインデックスを取得
     */
    private int getOriginalIndex(String coloredText, int cleanIndex) {
        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', coloredText));
        if (cleanIndex >= stripped.length()) {
            return coloredText.length();
        }

        int currentCleanPos = 0;
        for (int i = 0; i < coloredText.length(); i++) {
            if (currentCleanPos == cleanIndex) {
                return i;
            }

            // 色コードをスキップ
            if (i < coloredText.length() - 1 && coloredText.charAt(i) == '&') {
                i++; // 色コードの次の文字もスキップ
            } else {
                currentCleanPos++;
            }
        }

        return coloredText.length();
    }

    public void playCatchEffects(Player player, String category, String probabilityInfo) {
        // アクションバー表示
        displayActionBarMessage(player, category, probabilityInfo);

        // エフェクト実行
        executeEffects(player, category);

        // 全体通知
        sendBroadcastAnnouncement(player, category);
    }

    /**
     * アクションバーメッセージを表示（通常の釣り用）
     */
    private void displayActionBarMessage(Player player, String category, String probabilityInfo) {
        // カテゴリメッセージを取得・表示
        String categoryMessage = getCategoryMessage(player, category);
        sendActionBarMessage(player, categoryMessage);

        // 確率情報をアニメーション表示
        if (probabilityInfo != null && !probabilityInfo.isEmpty()) {
            animateProbabilityInfo(player, probabilityInfo, 30L);
        }
    }

    /**
     * カテゴリメッセージを取得
     */
    private String getCategoryMessage(Player player, String category) {
        String actionBarKey = "effects.action_bar_messages." + category;
        String defaultMessage = "&6&l" + category + "アイテムを釣り上げました！";
        String message = plugin.getConfig().getString(actionBarKey, defaultMessage);
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", player.getName()));
    }

    /**
     * アクションバーにメッセージを送信
     */
    private void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * 全エフェクトを実行
     */
    private void executeEffects(Player player, String category) {
        playParticleEffects(player, category);
        playPotionEffects(player, category);
        launchFirework(player, category);
        playSound(player, category);
    }

    private void playParticleEffects(Player player, String category) {
        if (!plugin.getConfig().getBoolean("effects.particles.enabled", true)) {
            return;
        }

        ConfigurationSection particleSection = plugin.getConfig().getConfigurationSection("effects.particles." + category);
        if (particleSection == null) {
            return;
        }

        Location loc = player.getLocation();

        // Primary particle
        String primaryName = particleSection.getString("primary");
        if (primaryName != null) {
            try {
                Particle primary = Particle.valueOf(primaryName);
                int primaryCount = particleSection.getInt("primary_count", 30);
                double spread = particleSection.getDouble("spread", 1.5);
                spawnParticles(loc, primary, primaryCount, spread);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type: " + primaryName);
            }
        }

        // Secondary particle
        String secondaryName = particleSection.getString("secondary");
        if (secondaryName != null) {
            try {
                Particle secondary = Particle.valueOf(secondaryName);
                int secondaryCount = particleSection.getInt("secondary_count", 20);
                double spread = particleSection.getDouble("spread", 1.5);
                spawnParticles(loc, secondary, secondaryCount, spread);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type: " + secondaryName);
            }
        }
    }

    private void playPotionEffects(Player player, String category) {
        if (!plugin.getConfig().getBoolean("effects.potion_effects.enabled", true)) {
            return;
        }

        ConfigurationSection potionSection = plugin.getConfig().getConfigurationSection("effects.potion_effects." + category);
        if (potionSection == null) {
            return;
        }

        String effectName = potionSection.getString("effect");
        if (effectName != null) {
            try {
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    int duration = potionSection.getInt("duration", 60);
                    int amplifier = potionSection.getInt("amplifier", 0);
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid potion effect: " + effectName);
            }
        }
    }

    private void spawnParticles(Location location, Particle particle, int count, double spread) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(particle,
                location.add(0, 1, 0),
                count,
                spread, spread, spread,
                0.1
        );
    }

    private void launchFirework(Player player, String category) {
        if (!plugin.getConfig().getBoolean("effects.fireworks.enabled", true)) {
            return;
        }

        ConfigurationSection fireworkSection = plugin.getConfig().getConfigurationSection("effects.fireworks." + category);
        if (fireworkSection == null) {
            return;
        }

        Location loc = player.getLocation().add(0, 1, 0);
        Firework firework = Objects.requireNonNull(loc.getWorld()).spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect.Builder builder = FireworkEffect.builder();

        // Colors
        List<String> colorNames = fireworkSection.getStringList("colors");
        if (!colorNames.isEmpty()) {
            Color[] colors = colorNames.stream()
                    .map(this::parseColor)
                    .filter(Objects::nonNull)
                    .toArray(Color[]::new);
            if (colors.length > 0) {
                builder.withColor(colors);
            }
        }

        // Fade colors
        List<String> fadeColorNames = fireworkSection.getStringList("fade_colors");
        if (!fadeColorNames.isEmpty()) {
            Color[] fadeColors = fadeColorNames.stream()
                    .map(this::parseColor)
                    .filter(Objects::nonNull)
                    .toArray(Color[]::new);
            if (fadeColors.length > 0) {
                builder.withFade(fadeColors);
            }
        }

        // Type
        String typeName = fireworkSection.getString("type", "BALL");
        try {
            FireworkEffect.Type type = FireworkEffect.Type.valueOf(typeName);
            builder.with(type);
        } catch (IllegalArgumentException e) {
            builder.with(FireworkEffect.Type.BALL);
        }

        // Flicker and trail
        if (fireworkSection.getBoolean("flicker", false)) {
            builder.withFlicker();
        }
        if (fireworkSection.getBoolean("trail", false)) {
            builder.withTrail();
        }

        meta.addEffect(builder.build());
        meta.setPower(fireworkSection.getInt("power", 1));
        firework.setFireworkMeta(meta);
    }

    private Color parseColor(String colorName) {
        try {
            return switch (colorName.toUpperCase()) {
                case "AQUA" -> Color.AQUA;
                case "BLACK" -> Color.BLACK;
                case "BLUE" -> Color.BLUE;
                case "FUCHSIA" -> Color.FUCHSIA;
                case "GRAY" -> Color.GRAY;
                case "GREEN" -> Color.GREEN;
                case "LIME" -> Color.LIME;
                case "MAROON" -> Color.MAROON;
                case "NAVY" -> Color.NAVY;
                case "OLIVE" -> Color.OLIVE;
                case "ORANGE" -> Color.ORANGE;
                case "PURPLE" -> Color.PURPLE;
                case "RED" -> Color.RED;
                case "SILVER" -> Color.SILVER;
                case "TEAL" -> Color.TEAL;
                case "WHITE" -> Color.WHITE;
                case "YELLOW" -> Color.YELLOW;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private void playSound(Player player, String category) {
        if (!plugin.getConfig().getBoolean("effects.sounds.enabled", true)) {
            return;
        }

        ConfigurationSection soundSection = plugin.getConfig().getConfigurationSection("effects.sounds." + category);
        if (soundSection == null) {
            return;
        }

        String soundName = soundSection.getString("sound");
        if (soundName != null) {
            try {
                Sound sound = Sound.valueOf(soundName);
                float volume = (float) soundSection.getDouble("volume", 1.0);
                float pitch = (float) soundSection.getDouble("pitch", 1.0);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }
    }

    private boolean shouldBroadcastCategory(String category) {
        String broadcastKey = "effects.broadcast_categories." + category.toLowerCase();
        return plugin.getConfig().getBoolean(broadcastKey, false);
    }

    /**
     * ダブルフィッシング用の特別なエフェクト表示
     */
    public void playDoubleFishingEffects(Player player, String primaryCategory, FishingListener.FishingResult firstResult, FishingListener.FishingResult secondResult) {
        // FishingResultから直接データを取得
        String firstCategory = firstResult.category();
        String secondCategory = secondResult.category();
        String firstProb = firstResult.probabilityInfo();
        String secondProb = secondResult.probabilityInfo();

        // アクションバー表示
        displayDoubleFishingProbability(player, firstCategory, firstProb, secondCategory, secondProb);

        // エフェクト実行
        executeEffects(player, primaryCategory);

        // 全体通知（各カテゴリ個別に）
        sendBroadcastAnnouncement(player, firstCategory);
        sendBroadcastAnnouncement(player, secondCategory);
    }

    /**
     * ダブルフィッシング用の確率表示（アクションバー専用）
     */
    private void displayDoubleFishingProbability(Player player, String firstCategory, String firstProb, String secondCategory, String secondProb) {
        // カテゴリメッセージを表示（ダブルフィッシング）
        String categoryMessage = String.format("&b&l✨ &6&l%s &7| &6&l%s &b&l✨",
                firstCategory.toUpperCase(), secondCategory.toUpperCase());
        sendActionBarMessage(player, categoryMessage);

        // 確率情報を2回に分けてアニメーション表示
        long firstAnimationDelay = 30L;
        long firstAnimationDuration = 0L;

        if (firstProb != null && !firstProb.isEmpty()) {
            firstAnimationDuration = animateProbabilityInfo(player, "&7" + firstProb, firstAnimationDelay);
        }

        if (secondProb != null && !secondProb.isEmpty()) {
            // 1つ目のアニメーション完了後に少し間を空けて2つ目を表示
            long secondAnimationDelay = firstAnimationDuration + 10L; // 10tick（0.5秒）の間隔
            animateProbabilityInfo(player, "&7" + secondProb, secondAnimationDelay);
        }
    }

    /**
     * 全体通知のみを送信
     */
    private void sendBroadcastAnnouncement(Player player, String category) {
        if (shouldBroadcastCategory(category)) {
            String broadcastKey = "effects.announcements." + category;
            String defaultBroadcastMessage = "&6" + player.getName() + "&eが&f" + category + "&eアイテムを釣り上げました！";
            String broadcastMessage = plugin.getConfig().getString(broadcastKey, defaultBroadcastMessage);
            String formattedBroadcastMessage = ChatColor.translateAlternateColorCodes('&', broadcastMessage.replace("%player%", player.getName()));

            // 全体通知
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(formattedBroadcastMessage);
            }
        }
    }
}