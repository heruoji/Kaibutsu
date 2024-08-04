package org.example.kaibutsu.core.downloader;

public class DownloaderException extends RuntimeException{

    public DownloaderException(String message) {
        super(message);
    }

    public DownloaderException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
