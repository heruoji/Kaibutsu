package org.example.kaibutsu.core.downloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DownloaderRequest {
    private final String url;
    private final String callbackMethodName;
    private final String method;
    private final Map<String, String> headers;
    private final String encoding;

    public DownloaderRequest(String url, String callbackKey) {
        this.url = url;
        this.callbackMethodName = callbackKey;
        this.method = "GET";
        this.headers = null;
        this.encoding = StandardCharsets.UTF_8.name();
    }

    public DownloaderRequest(String url, String callbackKey, String method, Map<String, String> headers, String encoding) {
        this.url = url;
        this.callbackMethodName = callbackKey;
        this.method = method;
        this.headers = headers;
        this.encoding = encoding;
    }

    public DownloaderRequest cloneWithNewUrl(String newUrl) {
        return new DownloaderRequest(newUrl, this.callbackMethodName, this.method, this.headers, this.encoding);
    }

    public String getBaseUrl() {
        URI uri;
        try {
            uri = new URI(this.url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL syntax error", e);
        }
        return uri.getScheme() + "://" + uri.getHost();
    }

    public String getUrl() {
        return url;
    }

    public String getCallbackMethodName() {
        return callbackMethodName;
    }
}
