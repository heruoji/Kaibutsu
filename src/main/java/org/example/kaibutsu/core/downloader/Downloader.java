package org.example.kaibutsu.core.downloader;

import reactor.core.publisher.Mono;

public interface Downloader {
    Mono<Response> download(Request request);

    void close();

}
