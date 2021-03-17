package cn.liujson.lib.mqtt.service.rx;

import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MQTTUtils;
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
    private final MqttClient mqttClient;
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
     * @param params 连接参数
     * @throws IllegalArgumentException 如果 URI 不是以"tcp://", "ssl://" or "local://" 开头的
     * @throws IllegalArgumentException 如果 clientId 是空或者字符长度超过65535
     * @throws MqttException            如果出现其他异常
     */
    public RxPahoClient(@NonNull ConnectionParams params) throws MqttException {
        Objects.requireNonNull(params);
        if (params.getServerURIs() == null && params.getServerURIs().length == 0) {
            throw new IllegalArgumentException("ServerURI must can not be null");
        }
        MemoryPersistence persistence = new MemoryPersistence();
        /**
         * MqttClient 内部实例化了MqttAsyncClient
         * 要连接的服务器地址，用URI指定。可以使用
         * {@link MqttConnectOptions#setServerURIs(String[])} 替换重复使用
         */
        mqttClient = new MqttClient(params.getServerURIs()[0], params.getClientId(), persistence);
        //设置默认超时的等待时间(ms)
        mqttClient.setTimeToWait(DEFAULT_TIMEOUT);
        this.params = params;
    }

    public String getClientId() {
        return mqttClient.getClientId();
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public String getCurrentServerURI() {

        return mqttClient.getCurrentServerURI();
    }

    public void setTimeToWait(long setTimeToWait) {
        mqttClient.setTimeToWait(setTimeToWait);
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


    @Override
    public Completable connect() {
        return Completable.create(emitter -> {
            final MqttConnectOptions options = params2Options(this.params);
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
            mqttClient.subscribe(topics, MQTTUtils.qoS2IntArr(qosArr));
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
            if (topics == null) {
                emitter.onError(new IllegalArgumentException("topics is null"));
                return;
            }
            mqttClient.unsubscribe(topics);
            synchronized (activeSubLock) {
                for (int i = 0; i < topics.length; i++) {
                    activeSubs.remove(topics[i]);
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
            mqttClient.publish(topic, payload, MQTTUtils.qoS2Int(qos), retained);
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
            emitter.onComplete();
        });
    }

    /**
     * 安全关闭连接
     *
     * @return
     */
    public Completable closeSafety() {
        return Completable.create(emitter -> {
            mqttClient.disconnect();
            mqttClient.close();
            emitter.onComplete();
        });
    }

    /**
     * 强制关闭连接
     *
     * @return
     */
    public Completable closeForcibly(final long quiesceTimeout, final long disconnectTimeout) {
        return Completable.create(emitter -> {
            mqttClient.disconnectForcibly(quiesceTimeout, disconnectTimeout);
            emitter.onComplete();
        });
    }


    public static MqttConnectOptions params2Options(ConnectionParams params) {
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(params.getServerURIs());
        options.setMaxReconnectDelay(params.getMaxReconnectDelay());
        options.setAutomaticReconnect(params.isAutomaticReconnect());
        options.setConnectionTimeout(params.getConnectionTimeout());
        options.setKeepAliveInterval(params.getKeepAlive());
        if (params.getWillTopic() != null && params.getWillMessage() != null) {
            final Message willMessage = params.getWillMessage();
            options.setWill(params.getWillTopic(),
                    willMessage.getPayload(),
                    willMessage.getQosInt(),
                    willMessage.isRetained());
        }
        if (TextUtils.isEmpty(params.getUsername())) {
            options.setUserName(params.getUsername());
        }
        if (TextUtils.isEmpty(params.getPassword())) {
            options.setPassword(params.getPassword().toCharArray());
        }
        options.setCleanSession(params.isCleanSession());
        options.setMqttVersion(params.getMqttVersion());
        return options;
    }
}
