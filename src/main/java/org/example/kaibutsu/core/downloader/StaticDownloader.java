package org.example.kaibutsu.core.downloader;

import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StaticDownloader implements Downloader {

    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_IN_MEMORY_SIZE = 1024 * 1024;

    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configure -> configure
                            .defaultCodecs()
                            .maxInMemorySize(MAX_IN_MEMORY_SIZE))
                    .build())
            .build();

    @Override
    public Mono<DownloaderResponse> download(DownloaderRequest downloaderRequest) {
        return download(downloaderRequest, 0);
    }

    private Mono<DownloaderResponse> download(DownloaderRequest downloaderRequest, int redirectCount) {
        return webClient.get()
                .uri(downloaderRequest.getUrl())
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.createException().flatMap(ex -> Mono.error(new DownloaderException("レスポンスのステータスコードが不正です。リクエスト：" + downloaderRequest, ex)));
                    } else if (clientResponse.statusCode().is3xxRedirection()) {
                        String newUrl = clientResponse.headers().header("Location").stream().findFirst().orElse(null);
                        if (newUrl == null) {
                            return Mono.error(new DownloaderException("リダイレクト先のURLが見つかりませんでした。リクエスト：" + downloaderRequest));
                        }
                        if (redirectCount >= MAX_REDIRECTS) {
                            return Mono.error(new DownloaderException("リダイレクトの上限回数に達しました。リクエスト：" + downloaderRequest));
                        }
                        DownloaderRequest newDownloaderRequest = downloaderRequest.cloneWithNewUrl(newUrl);
                        return download(newDownloaderRequest, redirectCount + 1);
                    } else {
                        return clientResponse.bodyToMono(byte[].class)
                                .map(body -> new DownloaderResponse(
                                        downloaderRequest.getUrl(),
                                        body,
                                        downloaderRequest
                                ));
                    }
                })
                .onErrorResume(e -> Mono.error(new DownloaderException("ダウンロード中にエラーが発生しました。リクエストURL：" + downloaderRequest.getUrl(), e)));
    }

    @Override
    public void close() {
    }
}
