package org.example.kaibutsu.core.tsuchigumo;

import org.example.kaibutsu.core.downloader.Request;
import org.example.kaibutsu.core.downloader.Response;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Tsuchigumo {
    Request startRequest();

    default Mono<TsuchigumoResponse> parse(Response response) {
        Method[] declaredMethods = this.getClass().getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (method.getName().equals(response.getCallbackKey())) {
                try {
                    return Mono.just((TsuchigumoResponse) method.invoke(this, response));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new TsuchigumoException("次のメソッドの呼び出しに失敗しました：" + response.getCallbackKey(), e);
                }
            }
        }

        throw new TsuchigumoException("次のメソッドが見つかりませんでした：" + response.getCallbackKey());
    }
}
