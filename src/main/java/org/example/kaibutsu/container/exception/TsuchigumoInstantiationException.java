package org.example.kaibutsu.container.exception;

public class TsuchigumoInstantiationException extends ContainerException {

    String tsuchigumoName;

    public TsuchigumoInstantiationException(String tsuchigumoName, String message, Throwable throwable) {
        super(message, throwable);
        this.tsuchigumoName = tsuchigumoName;
    }
}
