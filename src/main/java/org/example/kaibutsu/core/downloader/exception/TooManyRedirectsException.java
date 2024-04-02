package org.example.kaibutsu.core.downloader.exception;

public class TooManyRedirectsException extends DownloaderException {
    public TooManyRedirectsException(String url) {
        super(url, "リダイレクトの最大回数に達しました。");
    }
}