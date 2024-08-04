package org.example.kaibutsu.core.scheduler;

import org.example.kaibutsu.core.downloader.Request;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SchedulerQueue {

    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>(100);

    public void add(Request request) {
        try {
            requestQueue.put(request);
        } catch (InterruptedException e) {
            throw new SchedulerException(e.getMessage(), e);
        }
    }

    public void addAll(List<Request> requests) {
        for (Request request : requests) {
            add(request);
        }
    }

    public Request poll() {
        return requestQueue.poll();
    }

    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}
