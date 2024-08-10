package org.example.kaibutsu.parser;

import org.example.kaibutsu.core.downloader.DownloaderRequest;
import org.example.kaibutsu.core.downloader.DownloaderResponse;
import org.example.kaibutsu.core.parser.Parser;
import org.example.kaibutsu.core.parser.ParserResponse;
import org.example.kaibutsu.item.Author;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuoteParser implements Parser {

    public DownloaderRequest startRequest() {
        return new DownloaderRequest("https://quotes.toscrape.com", "parseMain");
    }

    public ParserResponse parseMain(DownloaderResponse downloaderResponse) {
        List<DownloaderRequest> newDownloaderRequests = downloaderResponse.getJsoupElements(".author + a").stream().map(link -> new DownloaderRequest(link.absUrl("href"), "parseAuthor")).collect(Collectors.toList());
        newDownloaderRequests.addAll(downloaderResponse.getJsoupElements("li.next a").stream().map(link -> new DownloaderRequest(link.absUrl("href"), "parseMain")).toList());

        return ParserResponse.fromNewRequests(newDownloaderRequests);
    }

    public ParserResponse parseAuthor(DownloaderResponse downloaderResponse) {
        String name = downloaderResponse.getJsoupElements("h3.author-title").text();
        String birthday = downloaderResponse.getJsoupElements(".author-born-date").text();
        String bio = downloaderResponse.getJsoupElements(".author-description").text();
        Author author = new Author();
        author.name = name;
        author.birthday = birthday;
        author.bio = bio;
        return ParserResponse.fromItems(Collections.singletonList(author));
    }
}
