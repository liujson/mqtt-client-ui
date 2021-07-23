package com.ubains.lib.mqtt.mod.provider.event;

/**
 * @author liujson
 * @date 2021/6/1.
 */
public class MqttFirstConnectRetryEvent {
    private final String message;

    public MqttFirstConnectRetryEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
