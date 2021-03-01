package cn.liujson.lib.mqtt.api;

/**
 * 封装后的操作回调
 * @author liujson
 */
public interface IMQTTCallback<T> {
    /**
     * 成功
     * @param value
     */
    void onSuccess(T value);

    /**
     * 失败
     * @param value
     */
    void onFailure(Throwable value);
}