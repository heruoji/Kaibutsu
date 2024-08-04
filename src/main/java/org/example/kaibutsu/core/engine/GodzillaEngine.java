package org.example.kaibutsu.core.engine;

import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.scheduler.Scheduler;
import org.example.kaibutsu.core.tsuchigumo.Magatama;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;
import org.example.kaibutsu.core.tsuchigumo.TsuchigumoResponse;
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

public class GodzillaEngine {
    private static final Logger logger = LoggerFactory.getLogger(GodzillaEngine.class);

    private final Scheduler scheduler;
    private final Downloader downloader;
    private final Tsuchigumo tsuchigumo;
    private final List<MagatamaPipeline> magatamaPipelines;

    private volatile boolean isRunning = true;
    private final Set<Request> inProgress = ConcurrentHashMap.newKeySet();
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final CountDownLatch latch = new CountDownLatch(1);


    public GodzillaEngine(Scheduler scheduler, Downloader downloader, Tsuchigumo tsuchigumo, List<MagatamaPipeline> magatamaPipelines) {
        this.scheduler = scheduler;
        this.downloader = downloader;
        this.tsuchigumo = tsuchigumo;
        this.magatamaPipelines = magatamaPipelines;
        initializeMagatamaPipeline();
    }

    public void run() throws InterruptedException {
        addRequestToScheduler(tsuchigumo.startRequest());
        startCrawl();
        waitCompletion();
    }

    private void startCrawl() {
        scheduler.requestStream()
                .flatMap(this::processRequest)
                .subscribe(this::onNext, this::onError, this::onComplete);
    }

    private Mono<Request> processRequest(Request request) {
        return download(request)
                .publishOn(Schedulers.parallel())
                .flatMap(this::parse)
                .doOnNext(this::addNewRequestsToScheduler)
                .flatMap(tsuchigumoResponse -> processMagatamaPipelines(tsuchigumoResponse).then(Mono.just(request)))
                .doFinally(signalType -> {
                    inProgress.remove(request);
                });
    }

    private Mono<Response> download(Request request) {
        return downloader.download(request)
                .onErrorContinue((throwable, o) -> logError("Downloader", request.getUrl(), throwable));
    }

    private Mono<TsuchigumoResponse> parse(Response response) {
        return tsuchigumo.parse(response)
                .doOnError(e -> logError("Tsuchigumo", response.getUrl(), e))
                .onErrorResume(e -> Mono.empty());
    }

    private void addNewRequestsToScheduler(TsuchigumoResponse tsuchigumoResponse) {
        tsuchigumoResponse.newRequests.stream()
                .filter(newRequest -> !visitedUrls.contains(newRequest.getUrl()))
                .forEach(newRequest -> {
                    visitedUrls.add(newRequest.getUrl());
                    addRequestToScheduler(newRequest);
                });
    }

    private Flux<Magatama> processMagatamaPipelines(TsuchigumoResponse tsuchigumoResponse) {
        return Flux.fromIterable(tsuchigumoResponse.magatamas)
                .flatMap(this::processMagatamaThroughPipeline);
    }

    private Mono<Magatama> processMagatamaThroughPipeline(Magatama magatama) {
        return Flux.fromIterable(magatamaPipelines)
                .flatMap(pipeline -> Mono.fromCallable(() -> pipeline.processMagatama(magatama)))
                .last();
    }

    private void addRequestToScheduler(Request request) {
        inProgress.add(request);
        scheduler.addRequest(request);
    }

    private void initializeMagatamaPipeline() {
        magatamaPipelines.forEach(MagatamaPipeline::open);
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

    private void onNext(Request request) {
        logger.info("次のクロールが完了しました：" + request.getUrl());
    }

    private void onError(Throwable throwable) {
        logger.error("エラーが発生しました。メッセージ：{}", throwable.getMessage(), throwable);
    }

    private void onComplete() {
        logger.info("全てのクロールが完了しました。");
        magatamaPipelines.forEach(MagatamaPipeline::close);
        downloader.close();
        latch.countDown();
    }

    private void logError(String component, String url, Throwable e) {
        logger.error("{} error for URL: {}, message: {}", component, url, e.getMessage(), e);
    }

}