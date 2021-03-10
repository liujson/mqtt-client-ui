package cn.liujson.lib.mqtt.service.refactor.service;


import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MQTTUtils;
import io.reactivex.Completable;

import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public class PahoV3MQTTClient extends MqttAsyncClient {

    private static final String TAG = "PahoV3MQTTClient";

    /**
     * 默认超时时间
     */
    public static final int DEFAULT_TIME_OUT = 10000;

    /**
     * 已经订阅的topic
     */
    private final ConcurrentHashMap<String, QoS> activeSubs = new ConcurrentHashMap<>();

    private final MqttConnectOptions mConnOpts;

    private MqttCallback mRealCallback;

    private IMQTTMessageReceiver messageReceiver;


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
        //设置自动重连
        mConnOpts.setAutomaticReconnect(builder.isAutoReconnect());
        //自动断线重连最大延时时间(默认128000)
        //mConnOpts.setMaxReconnectDelay();

        //set callback
        super.setCallback(new InternalProxyCallback());
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
            close();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }


    /**
     * 强制关闭连接
     * 先尝试发送断开信号，然后尝试关闭连接
     *
     * @param quiesceTimeout    允许当前入站和出站工作完成的超时时间
     * @param disconnectTimeout 发送断开连接包给服务端，直到超时时间
     * @return
     */
    public Completable rxCloseForcibly(final long quiesceTimeout, final long disconnectTimeout) {
        return Completable.create(emitter -> {
            disconnectForcibly(quiesceTimeout, disconnectTimeout);
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

    @Override
    public int hashCode() {
        return Objects.hash(activeSubs, mConnOpts, mRealCallback, messageReceiver);
    }

    @Override
    public void setCallback(MqttCallback callback) {
        this.mRealCallback = callback;
    }


    /**
     * 设置消息接收
     */
    public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    //---------------------------------------------------------------------------------------------

    /**
     * 内部代理 Callback
     */
    class InternalProxyCallback implements MqttCallbackExtended {

        /**
         * 触发条件是setAutomaticReconnect为True的时候,第一次连接成功和重连成功后会触发该方法
         *
         * @param reconnect 如何设置了cleanSession为false 重连后会订阅之前的主题，否则不会订阅之前的主题
         * @param serverURI 服务端URI
         */
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            MQTTUtils.logD(TAG, "connectComplete,serverURI:" + serverURI + ",reconnect:" + reconnect);
            if (mRealCallback != null && mRealCallback instanceof MqttCallbackExtended) {
                ((MqttCallbackExtended) mRealCallback).connectComplete(reconnect, serverURI);
            }
        }

        /**
         * connectionLost 是在连接已经连上且丢失后走这里,掉线之后会在消息接收线程上回调
         *
         * @param cause 连接丢失的原因
         */
        @Override
        public void connectionLost(Throwable cause) {
            MQTTUtils.logD(TAG, "connectionLost:" + cause.toString());
            if (mRealCallback != null) {
                mRealCallback.connectionLost(cause);
            }
        }

        /**
         * 接收到消息会触发此方法
         *
         * @param topic
         * @param message
         * @throws Exception
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            MQTTUtils.logD(TAG, "messageArrived detail,topic:" + topic +
                    ",Qos:" + message.getQos() + ",length:" + message.getPayload().length);
            if (mRealCallback != null) {
                mRealCallback.messageArrived(topic, message);
            }

            if (messageReceiver != null) {
                messageReceiver.onReceive(topic, message.getPayload());
            }
        }

        /**
         * 消息发送成功会触发此方法
         *
         * @param token
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            MQTTUtils.logD(TAG, "deliveryComplete");
            if (mRealCallback != null) {
                mRealCallback.deliveryComplete(token);
            }
        }
    }
}
