package com.ubains.lib.mqtt.mod.provider;

import android.util.Pair;

import androidx.annotation.Nullable;

import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;

import java.util.List;


import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import io.reactivex.Completable;

/**
 * @author liujson
 * @date 2021/4/21.
 */
public interface MqttConnection {

    //---------------------------------配置存储相关--------------------------------------

    /**
     * 存储 mqtt 连接配置信息
     *
     * @param connectionProfile
     * @return
     */
    boolean storeProfile(ConnectionProfile connectionProfile);

    /**
     * 读取本地存储的 mqtt 连接配置信息
     *
     * @return
     */
    @Nullable
    ConnectionProfile loadProfile();

    //---------------------------------配置存储相关--------------------------------------


    //----------------------------------操作MQTT相关------------------------------------

    /**
     * 服务是否绑定到 Application
     *
     * @return
     */
    boolean isBind();

    /**
     * 是否安装 MQTT 服务
     */
    boolean isInstalled();

    /**
     * 判断连接参数是否相同
     */
    boolean isSame(Object object);

    /**
     * MQTT 客服端是否已经连接
     */
    boolean isConnected();

    /**
     * MQTT 客服端是否已经关闭
     */
    boolean isClosed();

    void install(RxPahoClient client);

    void uninstall();

    List<Pair<String, QoS>> getSubList();

    RxPahoClient getClient();

    //----------------------------------操作MQTT相关------------------------------------


    //----------------------------------操作MQTT RxJava相关------------------------------------
    Completable connect();

    Completable subscribe(String topic, QoS qoS);

    Completable subscribe(String[] topic, QoS[] qoS);

    Completable unsubscribe(String topic);

    Completable publish(String topic, String message, QoS qoS, boolean retained);

    Completable closeSafety();

    Completable closeForcibly();
    //----------------------------------操作MQTT RxJava相关------------------------------------
}
