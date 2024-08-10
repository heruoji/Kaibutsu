package org.example.kaibutsu.core.scheduler;

import org.example.kaibutsu.core.downloader.DownloaderRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Scheduler {

    private final BlockingQueue<DownloaderRequest> downloaderRequestQueue = new LinkedBlockingQueue<>(100);

    private final Sinks.Many<DownloaderRequest> sink = Sinks.many().unicast().onBackpressureBuffer();

    private final int intervalMillSeconds;

    public Scheduler(int intervalMillSeconds) {
        this.intervalMillSeconds = intervalMillSeconds;
    }

    public Flux<DownloaderRequest> requestStream() {
        return sink.asFlux()
                .delayElements(Duration.ofMillis(intervalMillSeconds));
    }

    public void addRequest(DownloaderRequest downloaderRequest) {
        try {
            downloaderRequestQueue.put(downloaderRequest);
        } catch (InterruptedException e) {
            throw new SchedulerException("リクエストのスケジューラーへの追加に失敗しました。\nリクエスト：" + downloaderRequest + "\nエラーメッセージ：" + e.getMessage(), e);
        }
        emitRequests();
    }

    private void emitRequests() {
        DownloaderRequest nextDownloaderRequest;
        while ((nextDownloaderRequest = downloaderRequestQueue.poll()) != null) {
            sink.emitNext(nextDownloaderRequest, ((signalType, emitResult) -> Sinks.EmitResult.FAIL_OVERFLOW.equals(emitResult)));
        }
    }

    public void terminate() {
        emitRequests();
        sink.tryEmitComplete();
    }

    public boolean isEmpty() {
        return downloaderRequestQueue.isEmpty();
    }
}
