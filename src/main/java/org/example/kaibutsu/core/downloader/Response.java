package org.example.kaibutsu.core.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Map;

public class Response {
    private final String url;
    private final byte[] body;
    private final Request request;

    public Response(String url, byte[] body, Request request) {
        this.url = url;
        this.body = body;
        this.request = request;
    }

    public Elements getJsoupElements(String query) {
        Document doc = Jsoup.parse(new String(body), request.getBaseUrl());
        return doc.select(query);
    }

    public Document getJsoupDocument() {
        return Jsoup.parse(new String(body), request.getBaseUrl());
    }

    public String getCallbackKey() {
        return this.request.getCallbackMethodName();
    }

    public String getUrl() {
        return this.url;
    }

    public Request getRequest() {
        return request;
    }
}
