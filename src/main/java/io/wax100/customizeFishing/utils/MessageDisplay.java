package io.wax100.customizeFishing.utils;

import io.wax100.customizeFishing.CustomizeFishing;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageDisplay {

    private final CustomizeFishing plugin;
    private final DisplayType displayType;

    public MessageDisplay(CustomizeFishing plugin) {
        this.plugin = plugin;
        String configType = plugin.getConfig().getString("message_display_type", "subtitle").toUpperCase();
        this.displayType = parseDisplayType(configType);
    }

    private DisplayType parseDisplayType(String type) {
        try {
            return DisplayType.valueOf(type);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid display type: " + type + ", using SUBTITLE as default");
            return DisplayType.SUBTITLE;
        }
    }

    /**
     * プレースホルダーを置換する
     *
     * @param message      メッセージ
     * @param placeholders プレースホルダーと値のマップ
     * @return 置換後のメッセージ
     */
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 色コードを変換する（&記号と§記号の両方に対応）
     *
     * @param message メッセージ
     * @return 色コード変換後のメッセージ
     */
    private String translateColorCodes(String message) {
        // まず&記号を変換
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * メッセージを整形して送信
     *
     * @param player       プレイヤー
     * @param message      メッセージ
     * @param placeholders プレースホルダー
     * @param fadeIn       フェードイン時間
     * @param stay         表示時間
     * @param fadeOut      フェードアウト時間
     */
    public void sendFormattedMessage(Player player, String message, Map<String, String> placeholders, int fadeIn, int stay, int fadeOut) {
        String processedMessage = message;

        // プレースホルダー置換
        if (placeholders != null && !placeholders.isEmpty()) {
            processedMessage = replacePlaceholders(processedMessage, placeholders);
        }

        // 色コード変換
        processedMessage = translateColorCodes(processedMessage);

        // 表示タイプに応じて送信
        switch (displayType) {
            case TITLE -> player.sendTitle(processedMessage, "", fadeIn, stay, fadeOut);
            case SUBTITLE -> player.sendTitle("", processedMessage, fadeIn, stay, fadeOut);
            case ACTION_BAR ->
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(processedMessage));
        }
    }

    public enum DisplayType {
        TITLE,
        SUBTITLE,
        ACTION_BAR
    }
}