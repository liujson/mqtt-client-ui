package com.ubains.lib.mqtt.mod.provider.bean;

public class SimpleTopic {

    public String topic;
    public int qos;

    public SimpleTopic(String topic, int qos) {
        this.topic = topic;
        this.qos = qos;
    }

    public SimpleTopic() {
    }
}