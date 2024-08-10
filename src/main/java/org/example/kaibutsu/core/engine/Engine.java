package org.example.kaibutsu.core.engine;

import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.downloader.DownloaderException;
import org.example.kaibutsu.core.downloader.DownloaderRequest;
import org.example.kaibutsu.core.downloader.DownloaderResponse;
import org.example.kaibutsu.core.itempipeline.ItemPipeline;
import org.example.kaibutsu.core.scheduler.Scheduler;
import org.example.kaibutsu.core.parser.Item;
import org.example.kaibutsu.core.parser.Parser;
import org.example.kaibutsu.core.parser.ParserException;
import org.example.kaibutsu.core.parser.ParserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    private final Scheduler scheduler;
    private final Downloader downloader;
    private final Parser parser;
    private final List<ItemPipeline> itemPipelines;
    private volatile boolean isRunning = true;
    private final Set<DownloaderRequest> inProgress = ConcurrentHashMap.newKeySet();
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final Set<String> errorUrls = ConcurrentHashMap.newKeySet();
    private final CountDownLatch latch = new CountDownLatch(1);

    public Engine(Scheduler scheduler, Downloader downloader, Parser parser, List<ItemPipeline> itemPipelines) {
        this.scheduler = scheduler;
        this.downloader = downloader;
        this.parser = parser;
        this.itemPipelines = itemPipelines;
        initializeItemPipeline();
    }

    public void run() throws InterruptedException {
        addRequestToScheduler(parser.startRequest());
        crawl();
        waitCompletion();
    }

    private void addRequestToScheduler(DownloaderRequest downloaderRequest) {
        inProgress.add(downloaderRequest);
        scheduler.addRequest(downloaderRequest);
    }

    private void crawl() {
        scheduler.requestStream()
                .flatMap(this::processRequest)
                .subscribe(this::onNext, this::onError, this::onComplete);
    }

    private Mono<DownloaderRequest> processRequest(DownloaderRequest downloaderRequest) {
        return download(downloaderRequest)
                .publishOn(Schedulers.parallel())
                .flatMap(this::parse)
                .doOnNext(this::addNewRequestsToScheduler)
                .flatMap(parserResponse -> processItemPipelines(parserResponse, downloaderRequest))
                .onErrorResume(e -> {
                    handleError(e, downloaderRequest);
                    return Mono.empty();
                })
                .doFinally(signalType -> {
                    inProgress.remove(downloaderRequest);
                });
    }

    private Mono<DownloaderResponse> download(DownloaderRequest downloaderRequest) {
        return downloader.download(downloaderRequest);
    }

    private Mono<ParserResponse> parse(DownloaderResponse downloaderResponse) {
        return parser.parse(downloaderResponse);
    }

    private void addNewRequestsToScheduler(ParserResponse parserResponse) {
        parserResponse.newDownloaderRequests.stream()
                .filter(newRequest -> !visitedUrls.contains(newRequest.getUrl()))
                .forEach(newRequest -> {
                    visitedUrls.add(newRequest.getUrl());
                    addRequestToScheduler(newRequest);
                });
    }

    private Mono<DownloaderRequest> processItemPipelines(ParserResponse parserResponse, DownloaderRequest downloaderRequest) {
        return Flux.fromIterable(parserResponse.items)
                .flatMap(this::processItemThroughPipeline)
                .then(Mono.just(downloaderRequest));
    }

    private Mono<Item> processItemThroughPipeline(Item item) {
        return Flux.fromIterable(itemPipelines)
                .flatMap(pipeline -> Mono.fromCallable(() -> pipeline.process(item)))
                .last();
    }

    private void initializeItemPipeline() {
        itemPipelines.forEach(ItemPipeline::open);
    }

    private void waitCompletion() throws InterruptedException {
        startShutdownMonitor();
        latch.await();
    }

    private void startShutdownMonitor() {
        Flux.interval(Duration.ofSeconds(1))
                .takeWhile(i -> isRunning)
                .subscribe(i -> {
                    if (inProgress.isEmpty() && scheduler.isEmpty()) {
                        isRunning = false;
                        scheduler.terminate();
                    }
                });
    }

    private void handleError(Throwable e, DownloaderRequest downloaderRequest) {
        if (e instanceof DownloaderException) {
            logError("Downloader", downloaderRequest.getUrl(), e);
        } else if (e instanceof ParserException) {
            logError("Parser", downloaderRequest.getUrl(), e);
        } else {
            logError("Processing", downloaderRequest.getUrl(), e);
        }
        errorUrls.add(downloaderRequest.getUrl());
    }

    private void logError(String component, String url, Throwable e) {
        logger.error("{} error for URL: {}", component, url, e);
    }

    private void onNext(DownloaderRequest downloaderRequest) {
        logger.info("次のURLのクロールが完了しました：" + downloaderRequest.getUrl());
    }

    private void onError(Throwable throwable) {
    }

    private void onComplete() {
        logger.info("全てのクロールが完了しました。");
        itemPipelines.forEach(ItemPipeline::close);
        downloader.close();
        latch.countDown();
    }
}