package cn.liujson.lib.mqtt.service.refactor;

import cn.liujson.lib.mqtt.api.QoS;
import io.reactivex.Completable;

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
    Completable connect();

    /**
     * 订阅
     *
     * @param topics topics
     * @param qos    qos
     * @return
     */
    Completable subscribe(String[] topics, QoS[] qos);

    /**
     * 解除订阅
     *
     * @param topics
     * @return
     */
    Completable unsubscribe(String[] topics);


    /**
     * 发布
     *
     * @param topic
     * @param payload
     * @param qos
     * @return
     */
    Completable publish(String topic, byte[] payload, QoS qos);


    /**
     * 断开连接
     *
     * @return
     */
    Completable disconnect();

    //endregion 主动型动作
}
