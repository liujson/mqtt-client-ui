package cn.liujson.client.ui.bean.entity;

import java.util.Objects;

import cn.liujson.lib.mqtt.api.QoS;

public class SubTopicItem {
    public String topic;
    public QoS qos;
    public int msgCount;
    public boolean selected;

    public SubTopicItem(String topic, QoS qos) {
        this.topic = topic;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public QoS getQos() {
        return qos;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTopicItem that = (SubTopicItem) o;
        return Objects.equals(topic, that.topic) &&
                qos == that.qos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, qos);
    }
}