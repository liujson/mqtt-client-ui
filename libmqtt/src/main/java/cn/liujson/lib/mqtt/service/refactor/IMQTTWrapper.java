package cn.liujson.lib.mqtt.service.refactor;


/**
 * @author liujson
 * @date 2021/3/8.
 */
public interface IMQTTWrapper<T> {

    /**
     * 获取 IMQTTService 的实现
     *
     * @return IMQTTService 的实现
     */
    T getClient();
}
