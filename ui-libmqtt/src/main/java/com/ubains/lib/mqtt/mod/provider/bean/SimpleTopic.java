package com.ubains.lib.mqtt.mod.provider.bean;

import java.util.Objects;

public class SimpleTopic {

    public String topic;
    public int qos;

    public SimpleTopic(String topic, int qos) {
        this.topic = topic;
        this.qos = qos;
    }

    public SimpleTopic() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTopic that = (SimpleTopic) o;
        return qos == that.qos && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, qos);
    }
}