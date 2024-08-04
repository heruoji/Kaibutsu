package org.example.kaibutsu.core.scheduler;

import org.example.kaibutsu.core.downloader.Request;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Scheduler {

    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>(100);

    private final Sinks.Many<Request> sink = Sinks.many().unicast().onBackpressureBuffer();

    private final int intervalMillSeconds;

    public Scheduler(int intervalMillSeconds) {
        this.intervalMillSeconds = intervalMillSeconds;
    }

    public Flux<Request> requestStream() {
        return sink.asFlux()
                .delayElements(Duration.ofMillis(intervalMillSeconds));
    }

    public void addRequest(Request request) {
        try {
            requestQueue.put(request);
        } catch (InterruptedException e) {
            throw new SchedulerException("リクエストのスケジューラーへの追加に失敗しました。\nリクエスト：" + request + "\nエラーメッセージ：" + e.getMessage(), e);
        }
        emitRequests();
    }

    private void emitRequests() {
        Request nextRequest;
        while ((nextRequest = requestQueue.poll()) != null) {
            sink.emitNext(nextRequest, ((signalType, emitResult) -> Sinks.EmitResult.FAIL_OVERFLOW.equals(emitResult)));
        }
    }

    public void terminate() {
        emitRequests();
        sink.tryEmitComplete();
    }

    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}
