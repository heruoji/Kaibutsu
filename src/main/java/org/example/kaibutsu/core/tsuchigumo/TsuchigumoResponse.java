package org.example.kaibutsu.core.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TsuchigumoResponse {

    public List<Request> newRequests;
    public List<Magatama> magatamas;

    public TsuchigumoResponse(List<Request> newRequests, List<Magatama> magatamas) {
        this.newRequests = newRequests;
        this.magatamas = magatamas;
    }

    public static TsuchigumoResponse fromNewRequests(List<Request> newRequests) {
        return new TsuchigumoResponse(newRequests, Collections.emptyList());
    }

    public static TsuchigumoResponse fromMagatamas(List<Magatama> magatamas) {
        return new TsuchigumoResponse(Collections.emptyList(), magatamas);
    }
}
