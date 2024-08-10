package org.example.kaibutsu.application.config;

import java.util.Properties;

public class Config {
    public String parser;
    public String[] itemPipelines;
    public boolean dynamic;
    public int intervalMillSeconds;
    public String parserPackage;
    public String itemPipelinesPackage;

    public static Config loadFromProperties(Properties properties) {
        Config config = new Config();

        String parser = properties.getProperty("parser");
        if (parser == null || parser.trim().isEmpty()) {
            throw new ConfigException("parserの指定は必須です。");
        }
        config.parser = parser;
        config.dynamic = "true".equals(properties.getProperty("dynamic", "false"));
        config.itemPipelines = properties.getProperty("itemPipelines").split(",");
        config.intervalMillSeconds = Integer.parseInt(properties.getProperty("interval", "1000"));
        config.parserPackage = properties.getProperty("parserPackage");
        config.itemPipelinesPackage = properties.getProperty("itemPipelinesPackage");
        return config;
    }
}
