package cn.liujson.lib.mqtt.service.refactor.service;


import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MQTTUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public class PahoV3MQTTClient extends MqttAsyncClient {

    /**
     * 默认超时时间
     */
    public static final int DEFAULT_TIME_OUT = 10000;

    /**
     * 已经订阅的topic
     */
    private final ConcurrentHashMap<String, QoS> activeSubs = new ConcurrentHashMap<>();

    private final MqttConnectOptions mConnOpts;

    public PahoV3MQTTClient(final IMQTTBuilder builder) throws MqttException {
        super(Objects.requireNonNull(builder.getHost()),
                TextUtils.isEmpty(builder.getClientId()) ?
                        MQTTUtils.generateClientId() : builder.getClientId(),
                new MemoryPersistence());
        mConnOpts = new MqttConnectOptions();
        mConnOpts.setCleanSession(builder.isCleanSession());

        if (!TextUtils.isEmpty(builder.getUserName())) {
            mConnOpts.setUserName(builder.getUserName());
        }
        if (!TextUtils.isEmpty(builder.getPassword())) {
            mConnOpts.setPassword(builder.getPassword().toCharArray());
        }
        if (!TextUtils.isEmpty(builder.getWillTopic())
                && !TextUtils.isEmpty(builder.getWillMessage())
                && builder.getWillQos() != null) {
            mConnOpts.setWill(builder.getWillTopic(),
                    builder.getWillMessage().getBytes(),
                    MQTTUtils.qoS2Int(builder.getWillQos()), false);
        }
        //连接超时时间（秒）
        mConnOpts.setConnectionTimeout(20);
        //保持存活间隔（秒）
        mConnOpts.setKeepAliveInterval(builder.getKeepAlive());
        //自动重连
        mConnOpts.setAutomaticReconnect(true);
    }

    @Override
    public IMqttToken connect(Object userContext, IMqttActionListener callback) throws MqttException {
        return super.connect(mConnOpts, userContext, callback);
    }

    @Override
    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext,
                                IMqttActionListener callback) throws MqttException {
        return super.subscribe(topicFilters, qos, userContext, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                //把订阅成功的topic添加到列表
                final String[] subTopics = asyncActionToken.getTopics();
                final int[] subQos = asyncActionToken.getGrantedQos();
                if (subTopics != null && subTopics.length != 0) {
                    for (int i = 0; i < subTopics.length; i++) {
                        activeSubs.put(subTopics[i], MQTTUtils.int2QoS(subQos[i]));
                    }
                }
                if (callback != null) {
                    callback.onSuccess(asyncActionToken);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                if (callback != null) {
                    callback.onFailure(asyncActionToken, exception);
                }
            }
        });
    }


    @Override
    public IMqttToken unsubscribe(String[] topicFilters, Object userContext, IMqttActionListener callback) throws MqttException {
        return super.unsubscribe(topicFilters, userContext, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                //把取消订阅成功的topic从列表移除
                final String[] subTopics = asyncActionToken.getTopics();
                if (subTopics != null && subTopics.length != 0) {
                    for (int i = 0; i < subTopics.length; i++) {
                        activeSubs.remove(subTopics[i]);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(asyncActionToken);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                if (callback != null) {
                    callback.onFailure(asyncActionToken, exception);
                }
            }
        });
    }

    /**
     * 使用 Rx 堵塞方式连接
     *
     * @return
     */
    public Completable rxConnect() {
        return Completable
                .create(emitter -> {
                    connect(mConnOpts).waitForCompletion(DEFAULT_TIME_OUT);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 先断开连接然后 Close
     * 安全地关闭连接
     *
     * @return
     */
    public Completable rxCloseSafety() {
        return Completable.create(emitter -> {
            disconnect().waitForCompletion();
            close(true);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 强制关闭连接
     * 先尝试发送断开信号，然后尝试关闭连接
     */
    public Completable rxCloseForcibly() {
        return Completable.create(emitter -> {
            disconnectForcibly(DEFAULT_TIME_OUT, DEFAULT_TIME_OUT >> 1);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }


    @Override
    public String toString() {
        return "PahoV3MQTTClient{" +
                "clientId=" + getClientId() + "," +
                "serverUri=" + getServerURI() + "," +
                "activeSub=" + activeSubs + "}";
    }
}
