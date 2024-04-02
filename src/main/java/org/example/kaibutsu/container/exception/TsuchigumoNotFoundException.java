package org.example.kaibutsu.container.exception;

public class TsuchigumoNotFoundException extends ContainerException {
    String tsuchigumoName;

    public TsuchigumoNotFoundException(String tsuchigumoName, String message) {
        super(message);
        this.tsuchigumoName = tsuchigumoName;
    }
}
