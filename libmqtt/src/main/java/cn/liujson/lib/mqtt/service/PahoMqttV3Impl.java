package cn.liujson.lib.mqtt.service;

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
import org.fusesource.hawtbuf.UTF8Buffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTConnectionBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.IReconnectionStrategy;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.util.MQTTUtils;

/**
 * 同步型的 MqttClient 封装
 * 为什么用同步消息，因为这是最简单的方式
 *
 * @author liujson
 * @date 2021/2/7.
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class PahoMqttV3Impl implements IMQTT {

    private static final String TAG = "PahoMqttV3Impl";
    /**
     * 连接超时时间
     */
    public static final int CONNECTION_TIMEOUT = 12;

    private final MqttAsyncClient mqttAsyncClient;
    private final MqttConnectOptions connOpts;
    /**
     * 消息接收者
     */
    private IMQTTMessageReceiver messageReceiver;

    /**
     * 已经订阅的topic
     */
    private final HashMap<String, QoS> activeSubs = new HashMap<>();

    public PahoMqttV3Impl(final IMQTTConnectionBuilder builder) throws WrapMQTTException {
        Objects.requireNonNull(builder.getHost());
        String clientId = builder.getClientId();
        //如果clientId是空或者null，生成一个随机的clientId
        if (TextUtils.isEmpty(clientId)) {
            clientId = MQTTUtils.generateClientId();
        }
        try {
            /**
             * MemoryPersistence设置clientid的保存形式，默认为以内存保存
             */
            MemoryPersistence persistence = new MemoryPersistence();
            mqttAsyncClient = new MqttAsyncClient(builder.getHost(), clientId, persistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(builder.isCleanSession());

            if (!TextUtils.isEmpty(builder.getUserName())) {
                connOpts.setUserName(builder.getUserName());
            }
            if (!TextUtils.isEmpty(builder.getPassword())) {
                connOpts.setPassword(builder.getPassword().toCharArray());
            }
            if (!TextUtils.isEmpty(builder.getWillTopic())
                    && !TextUtils.isEmpty(builder.getWillMessage())
                    && builder.getWillQos() != null) {
                connOpts.setWill(builder.getWillTopic(),
                        builder.getWillMessage().getBytes(),
                        MQTTUtils.qoS2Int(builder.getWillQos()), false);
            }
            //连接超时时间（秒）
            connOpts.setConnectionTimeout(CONNECTION_TIMEOUT);
            //保持存活间隔（秒）
            connOpts.setKeepAliveInterval(builder.getKeepAlive());
            //设置回调
            mqttAsyncClient.setCallback(mMqttCallback);

            //自动重连
            connOpts.setAutomaticReconnect(true);

            //初始化
            activeSubs.clear();
        } catch (MqttException e) {
            throw new WrapMQTTException(e);
        }
    }

    @Override
    public void connect(IMQTTCallback<Void> callback) {
        try {
            final LoginHandler loginHandler = new LoginHandler(callback);
            mqttAsyncClient.connect(connOpts, null, loginHandler);
        } catch (MqttException e) {
            //第一次重连就出错了，很可能是配置出了问题
            callback.onFailure(new FirstConnectMqttException(e));
        }
    }

    @Override
    public void subscribe(final String topic, final QoS qoS, IMQTTCallback<byte[]> callback) {
        subscribe(new String[]{topic}, new QoS[]{qoS}, callback);
    }

    @Override
    public void subscribe(String[] topics, QoS[] qoS, IMQTTCallback<byte[]> callback) {
        try {
            mqttAsyncClient.subscribe(topics, MQTTUtils.qoS2IntArr(qoS), null,
                    new IMqttActionListener() {
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
                                callback.onSuccess(new byte[1]);
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            if (callback != null) {
                                callback.onFailure(exception);
                            }
                        }
                    });
        } catch (MqttException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void unsubscribe(String topic, IMQTTCallback<Void> callback) {
        unsubscribe(new String[]{topic}, callback);
    }

    @Override
    public void unsubscribe(String[] topics, IMQTTCallback<Void> callback) {
        try {
            mqttAsyncClient.unsubscribe(topics, null,
                    new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            //把订阅成功的topic添加到列表
                            final String[] subTopics = asyncActionToken.getTopics();
                            if (subTopics != null && subTopics.length != 0) {
                                for (int i = 0; i < subTopics.length; i++) {
                                    activeSubs.remove(subTopics[i]);
                                }
                            }
                            if (callback != null) {
                                callback.onSuccess(null);
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            if (callback != null) {
                                callback.onFailure(exception);
                            }
                        }
                    });
        } catch (MqttException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void publish(String topic, byte[] payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
        try {
            mqttAsyncClient.publish(topic, payload, MQTTUtils.qoS2Int(qos), retained, null,
                    MQTTUtils.adapterActionListener(callback));
        } catch (MqttException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    @Override
    public void publish(String topic, String payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
        publish(topic, payload.getBytes(), qos, retained, callback);
    }

    @Override
    public void disconnect(IMQTTCallback<Void> callback) {
        try {
            mqttAsyncClient.disconnect(null,
                    MQTTUtils.adapterActionListener(callback));
        } catch (MqttException e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public void close() throws Exception {
        //释放资源
        messageReceiver = null;
        //强制断开连接
        mqttAsyncClient.close(true);
    }


    @Override
    public String toString() {
        return "PahoMqttV3Impl{client=" + mqttAsyncClient.getClientId() + ",activeSub=" + activeSubs + "}";
    }


    private final MqttCallbackExtended mMqttCallback = new MqttCallbackExtended() {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (reconnect) {
                //重连
                return;
            }
            //第一次连接
        }

        @Override
        public void connectionLost(Throwable cause) {
            //失去连接
            //用户主动关闭和其他原因失去连接会走此方法，连接将被关闭,此Client将变得不可用，意味着生命周期的结束
            MQTTUtils.logD(TAG, "connectionLost:" + cause.toString());

            //需要自己重写重连机制
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            MQTTUtils.logD(TAG, "messageArrived,topic:" + topic);
            //消息抵达
            if (messageReceiver != null) {
                messageReceiver.onReceive(topic, message.getPayload());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //发送完成
//            try {
//                final MqttMessage message = token.getMessage();
//                MQTTUtils.logD(TAG, "deliveryComplete   topic:" + Arrays.toString(token.getTopics()) + ",message" + message.toString());
//            } catch (MqttException e) {
//                e.printStackTrace();
//                MQTTUtils.logD(TAG, "deliveryComplete topic:" + Arrays.toString(token.getTopics()));
//            }
        }
    };


    private static class LoginHandler implements IReconnectionStrategy, IMqttActionListener {
        final IMQTTCallback<Void> cb;

        public LoginHandler(IMQTTCallback<Void> cb) {
            this.cb = cb;
        }


        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            //第一次连接成功
            if (this.cb != null) {
                this.cb.onSuccess(null);
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            //第一次连接失败了
            if (this.cb != null) {
                this.cb.onFailure(exception);
            }
        }

        @Override
        public void reconnect() {

        }
    }

    /**
     * 首次进行连接异常了
     */
    public static class FirstConnectMqttException extends MqttException {

        public FirstConnectMqttException(MqttException cause) {
            super(cause);
        }
    }
}
