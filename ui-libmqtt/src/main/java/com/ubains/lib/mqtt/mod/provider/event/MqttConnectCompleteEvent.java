package com.ubains.lib.mqtt.mod.provider.event;

/**
 * @author liujson
 * @date 2021/6/29.
 */
public class MqttConnectCompleteEvent {
    /**
     * 如果cleanSession是true,重连后需要重新订阅topic
     */
    public boolean reconnect;
    public String serverURI;

    public MqttConnectCompleteEvent(boolean reconnect, String serverURI) {
        this.reconnect = reconnect;
        this.serverURI = serverURI;
    }
}
