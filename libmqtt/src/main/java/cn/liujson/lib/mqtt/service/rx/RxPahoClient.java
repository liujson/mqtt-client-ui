package cn.liujson.lib.mqtt.service.rx;

import android.util.Pair;

import androidx.annotation.NonNull;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.IRxMqttClient;
import cn.liujson.lib.mqtt.api.Message;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.paho.PahoLoggerImpl;
import cn.liujson.lib.mqtt.service.paho.PahoMqttClient;
import cn.liujson.lib.mqtt.util.MqttUtils;
import io.reactivex.Completable;

/**
 * 同步类型的Paho MQTT Client V3 Rxjava2封装
 *
 * @author liujson
 * @date 2021/3/17.
 */
public class RxPahoClient implements IRxMqttClient {
    /**
     * 默认超时时间（ms）
     */
    private static final int DEFAULT_TIMEOUT = 15 * 1000;

    /**
     * MqttClient
     */
    @NonNull
    private final PahoMqttClient mqttClient;
    /**
     * 连接参数
     */
    @NonNull
    private final ConnectionParams params;

    /**
     * 已经订阅的topic
     */
    private final LinkedHashMap<String, QoS> activeSubs = new LinkedHashMap<>();

    private final Object activeSubLock = new Object();
    /**
     * 代理一层
     */
    private MqttCallback mRealCallback;

    /**
     * @param params 连接参数
     * @throws IllegalArgumentException 如果 URI 不是以"tcp://", "ssl://" or "local://" 开头的
     * @throws IllegalArgumentException 如果 clientId 是空或者字符长度超过65535
     * @throws MqttException            如果出现其他异常
     */
    public RxPahoClient(@NonNull ConnectionParams params) throws MqttException {
        Objects.requireNonNull(params);
        if (params.getServerURIs().length == 0) {
            throw new IllegalArgumentException("ServerURI can not be empty");
        }
        MemoryPersistence persistence = new MemoryPersistence();
        /*
         * MqttClient 内部实例化了MqttAsyncClient
         * 要连接的服务器地址，用URI指定。可以使用
         * {@link MqttConnectOptions#setServerURIs(String[])} 替换重复使用
         */
        mqttClient = new PahoMqttClient(params.getServerURIs()[0], params.getClientId(), persistence);
        //设置默认超时的等待时间(ms)
        mqttClient.setTimeToWait(DEFAULT_TIMEOUT);
        //set callback
        mqttClient.setCallback(new InternalProxyCallback());

        activeSubs.clear();

        this.params = params;
    }

    @NonNull
    public ConnectionParams getParams() {
        return params;
    }

