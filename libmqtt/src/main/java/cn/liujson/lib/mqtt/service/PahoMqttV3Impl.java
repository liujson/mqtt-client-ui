package cn.liujson.lib.mqtt.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
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
    public static final int CONNECTION_TIMEOUT = 20;

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

    public PahoMqttV3Impl(final IMQTTBuilder builder) throws WrapMQTTException {
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
            mqttAsyncClient.connect(connOpts, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //第一次重连就出错了，很可能是配置出了问题
                    if (callback != null) {
                        callback.onFailure(exception);
                    }
                }
            });
        } catch (MqttException e) {
            //第一次重连就出错了，很可能是配置出了问题
            if (callback != null) {
                callback.onFailure(e);
            }
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
            if (callback != null) {
                callback.onFailure(e);
            }
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
            if (callback != null) {
                callback.onFailure(e);
            }
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
                    new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
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
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    @Override
    public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public void disconnectForcibly() throws Exception {
        //释放资源
        messageReceiver = null;
        //先尝试断开连接
        mqttAsyncClient.disconnect();
        //强制断开连接
        mqttAsyncClient.disconnectForcibly();
        //终止重连任务
        stopRetryTask();
    }


    @Override
    public String toString() {
        return "PahoMqttV3Impl{" +
                "client=" + mqttAsyncClient.getClientId() + "," +
                "serverUri=" + mqttAsyncClient.getServerURI() + "," +
                "activeSub=" + activeSubs + "}";
    }


    private final MqttCallbackExtended mMqttCallback = new MqttCallbackExtended() {

        /**
         * setAutomaticReconnect为True的时候
         * 它是在丢失重连成功后会触发该方法
         * @param reconnect
         * @param serverURI
         */
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            MQTTUtils.logD(TAG, "connectComplete,reconnect:" + reconnect);
            if (reconnect) {
                //重连
                return;
            }
            //第一次连接
        }

        /**
         *  connectionLost 是在连接已经连上且丢失后走这里
         *  掉线之后会在消息接收线程上回调
         * @param cause
         */
        @Override
        public void connectionLost(Throwable cause) {
            //失去连接
            //用户主动关闭和其他原因失去连接会走此方法，连接将被关闭,此Client将变得不可用，意味着生命周期的结束
            MQTTUtils.logD(TAG, "connectionLost:" + cause.toString());
            //启动断线重连任务
            startRetryTask();
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


    /**
     * 重连任务
     */
    private final Runnable retryRunning = () -> {
        connect(new IMQTTCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                MQTTUtils.logD(TAG, "IReconnectionStrategy reconnect success.");
            }

            @Override
            public void onFailure(Throwable value) {
                MQTTUtils.logE(TAG, "IReconnectionStrategy reconnect failure.");
                //继续重试
                if (mHandle != null) {
                    mHandle.postDelayed(retryRunning, calculateRetryTime());
                }
            }
        });
    };

    private HandlerThread retryHandlerThread;
    private Handler mHandle;

    /**
     * 启动重连定时任务
     */
    private void startRetryTask() {
        if (retryHandlerThread == null) {
            retryHandlerThread = new HandlerThread("PahoRetry");
            retryHandlerThread.start();
            mHandle = new Handler(retryHandlerThread.getLooper());
        }
        mHandle.postDelayed(retryRunning, calculateRetryTime());
    }


    private void stopRetryTask() {
        mHandle.removeCallbacks(retryRunning);
        retryHandlerThread.quitSafely();
        retryHandlerThread = null;
        mHandle = null;
    }

    /**
     * 计算下次重连需要等待的时间
     *
     * @return
     */
    private long calculateRetryTime() {
        return 3000;
    }
}
