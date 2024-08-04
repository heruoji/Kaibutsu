package org.example.kaibutsu.core.scheduler;

public class SchedulerException extends RuntimeException {
    public SchedulerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
