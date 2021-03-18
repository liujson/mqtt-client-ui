package cn.liujson.lib.mqtt.service;

import android.text.TextUtils;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.ExtendedListener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.fusesource.mqtt.codec.MQTTFrame;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.util.MQTTUtils;

/**
 * FuseSource MqttClient 自带重连机制
 *
 * @author liujson
 * @date 2021/2/22.
 */
public class FuseSourceImpl {

    private static final String TAG = "FuseSourceMqtt";

    private final MQTT mqtt;
    private final CallbackConnection callbackConnection;

    private IMQTTMessageReceiver messageReceiver;

    public FuseSourceImpl(final IMQTTBuilder builder) throws WrapMQTTException {
        Objects.requireNonNull(builder.getHost());
        try {
            mqtt = new MQTT();
            mqtt.setHost(builder.getHost());
            mqtt.setCleanSession(builder.isCleanSession());
            if (!TextUtils.isEmpty(builder.getClientId())) {
                mqtt.setClientId(builder.getClientId());
            }
            mqtt.setKeepAlive((short) builder.getKeepAlive());
            if (!TextUtils.isEmpty(builder.getUserName())) {
                mqtt.setUserName(builder.getUserName());
            }
            if (!TextUtils.isEmpty(builder.getPassword())) {
                mqtt.setPassword(builder.getPassword());
            }
            mqtt.setWillQos(MQTTUtils.convertQoS(builder.getWillQos()));
            if (!TextUtils.isEmpty(builder.getWillMessage())) {
                mqtt.setWillMessage(builder.getWillMessage());
            }
            if (!TextUtils.isEmpty(builder.getWillTopic())) {
                mqtt.setWillTopic(builder.getWillTopic());
            }
            //设置追踪者
            mqtt.setTracer(new FuseSourceTracer());
            //异步不堵塞模式
            callbackConnection = mqtt.callbackConnection();
            callbackConnection.listener(mExtendedListener);
        } catch (URISyntaxException e) {
            throw new WrapMQTTException(e);
        }
    }

    /**
     * 最好时机是在未连接之前设置追踪者
     */
    public void setTracer(Tracer tracer) {
        if (tracer != null) {
            mqtt.setTracer(tracer);
        }
    }



    public void connect(IMQTTCallback<Void> callback) {
        callbackConnection.connect(MQTTUtils.adapterCallback(callback));
    }


    public void subscribe(String topic, QoS qoS, IMQTTCallback<byte[]> callback) {
        callbackConnection.subscribe(new Topic[]{new Topic(topic, MQTTUtils.convertQoS(qoS))},
                MQTTUtils.adapterCallback(callback));
    }


    public void subscribe(String[] topics, QoS[] qoS, IMQTTCallback<byte[]> callback) {
        if (qoS.length != topics.length) {
            throw new IllegalArgumentException("订阅失败，主题长度与质量长度不一致");
        }
        Topic[] desTopics = new Topic[topics.length];
        for (int i = 0; i < desTopics.length; i++) {
            desTopics[i] = new Topic(topics[i], MQTTUtils.convertQoS(qoS[i]));
        }
        callbackConnection.subscribe(desTopics, MQTTUtils.adapterCallback(callback));
    }


    public void unsubscribe(String topics, IMQTTCallback<Void> callback) {
        callbackConnection.unsubscribe(new UTF8Buffer[]{new UTF8Buffer(topics)},
                MQTTUtils.adapterCallback(callback));
    }


    public void unsubscribe(String[] topics, IMQTTCallback<Void> callback) {
        if (topics == null) {
            throw new IllegalArgumentException("取消订阅参数不能为空");
        }
        UTF8Buffer[] utf8Buffers = new UTF8Buffer[topics.length];
        for (int i = 0; i < utf8Buffers.length; i++) {
            utf8Buffers[i] = new UTF8Buffer(topics[i]);
        }
        callbackConnection.unsubscribe(utf8Buffers, MQTTUtils.adapterCallback(callback));
    }


    public void publish(String topic, byte[] payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
        //retain MQTT服务器只会为每一个Topic保存最近收到的一条RETAIN标志位为true的消息！
        // 也就是说，如果MQTT服务器上已经为某个Topic保存了一条Retained消息，
        // 当客户端再次发布一条新的Retained消息，那么服务器上原来的那条消息会被覆盖！
        callbackConnection.publish(topic, payload, MQTTUtils.convertQoS(qos),
                retained, MQTTUtils.adapterCallback(callback));
    }


    public void publish(String topic, String payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
        callbackConnection.publish(new UTF8Buffer(topic), new UTF8Buffer(payload), MQTTUtils.convertQoS(qos),
                retained, MQTTUtils.adapterCallback(callback));
    }


    public void disconnect(IMQTTCallback<Void> callback) {
        callbackConnection.disconnect(MQTTUtils.adapterCallback(callback));
    }


    public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }


    public void disconnectForcibly() throws Exception {
        this.messageReceiver = null;
        callbackConnection.kill(null);
    }


    private ExtendedListener mExtendedListener = new ExtendedListener() {

        @Override
        public void onConnected() {
            MQTTUtils.logD(TAG, "onConnected uri:" + mqtt.getHost().toString());
        }

        @Override
        public void onDisconnected() {
            MQTTUtils.logD(TAG, "onDisconnected uri:" + mqtt.getHost().toString());
        }

        @Override
        public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
            MQTTUtils.logD(TAG, "onPublish 1 topic:" + topic.toString() + ",body:" + body.toString());
            ack.run();
        }

        @Override
        public void onFailure(Throwable value) {
            MQTTUtils.logE(TAG, "onFailure :" + value.toString());
            callbackConnection.disconnect(null);
        }

        @Override
        public void onPublish(UTF8Buffer topic, Buffer body, Callback<Callback<Void>> ack) {
            //收到消息
            MQTTUtils.logD(TAG, "onPublish 2 topic:" + topic.toString() + ",body:" + body.toString());
            if (messageReceiver != null) {
                try {
                    messageReceiver.onReceive(topic.toString(), body.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static class FuseSourceTracer extends Tracer {

        /**
         * Override to log/capture debug level messages
         *
         * @param message
         * @param args
         */
        public void debug(String message, Object... args) {
            MQTTUtils.logD(TAG, "debug :" + Arrays.toString(args));
        }

        /**
         * Called when a MQTTFrame sent to the remote peer.
         *
         * @param frame
         */
        public void onSend(MQTTFrame frame) {
            MQTTUtils.logD(TAG, "onSend :" + frame);
        }

        /**
         * Called when a MQTTFrame is received from the remote peer.
         *
         * @param frame
         */
        public void onReceive(MQTTFrame frame) {
            MQTTUtils.logD(TAG, "onReceive :" + frame);
        }
    }


}
