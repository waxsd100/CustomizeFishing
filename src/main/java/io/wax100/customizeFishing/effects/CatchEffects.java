package io.wax100.customizeFishing.effects;

import io.wax100.customizeFishing.CustomizeFishing;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
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
    
    public void playCatchEffects(Player player, String category) {
        // お知らせメッセージ
        sendAnnouncement(player, category);
        
        // パーティクルエフェクト
        playParticleEffects(player, category);
        
        // ポーションエフェクト
        playPotionEffects(player, category);
        
        // 花火
        launchFirework(player, category);
        
        // サウンド
        playSound(player, category);
    }
    
    private void sendAnnouncement(Player player, String category) {
        // サブタイトル用メッセージ（個人通知）
        String actionBarKey = "effects.action_bar_messages." + category;
        String defaultActionBarMessage = "&6&l" + category + "アイテムを釣り上げました！";
        String actionBarMessage = plugin.getConfig().getString(actionBarKey, defaultActionBarMessage);
        String formattedActionBarMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage.replace("%player%", player.getName()));
        
        // 自分にはサブタイトルで表示（空のタイトルと一緒に）
        player.sendTitle("", formattedActionBarMessage, 10, 60, 20);
        
        // 全体通知用メッセージ（設定に基づいて通知）
        if (shouldBroadcastCategory(category)) {
            String broadcastKey = "effects.announcements." + category;
            String defaultBroadcastMessage = "&6" + player.getName() + "&eが&f" + category + "&eアイテムを釣り上げました！";
            String broadcastMessage = plugin.getConfig().getString(broadcastKey, defaultBroadcastMessage);
            String formattedBroadcastMessage = ChatColor.translateAlternateColorCodes('&', broadcastMessage.replace("%player%", player.getName()));
            
            // 全体通知（自分以外）
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player)) {
                    p.sendMessage(formattedBroadcastMessage);
                }
            }
        }
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
    
}