    public String getClientId() {
        return mqttClient.getClientId();
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public boolean isClosed() {
        return mqttClient.isClosed();
    }

    public boolean isConnecting() {
        return mqttClient.isConnecting();
    }

    public boolean isResting() {
        return mqttClient.isResting();
    }


    public String getCurrentServerURI() {
        return mqttClient.getCurrentServerURI();
    }

    public void setTimeToWait(long setTimeToWait) {
        mqttClient.setTimeToWait(setTimeToWait);
    }

    /**
     * 设置callback
     */
    public void setCallback(MqttCallback callback) {
        this.mRealCallback = callback;
    }

    /**
     * 获取订阅列表
     */
    public List<Pair<String, QoS>> getActiveSubs() {
        ArrayList<Pair<String, QoS>> subList = new ArrayList<>();
        for (Map.Entry<String, QoS> entry : activeSubs.entrySet()) {
            subList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return subList;
    }

    /**
     * 带返回参数的连接
     *
     * @return
     * @throws MqttException
     */
    public IMqttToken connectWithResult() throws MqttException {
        final MqttConnectOptions options = MqttUtils.params2Options(this.params);
        return mqttClient.connectWithResult(options);
    }

    public IMqttToken subscribeWithResponse(@NonNull final String[] topics, @NonNull final QoS[] qosArr) throws MqttException {
        return mqttClient.subscribeWithResponse(topics, MqttUtils.qoS2IntArr(qosArr));
    }


    @Override
    public Completable connect() {
        return Completable.create(emitter -> {
            final MqttConnectOptions options = MqttUtils.params2Options(this.params);
            mqttClient.connect(options);
            emitter.onComplete();
        });
    }

    @Override
    public Completable subscribe(@NonNull final String[] topics, @NonNull final QoS[] qosArr) {
        return Completable.create(emitter -> {
            Objects.requireNonNull(topics);
            Objects.requireNonNull(qosArr);
            if (topics.length != qosArr.length) {
                emitter.onError(new IllegalArgumentException("topics.length != qosArr.length"));
                return;
            }
            mqttClient.subscribe(topics, MqttUtils.qoS2IntArr(qosArr));
            synchronized (activeSubLock) {
                for (int i = 0; i < topics.length; i++) {
                    activeSubs.put(topics[i], qosArr[i]);
                }
            }
            emitter.onComplete();
        });
    }

    @Override
    public Completable subscribe(@NonNull String topic, @NonNull QoS qos) {
        return subscribe(new String[]{topic}, new QoS[]{qos});
    }

    @Override
    public Completable unsubscribe(@NonNull String[] topics) {
        return Completable.create(emitter -> {
            Objects.requireNonNull(topics);
            mqttClient.unsubscribe(topics);
            synchronized (activeSubLock) {
                for (String topic : topics) {
                    activeSubs.remove(topic);
                }
            }
            emitter.onComplete();
        });
    }

    @Override
    public Completable unsubscribe(@NonNull String topic) {
        return unsubscribe(new String[]{topic});
    }

    @Override
    public Completable publish(@NonNull String topic, @NonNull byte[] payload, @NonNull QoS qos, boolean retained) {
        return Completable.create(emitter -> {
            mqttClient.publish(topic, payload, MqttUtils.qoS2Int(qos), retained);
            emitter.onComplete();
        });
    }

    @Override
    public Completable publish(@NonNull String topic, @NonNull Message message) {
        return Completable.create(emitter -> {
            Message.checkMessageNonNull(message);
            mqttClient.publish(topic, message.getPayload(), message.getQosInt(), message.isRetained());
            emitter.onComplete();
        });
    }

    @Override
    public Completable disconnect() {
        return Completable.create(emitter -> {
            mqttClient.disconnect();
            synchronized (activeSubLock) {
                activeSubs.clear();
            }
            emitter.onComplete();
        });
    }

    @Override
    public Completable close() {
        //强制关闭
        return closeForcibly(10000, 5000);
    }

    /**
     * 释放资源后所有回调会失效
     */
    public void release() {
        setCallback(null);
        synchronized (activeSubLock) {
            activeSubs.clear();
        }
    }

    /**
     * 安全关闭连接
     */
    public Completable closeSafety() {
        return Completable.create(emitter -> {
            mqttClient.disconnect();
            synchronized (activeSubLock) {
                activeSubs.clear();
            }
            mqttClient.close();
            release();
            emitter.onComplete();
        });
    }

    public void close(boolean force) throws MqttException {
        mqttClient.close(force);
    }

    public void disconnectForcibly(final long quiesceTimeout, final long disconnectTimeout) throws MqttException {
        mqttClient.disconnectForcibly(quiesceTimeout, disconnectTimeout);
    }

    /**
     * 强制关闭连接
     */
    public Completable closeForcibly(final long quiesceTimeout, final long disconnectTimeout) {
        return Completable.create(emitter -> {
            mqttClient.disconnectForcibly(quiesceTimeout, disconnectTimeout);
            release();
            emitter.onComplete();
        });
    }

    /**
     * 内部 Callback 用来处理一些事物
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
            //连接丢失后清空订阅成功的内容
            if (params.isCleanSession()) {
                synchronized (activeSubLock) {
                    activeSubs.clear();
                }
            }
            if (mRealCallback != null) {
                mRealCallback.connectionLost(cause);
            }
        }

        /**
         * 接收到消息会触发此方法
         *
         * @param topic   topic
         * @param message message
         * @throws Exception exception
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (mRealCallback != null) {
                mRealCallback.messageArrived(topic, message);
            }
        }

        /**
         * 消息发送成功会触发此方法
         *
         * @param token token
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            if (mRealCallback != null) {
                mRealCallback.deliveryComplete(token);
            }
        }
    }

}
