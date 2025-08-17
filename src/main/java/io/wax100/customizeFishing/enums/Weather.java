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
     * @return コンフィグキー
     */
    public String getConfigKey() {
        return configKey;
    }
    
    /**
     * コンフィグキーからWeatherを取得
     * @param configKey コンフィグキー
     * @return 対応するWeather、見つからない場合はCLEAR
     */
    public static Weather fromConfigKey(String configKey) {
        for (Weather weather : values()) {
            if (weather.configKey.equals(configKey)) {
                return weather;
            }
        }
        return CLEAR; // デフォルト
    }
}