package org.example.kaibutsu.core.engine;

import org.example.kaibutsu.core.downloader.Downloader;
import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.downloader.exception.ConnectionException;
import org.example.kaibutsu.core.downloader.exception.HttpErrorException;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.scheduler.Scheduler;
import org.example.kaibutsu.core.tsuchigumo.Tsuchigumo;
import org.example.kaibutsu.core.tsuchigumo.TsuchigumoResponse;
import org.example.kaibutsu.core.tsuchigumo.Magatama;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class GodzillaEngine {
    private final Scheduler scheduler;
    private final Downloader downloader;
    private final Tsuchigumo tsuchigumo;
    private final List<MagatamaPipeline> magatamaPipelines;
    private final EngineConfig engineConfig;
    private volatile boolean isRunning = true;
    // ConcurrentHashMapのキーとしてRequestを使用し、値としては単にBoolean.TRUEを使用する
    private final ConcurrentHashMap<Request, Boolean> inProgress = new ConcurrentHashMap<>();

    private final CountDownLatch latch = new CountDownLatch(1);

    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    private static final Logger logger = LoggerFactory.getLogger(GodzillaEngine.class);

    public GodzillaEngine(Scheduler scheduler, Downloader downloader, Tsuchigumo tsuchigumo, List<MagatamaPipeline> magatamaPipelines, EngineConfig engineConfig) {
        this.scheduler = scheduler;
        this.downloader = downloader;
        this.tsuchigumo = tsuchigumo;
        this.magatamaPipelines = magatamaPipelines;
        this.engineConfig = engineConfig;
    }

    public void run() throws InterruptedException {
        initializeComponents();
        processRequests();
        waitCompletion();
    }

    private void initializeComponents() {
        initializeMagatamaPipeline();
        initializeScheduler();
    }

    private void initializeMagatamaPipeline() {
        magatamaPipelines.forEach(MagatamaPipeline::open);
    }

    private void initializeScheduler() {
        scheduler.setInProgress(inProgress);
        scheduler.addRequest(tsuchigumo.startRequest());
    }

    private void processRequests() {
        scheduler.requestStream()
                .delayElements(Duration.ofMillis(engineConfig.interval))
                .flatMap(this::download)
                .publishOn(Schedulers.parallel())
                .flatMap(this::parse)
                .doOnNext(this::addNewRequestsToScheduler)
                .flatMap(tsuchigumoResponse -> processMagatamaPipeline(tsuchigumoResponse)
                        .doFinally(signalType -> {
                            inProgress.remove(tsuchigumoResponse.getOriginalRequest());
                            logger.debug("Request processed and removed from inProgress: {}", tsuchigumoResponse.getOriginalRequest().url);
                        })
                )
                .subscribe(this::onNext, this::onError, this::onComplete);
    }

    private void waitCompletion() throws InterruptedException {
        startShutdownMonitor();
        latch.await();
    }

    private Mono<Response> download(Request request) {
        return downloader.download(request)
                .retryWhen(Retry.fixedDelay(engineConfig.retryCount, Duration.ofSeconds(2)).filter(this::isRetractable))
                .doOnError(e -> logError("Downloader", request.url, e))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<TsuchigumoResponse> parse(Response response) {
        return tsuchigumo.parse(response)
                .doOnError(e -> logError("Tsuchigumo", response.url, e))
                .onErrorResume(e -> Mono.empty());
    }

    private void addNewRequestsToScheduler(TsuchigumoResponse tsuchigumoResponse) {
        tsuchigumoResponse.newRequests.stream()
                .filter(newRequest -> !visitedUrls.contains(newRequest.url))
                .forEach(newRequest -> {
                    visitedUrls.add(newRequest.url);
                    scheduler.addRequest(newRequest);
                });
    }

    private Mono<TsuchigumoResponse> processMagatamaPipeline(TsuchigumoResponse response) {
        return Flux.fromIterable(response.magatamas)
                .map(this::processMagatamaThroughPipeline)
                .collectList() // このリクエストに関連する全アイテムの処理を完了するまで待つ
                .map(magatamas -> response);
    }

    private Magatama processMagatamaThroughPipeline(Magatama magatama) {
        for (MagatamaPipeline pipeline : magatamaPipelines) {
            magatama = pipeline.processMagatama(magatama);
        }
        return magatama;
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

    private void onNext(TsuchigumoResponse tsuchigumoResponse) {
        logger.info("次のクロールが完了しました：" + tsuchigumoResponse.getOriginalRequest().url);
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

    private boolean isRetractable(Throwable throwable) {
        if (throwable instanceof ConnectionException) {
            return true;
        } else if (throwable instanceof HttpErrorException httpErrorException) {
            int statusCode = httpErrorException.getStatusCode();
            // 一時的なエラー（例：503 Service Unavailable）の場合は再試行可能と判断
            return statusCode == 503;
        }
        return false;
    }

    private void logError(String component, String url, Throwable e) {
        logger.error("{} error for URL: {}, message: {}", component, url, e.getMessage(), e);
    }

}