package org.example.kaibutsu.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;
import org.example.kaibutsu.core.tsuchigumo.TsuchigumoResponse;
import org.example.kaibutsu.magatama.Author;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuoteTsuchigumo implements Tsuchigumo {
    public Request startRequest() {
        return new Request("https://quotes.toscrape.com", "parseMain");
    }

    public TsuchigumoResponse parseMain(Response response) {
        List<Request> newRequests = response.getJsoupElements(".author + a").stream().map(link -> new Request(link.absUrl("href"), "parseAuthor")).collect(Collectors.toList());
        newRequests.addAll(response.getJsoupElements("li.next a").stream().map(link -> new Request(link.absUrl("href"), "parseMain")).toList());

        return TsuchigumoResponse.fromNewRequests(newRequests);
    }

    public TsuchigumoResponse parseAuthor(Response response) {
        String name = response.getJsoupElements("h3.author-title").text();
        String birthday = response.getJsoupElements(".author-born-date").text();
        String bio = response.getJsoupElements(".author-description").text();
        Author author = new Author();
        author.name = name;
        author.birthday = birthday;
        author.bio = bio;
        return TsuchigumoResponse.fromMagatamas(Collections.singletonList(author));
    }
}
