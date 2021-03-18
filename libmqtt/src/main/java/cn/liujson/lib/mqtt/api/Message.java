package cn.liujson.lib.mqtt.api;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

import cn.liujson.lib.mqtt.util.MqttUtils;

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
        return MqttUtils.qoS2Int(qos);
    }

    public Message setQos(@NonNull QoS qos) {
        Objects.requireNonNull(qos);
        this.qos = qos;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return retained == message.retained &&
                Arrays.equals(payload, message.payload) &&
                qos == message.qos;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(retained, qos);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
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
