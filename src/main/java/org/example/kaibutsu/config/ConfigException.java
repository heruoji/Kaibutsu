package org.example.kaibutsu.config;

public class ConfigException extends RuntimeException{

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
