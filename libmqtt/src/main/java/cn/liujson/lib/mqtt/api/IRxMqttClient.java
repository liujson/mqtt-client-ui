package cn.liujson.lib.mqtt.api;

import androidx.annotation.NonNull;

import io.reactivex.Completable;

/**
 * Rxjava 版本的契约
 *
 * @author liujson
 * @date 2021/2/21.
 */
public interface IRxMqttClient {

    //region 主动型动作

    /**
     * 连接服务器
     *
     * @return
     */
    Completable connect();

    /**
     * 订阅
     *
     * @param topics topics
     * @param qos    qos
     * @return
     */
    Completable subscribe(@NonNull String[] topics, @NonNull QoS[] qosArr);


    /**
     * 订阅
     */
    Completable subscribe(@NonNull String topic, @NonNull QoS qos);

    /**
     * 解除订阅
     *
     * @param topics
     * @return
     */
    Completable unsubscribe(@NonNull String[] topics);

    /**
     * 解除订阅
     */
    Completable unsubscribe(@NonNull String topic);

    /**
     * 发布
     *
     * @param topic
     * @param payload
     * @param qos
     * @return
     */
    Completable publish(@NonNull String topic, @NonNull byte[] payload, @NonNull QoS qos, boolean retained);

    /**
     * 发布
     *
     * @param topic   主题
     * @param message 消息
     * @return
     */
    Completable publish(@NonNull String topic, @NonNull Message message);

    /**
     * 断开连接
     *
     * @return
     */
    Completable disconnect();

    /**
     * 关闭客户端
     */
    Completable close();

    //endregion 主动型动作
}
