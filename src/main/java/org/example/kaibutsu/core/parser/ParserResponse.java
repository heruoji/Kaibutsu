package org.example.kaibutsu.core.parser;

import org.example.kaibutsu.core.downloader.DownloaderRequest;

import java.util.Collections;
import java.util.List;

public class ParserResponse {

    public List<DownloaderRequest> newDownloaderRequests;
    public List<Item> items;

    public ParserResponse(List<DownloaderRequest> newDownloaderRequests, List<Item> items) {
        this.newDownloaderRequests = newDownloaderRequests;
        this.items = items;
    }

    public static ParserResponse fromNewRequests(List<DownloaderRequest> newDownloaderRequests) {
        return new ParserResponse(newDownloaderRequests, Collections.emptyList());
    }

    public static ParserResponse fromItems(List<Item> items) {
        return new ParserResponse(Collections.emptyList(), items);
    }
}
