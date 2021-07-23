package com.ubains.lib.mqtt.mod.provider.event;


/**
 * @author liujson
 * @date 2021/6/30.
 */
public class MqttMessageArrivedEvent {
    public String topic;
    public String message;
    public int qos;

    public MqttMessageArrivedEvent(String topic, String message,int qos) {
        this.topic = topic;
        this.message = message;
        this.qos = qos;
    }
}
