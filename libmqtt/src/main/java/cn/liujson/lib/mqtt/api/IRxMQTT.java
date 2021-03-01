package cn.liujson.lib.mqtt.api;

import io.reactivex.Observable;

/**
 * Rxjava 版本的契约
 *
 * @author liujson
 * @date 2021/2/21.
 */
public interface IRxMQTT {

    //region 主动型动作

    /**
     * 连接服务器
     *
     * @return
     */
    Observable<Void> connect();

    /**
     * 订阅
     *
     * @param topics topics
     * @param qos    qos
     * @return
     */
    Observable<Void> subscribe(String[] topics, QoS[] qos);

    /**
     * 解除订阅
     *
     * @param topics
     * @return
     */
    Observable<Void> unsubscribe(String[] topics);


    /**
     * 发布
     *
     * @param topic
     * @param payload
     * @param qos
     * @return
     */
    Observable<Void> publish(String topic, byte[] payload, QoS qos);


    /**
     * 断开连接
     *
     * @return
     */
    Observable<Void> disconnect();

    //endregion 主动型动作
}
