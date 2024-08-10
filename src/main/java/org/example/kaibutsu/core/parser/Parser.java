package org.example.kaibutsu.core.parser;

import org.example.kaibutsu.core.downloader.DownloaderRequest;
import org.example.kaibutsu.core.downloader.DownloaderResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Parser {
    DownloaderRequest startRequest();

    default Mono<ParserResponse> parse(DownloaderResponse downloaderResponse) {
        Method[] declaredMethods = this.getClass().getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (method.getName().equals(downloaderResponse.getCallbackKey())) {
                try {
                    return Mono.just((ParserResponse) method.invoke(this, downloaderResponse));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ParserException("次のメソッドの呼び出しに失敗しました：" + downloaderResponse.getCallbackKey(), e);
                }
            }
        }

        throw new ParserException("次のメソッドが見つかりませんでした：" + downloaderResponse.getCallbackKey());
    }
}
