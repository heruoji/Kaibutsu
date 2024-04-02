package org.example.kaibutsu.core.tsuchigumo.exception;

public class MethodNotFoundException extends TsuchigumoException {
    public MethodNotFoundException(String callbackKey) {
        super("指定されたメソッドが見つかりません：" + callbackKey);
    }
}
