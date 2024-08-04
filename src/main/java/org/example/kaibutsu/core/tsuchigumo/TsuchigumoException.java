package org.example.kaibutsu.core.tsuchigumo;

public class TsuchigumoException extends RuntimeException {
    public TsuchigumoException(String message) {
        super(message);
    }

    public TsuchigumoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
