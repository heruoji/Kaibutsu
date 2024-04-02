package org.example.kaibutsu.core.scheduler;

import org.example.kaibutsu.core.downloader.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

public class Scheduler {

    private final SchedulerQueue schedulerQueue = new SchedulerQueue();
    private final Sinks.Many<Request> sink = Sinks.many().unicast().onBackpressureBuffer();

    // ConcurrentHashMapのキーとしてRequestを使用し、値としては単にBoolean.TRUEを使用する
    ConcurrentHashMap<Request, Boolean> inProgress = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    public void addRequest(Request request) {
        inProgress.put(request, Boolean.TRUE);
        schedulerQueue.add(request);
        tryEmitNextRequest();
    }

    public Request getNextRequest() {
        return schedulerQueue.poll();
    }

    private void tryEmitNextRequest() {
        Request nextRequest;
        while ((nextRequest = getNextRequest()) != null) {
            Sinks.EmitResult result = sink.tryEmitNext(nextRequest);
            if (result.isFailure()) {
                logger.error("Request emit failed: " + result);
                break; // バックプレッシャーを受けて処理を一時停止
            }
        }
    }


    public Flux<Request> requestStream() {
        return sink.asFlux();
    }

    public void terminate() {
        tryEmitNextRequest();
        sink.tryEmitComplete();
    }

    public boolean isEmpty() {
        return this.schedulerQueue.isEmpty();
    }

    public void setInProgress(ConcurrentHashMap<Request, Boolean> inProgress) {
        this.inProgress = inProgress;
    }
}
