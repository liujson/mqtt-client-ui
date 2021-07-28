package cn.liujson.client.ui.bean.entity;

import cn.liujson.lib.mqtt.api.QoS;

public class MsgItem {
    public String topic;
    public QoS qoS;
    public String message;
    public long messageDate;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public QoS getQoS() {
        return qoS;
    }

    public void setQoS(QoS qoS) {
        this.qoS = qoS;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(long messageDate) {
        this.messageDate = messageDate;
    }
}