package org.example.kaibutsu.core.downloader;

import reactor.core.publisher.Mono;

public interface Downloader {
    Mono<DownloaderResponse> download(DownloaderRequest downloaderRequest);

    void close();

}
