package org.example.kaibutsu.core.downloader.exception;

public class DownloaderException extends RuntimeException{
    private final String url;

    public DownloaderException(String url, String message, Throwable throwable) {
        super(message, throwable);
        this.url = url;
    }

    public DownloaderException(String url, String message) {
        super(message);
        this.url = url;
    }

}
