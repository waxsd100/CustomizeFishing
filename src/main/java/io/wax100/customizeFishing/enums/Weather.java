package io.wax100.customizeFishing.enums;

/**
 * 天気の種類を表すEnum
 */
public enum Weather {
    CLEAR("clear"),
    RAIN("rain"),
    THUNDER("thunder");

    private final String configKey;

    Weather(String configKey) {
        this.configKey = configKey;
    }


    /**
     * コンフィグファイルで使用されるキー名を取得
     *
     * @return コンフィグキー
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Bukkitの天気情報から対応するWeatherを取得
     *
     * @param hasStorm 嵐が発生しているか
     * @param isThundering 雷が鳴っているか
     * @return 対応するWeather
     */
    public static Weather fromBukkitWeather(boolean hasStorm, boolean isThundering) {
        if (isThundering) {
            return THUNDER;
        } else if (hasStorm) {
            return RAIN;
        } else {
            return CLEAR;
        }
    }
}