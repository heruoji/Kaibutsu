package org.example.kaibutsu.core.downloader.exception;

public class HttpErrorException extends DownloaderException {
    private final int statusCode;

    public HttpErrorException(String url, int statusCode, Throwable throwable) {
        super(url, "HTTPエラーが発生しました。ステータスコード：" + statusCode, throwable);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
