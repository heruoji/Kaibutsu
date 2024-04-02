package org.example.kaibutsu.core.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;

import java.util.ArrayList;
import java.util.List;

public class TsuchigumoResponse {

    private final Request originalRequest;

    public List<Request> newRequests;
    public List<Magatama> magatamas;

    private TsuchigumoResponse(Request originalRequest, List<Request> newRequests, List<Magatama> magatamas) {
        this.originalRequest = originalRequest;
        this.newRequests = newRequests;
        this.magatamas = magatamas;
    }

    public Request getOriginalRequest() {
        return originalRequest;
    }

    public static class TsuchigumoResponseBuilder {
        private final Request originalRequest;
        private List<Request> requests = new ArrayList<>();
        private List<Magatama> magatamas = new ArrayList<>();

        public TsuchigumoResponseBuilder(Request originalRequest) {
            this.originalRequest = originalRequest;
        }

        public TsuchigumoResponseBuilder requests(List<Request> requests) {
            this.requests = new ArrayList<>(requests);
            return this;
        }

        public TsuchigumoResponseBuilder magatamas(List<Magatama> magatamas) {
            this.magatamas = new ArrayList<>(magatamas);
            return this;
        }

        public TsuchigumoResponseBuilder addRequest(Request request) {
            this.requests.add(request);
            return this;
        }

        public TsuchigumoResponseBuilder addMagatama(Magatama magatama) {
            this.magatamas.add(magatama);
            return this;
        }

        public TsuchigumoResponse build() {
            return new TsuchigumoResponse(originalRequest, requests, magatamas);
        }
    }
}
