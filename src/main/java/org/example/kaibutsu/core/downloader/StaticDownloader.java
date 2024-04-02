package org.example.kaibutsu.core.downloader;

import org.example.kaibutsu.core.downloader.exception.ConnectionException;
import org.example.kaibutsu.core.downloader.exception.DownloaderException;
import org.example.kaibutsu.core.downloader.exception.HttpErrorException;
import org.example.kaibutsu.core.downloader.exception.TooManyRedirectsException;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class StaticDownloader implements Downloader {

    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_IN_MEMORY_SIZE = 1024 * 1024;

    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(MAX_IN_MEMORY_SIZE))
                    .build())
            .build();

    @Override
    public Mono<Response> download(Request request) {
        return download(request, 0);
    }

    private Mono<Response> download(Request request, int redirectCount) {
        return webClient.get()
                .uri(request.url)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.createException().flatMap(ex -> Mono.error(new HttpErrorException(request.url, clientResponse.statusCode().value(), ex)));
                    } else if (clientResponse.statusCode().is3xxRedirection()) {
                        String newUrl = clientResponse.headers().header("Location").stream().findFirst().orElse(null);
                        if (newUrl == null) {
                            return Mono.error(new HttpErrorException(request.url, clientResponse.statusCode().value(), new Exception("リダイレクトURLが見つかりません。")));
                        }
                        if (redirectCount >= MAX_REDIRECTS) {
                            return Mono.error(new TooManyRedirectsException(request.url));
                        }
                        request.url = newUrl;
                        return download(request, redirectCount + 1);
                    } else {
                        return clientResponse.bodyToMono(byte[].class)
                                .map(body -> new Response(
                                        request.url,
                                        clientResponse.statusCode().value(),
                                        clientResponse.headers().asHttpHeaders().toSingleValueMap(),
                                        body,
                                        request
                                ));
                    }
                })
                .onErrorMap(originalException -> handleException(originalException, request));
    }

    private Throwable handleException(Throwable originalException, Request request) {
        return switch (originalException) {
            case DownloaderException ignored -> originalException;
            case WebClientResponseException responseException ->
                    new HttpErrorException(request.url, responseException.getStatusCode().value(), originalException);
            case WebClientRequestException webClientRequestException ->
                    new ConnectionException(request.url, originalException);
            case null, default ->
                    new DownloaderException(request.url, "不明なエラーが発生しました。", originalException);
        };
    }


    @Override
    public void close() {
    }
}
