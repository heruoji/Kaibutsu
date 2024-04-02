package org.example.kaibutsu.core.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import org.example.kaibutsu.core.tsuchigumo.exception.MethodInvocationException;
import org.example.kaibutsu.core.tsuchigumo.exception.MethodNotFoundException;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Tsuchigumo {
    Request startRequest();

    default Mono<TsuchigumoResponse> parse(Response response) {
        TsuchigumoResponse.TsuchigumoResponseBuilder builder = new TsuchigumoResponse.TsuchigumoResponseBuilder(response.request);
        Method[] declaredMethods = this.getClass().getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (method.getName().equals(response.getCallbackKey())) {
                try {
                    return Mono.just((TsuchigumoResponse) method.invoke(this, response, builder));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new MethodInvocationException(response.getCallbackKey(), e);
                }
            }
        }

        throw new MethodNotFoundException(response.getCallbackKey());
    }
}
