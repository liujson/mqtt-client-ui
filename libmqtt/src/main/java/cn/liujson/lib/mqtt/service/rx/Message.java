package cn.liujson.lib.mqtt.service.rx;

import androidx.annotation.NonNull;

import java.util.Objects;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MQTTUtils;

/**
 * MQTT 消息
 *
 * @author liujson
 * @date 2021/3/17.
 */
public class Message {

    private byte[] payload = new byte[0];
    private boolean retained = false;
    private QoS qos = QoS.AT_LEAST_ONCE;

    public byte[] getPayload() {
        return payload;
    }

    public Message setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public boolean isRetained() {
        return retained;
    }

    public Message setRetained(boolean retained) {
        this.retained = retained;
        return this;
    }

    public QoS getQos() {
        return qos;
    }

    public int getQosInt() {
        return MQTTUtils.qoS2Int(qos);
    }

    public Message setQos(@NonNull QoS qos) {
        Objects.requireNonNull(qos);
        this.qos = qos;
        return this;
    }

    public static Message newMessage(){
        return new Message();
    }

    public static void checkMessageNonNull(Message message){
        Objects.requireNonNull(message);
        Objects.requireNonNull(message.payload);
        Objects.requireNonNull(message.qos);
    }
}
