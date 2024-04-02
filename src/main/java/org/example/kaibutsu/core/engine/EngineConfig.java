package org.example.kaibutsu.core.engine;

public class EngineConfig {
    public int interval;
    public int retryCount;

    public EngineConfig(int interval, int retryCount) {
        this.interval = interval;
        this.retryCount = retryCount;
    }
}
