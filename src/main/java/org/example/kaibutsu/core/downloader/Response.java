package org.example.kaibutsu.core.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Map;

public class Response {
    public String url;
    public int status;
    public Map<String, String> headers;
    public byte[] body;
    public Request request;

    public Response(String url, int status, Map<String, String> headers, byte[] body, Request request) {
        this.url = url;
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.request = request;
    }

    public Elements select(String query) {
        Document doc = Jsoup.parse(new String(body), request.getBaseUrl());
        return doc.select(query);
    }

    public Document parse() {
        return Jsoup.parse(new String(body), request.getBaseUrl());
    }

    public String getOption(String key) {
        return this.request.getOption(key);
    }

    public String getCallbackKey() {
        return this.request.callbackMethodName;
    }
}
