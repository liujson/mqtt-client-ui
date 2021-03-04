package cn.liujson.lib.mqtt.api;

/**
 * MQTT 操作抽象接口
 * @author liujson
 * @date 2021/2/22.
 */
public interface IMQTT {

    void connect(IMQTTCallback<Void> callback);

    void subscribe(String topic, QoS qoS, IMQTTCallback<byte[]> callback);

    void subscribe(String[] topics, QoS[] qoS, IMQTTCallback<byte[]> callback);

    void unsubscribe(String topic, IMQTTCallback<Void> callback);

    void unsubscribe(String[] topics, IMQTTCallback<Void> callback);

    void publish(String topic, byte[] payload, QoS qos, boolean retained, IMQTTCallback<Void> callback);

    void publish(String topic, String payload, QoS qos, boolean retained, IMQTTCallback<Void> callback);

    void disconnect(IMQTTCallback<Void> callback);

    void setMessageReceiver(IMQTTMessageReceiver messageReceiver);

    /**
     * 强制关闭连接
     * @throws Exception exception
     */
    void close() throws Exception;
}
