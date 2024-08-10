package org.example.kaibutsu.core.parser;

public class ParserException extends RuntimeException {
    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
