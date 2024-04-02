package org.example.kaibutsu.core.tsuchigumo.exception;

public class MethodInvocationException extends TsuchigumoException {
    public MethodInvocationException(String callbackKey, Throwable throwable) {
        super("メソッドの実行中にエラーが発生しました：" + callbackKey, throwable);
    }
}
