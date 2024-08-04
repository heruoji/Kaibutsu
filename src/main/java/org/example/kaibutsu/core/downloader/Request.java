package org.example.kaibutsu.core.downloader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String url;
    private final String callbackMethodName;
    private String method;
    private Map<String, String> headers;
    private String encoding;
    private Map<String, String> options = new HashMap<>();

    public Request(String url, String callbackKey) {
        this.url = url;
        this.callbackMethodName = callbackKey;
        this.method = "GET";
        this.headers = null;
        this.encoding = StandardCharsets.UTF_8.name();
    }

    public Request(String url, String callbackKey, String method, Map<String, String> headers, String encoding) {
        this.url = url;
        this.callbackMethodName = callbackKey;
        this.method = method;
        this.headers = headers;
        this.encoding = encoding;
    }

    public Request cloneWithNewUrl(String newUrl) {
        return new Request(newUrl, this.callbackMethodName, this.method, this.headers, this.encoding);
    }

    public String getBaseUrl() {
        URI uri = null;
        try {
            uri = new URI(this.url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL syntax error", e);
        }
        return uri.getScheme() + "://" + uri.getHost();
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public String getUrl() {
        return url;
    }

    public String getCallbackMethodName() {
        return callbackMethodName;
    }
}
