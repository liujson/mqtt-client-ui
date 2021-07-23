package com.ubains.lib.mqtt.mod.provider.event;

/**
 * @author liujson
 * @date 2021/6/30.
 */
public class MqttConnectionLostEvent {

    public Throwable cause;

    public MqttConnectionLostEvent(Throwable cause) {
        this.cause = cause;
    }
}
