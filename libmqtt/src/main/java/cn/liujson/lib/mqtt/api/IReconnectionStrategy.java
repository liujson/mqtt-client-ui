package cn.liujson.lib.mqtt.api;

/**
 * 重连策略
 * @author liujson
 * @date 2021/2/24.
 */
public interface IReconnectionStrategy {
    /**
     * 重新连接（需要实现）
     * @param param
     */
    void reconnect();
}
