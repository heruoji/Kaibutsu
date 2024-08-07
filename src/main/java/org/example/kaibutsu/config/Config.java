package org.example.kaibutsu.config;

import java.util.Properties;

public class Config {
    public String tsuchigumo;
    public String[] magatamaPipelines;
    public boolean dynamic;
    public int intervalMillSeconds;
    public String tsuchigumoPackage;
    public String magatamaPipelinesPackage;

    public static Config loadFromProperties(Properties properties) {
        Config config = new Config();

        String tsuchigumo = properties.getProperty("tsuchigumo");
        if (tsuchigumo == null || tsuchigumo.trim().isEmpty()) {
            throw new ConfigException("tsuchigumo設定は必須です。");
        }
        config.tsuchigumo = tsuchigumo;
        config.dynamic = "true".equals(properties.getProperty("dynamic", "false"));
        config.magatamaPipelines = properties.getProperty("magatamaPipelines").split(",");
        config.intervalMillSeconds = Integer.parseInt(properties.getProperty("interval", "1000"));
        config.tsuchigumoPackage = properties.getProperty("tsuchigumoPackage");
        config.magatamaPipelinesPackage = properties.getProperty("magatamaPipelinesPackage");
        return config;
    }
}
