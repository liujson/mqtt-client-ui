package cn.liujson.lib.mqtt.service.rx;

import androidx.annotation.NonNull;

import cn.liujson.lib.mqtt.api.IRxMqttClient;
import cn.liujson.lib.mqtt.api.Message;
import cn.liujson.lib.mqtt.api.QoS;
import io.reactivex.Completable;

/**
 *  FuseSource MqttClient
 * @author liujson
 * @date 2021/3/18.
 */
public class RxFuseSourceClient implements IRxMqttClient {


    @Override
    public Completable connect() {
        return null;
    }

    @Override
    public Completable subscribe(@NonNull String[] topics, @NonNull QoS[] qosArr) {
        return null;
    }

    @Override
    public Completable subscribe(@NonNull String topic, @NonNull QoS qos) {
        return null;
    }

    @Override
    public Completable unsubscribe(@NonNull String[] topics) {
        return null;
    }

    @Override
    public Completable unsubscribe(@NonNull String topic) {
        return null;
    }

    @Override
    public Completable publish(@NonNull String topic, @NonNull byte[] payload, @NonNull QoS qos, boolean retained) {
        return null;
    }

    @Override
    public Completable publish(@NonNull String topic, @NonNull Message message) {
        return null;
    }

    @Override
    public Completable disconnect() {
        return null;
    }

    @Override
    public Completable close() {
        return null;
    }
}
