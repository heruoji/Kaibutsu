package org.example.kaibutsu.config;

import org.example.kaibutsu.config.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static Config load(String propertiesName) {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(propertiesName + ".properties")) {
            if (input == null) {
                throw new ConfigException("次の設定ファイルが見つかりません：" + propertiesName);
            }
            properties.load(input);
            return Config.loadFromProperties(properties);
        } catch (IOException e) {
            throw new ConfigException("設定ファイルの作成に失敗しました。", e);
        }
    }
}
