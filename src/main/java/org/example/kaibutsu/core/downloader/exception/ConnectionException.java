package org.example.kaibutsu.core.downloader.exception;

public class ConnectionException extends DownloaderException {
    public ConnectionException(String url, Throwable throwable) {
        super(url, "接続エラーが発生しました。", throwable);
    }
}
