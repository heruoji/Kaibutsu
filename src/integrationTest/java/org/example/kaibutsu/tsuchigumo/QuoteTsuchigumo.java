package org.example.kaibutsu.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;
import org.example.kaibutsu.core.tsuchigumo.TsuchigumoResponse;
import org.example.kaibutsu.magatama.Author;

import java.util.List;
import java.util.stream.Collectors;

public class QuoteTsuchigumo implements Tsuchigumo {
    public Request startRequest() {
        return new Request("https://quotes.toscrape.com", "parseMain");
    }

    public TsuchigumoResponse parseMain(Response response, TsuchigumoResponse.TsuchigumoResponseBuilder builder) {
        List<Request> authorRequests = response.select(".author + a").stream().map(link -> new Request(link.absUrl("href"), "parseAuthor")).collect(Collectors.toList());
        List<Request> paginationRequests = response.select("li.next a").stream().map(link -> new Request(link.absUrl("href"), "parseMain")).toList();
        authorRequests.addAll(paginationRequests);

        return builder.requests(authorRequests).build();
    }

    public TsuchigumoResponse parseAuthor(Response response, TsuchigumoResponse.TsuchigumoResponseBuilder builder) {
        String name = response.select("h3.author-title").text();
        String birthday = response.select(".author-born-date").text();
        String bio = response.select(".author-description").text();
        Author author = new Author();
        author.name = name;
        author.birthday = birthday;
        author.bio = bio;
        return builder.addMagatama(author).build();
    }
}
