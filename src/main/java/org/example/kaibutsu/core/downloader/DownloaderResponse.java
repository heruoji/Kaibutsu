package org.example.kaibutsu.core.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DownloaderResponse {
    private final String url;
    private final byte[] body;
    private final DownloaderRequest downloaderRequest;

    public DownloaderResponse(String url, byte[] body, DownloaderRequest downloaderRequest) {
        this.url = url;
        this.body = body;
        this.downloaderRequest = downloaderRequest;
    }

    public Elements getJsoupElements(String query) {
        Document doc = Jsoup.parse(new String(body), downloaderRequest.getBaseUrl());
        return doc.select(query);
    }

    public Document getJsoupDocument() {
        return Jsoup.parse(new String(body), downloaderRequest.getBaseUrl());
    }

    public String getCallbackKey() {
        return this.downloaderRequest.getCallbackMethodName();
    }

    public String getUrl() {
        return this.url;
    }

    public DownloaderRequest getRequest() {
        return downloaderRequest;
    }
}
