package org.example.kaibutsu.container;

public class ContainerException extends RuntimeException{

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
