package cn.liujson.lib.mqtt.service;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.api.IRxMQTT;
import io.reactivex.Observable;

/**
 * TODO 未开发完成
 * @author liujson
 * @date 2021/2/22.
 */
public class RxFuseSource implements IRxMQTT {

    public RxFuseSource() {

    }

    @Override
    public Observable<Void> connect() {
        return null;
    }

    @Override
    public Observable<Void> subscribe(String[] topics, QoS[] qos) {
        return null;
    }

    @Override
    public Observable<Void> unsubscribe(String[] topics) {
        return null;
    }

    @Override
    public Observable<Void> publish(String topic, byte[] payload, QoS qos) {
        return null;
    }

    @Override
    public Observable<Void> disconnect() {
        return null;
    }
}
