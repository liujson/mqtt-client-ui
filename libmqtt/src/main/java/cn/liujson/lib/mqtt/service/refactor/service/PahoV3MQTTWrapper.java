package cn.liujson.lib.mqtt.service.refactor.service;

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

import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.util.MQTTUtils;
import io.reactivex.annotations.NonNull;

/**
 * Paho MQTT Wrapper
 *
 * @author liujson
 * @date 2021/3/8.
 */
public class PahoV3MQTTWrapper implements IMQTTWrapper<PahoV3MQTTClient> {

    private static final String TAG = "PahoV3MQTTWrapper";

    private final PahoV3MQTTClient pahoV3MQTTClient;

    /**
     * 重连线程和Handler
     */
    private HandlerThread retryHandlerThread;
    private Handler retryHandle;
    /**
     * 消息接收
     */
    private IMQTTMessageReceiver messageReceiver;

    public PahoV3MQTTWrapper(final IMQTTBuilder builder) throws WrapMQTTException {
        try {
            pahoV3MQTTClient = new PahoV3MQTTClient(builder);
            //监听回调
            pahoV3MQTTClient.setCallback(mMqttCallback);
        } catch (Exception e) {
            throw new WrapMQTTException(e);
        }
    }

    @NonNull
    @Override
    public PahoV3MQTTClient getClient() {
        return pahoV3MQTTClient;
    }

    @Override
    public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public void destroy() {
        stopRetryTask();
    }

    @Override
    public String toString() {
        return "PahoV3MQTTWrapper{" + getClient().toString() + "}";
    }

    /**
     * 启动重连定时任务
     */
    private void startRetryTask() {
        if (retryHandlerThread == null) {
            retryHandlerThread = new HandlerThread("PahoRetry");
            retryHandlerThread.start();
            retryHandle = new Handler(retryHandlerThread.getLooper());
        }
        retryDelayed();
    }

    /**
     * 结束重连任务
     */
    private void stopRetryTask() {
        if (retryHandlerThread != null) {
            retryHandle.removeCallbacks(retryRunning);
            retryHandlerThread.quitSafely();
            retryHandlerThread = null;
            retryHandle = null;
        }
    }

    /**
     * 重试自动计算延时时间
     */
    private void retryDelayed() {
        if (retryHandle != null) {
            retryHandle.postDelayed(retryRunning, calculateRetryTime());
        }
    }


    /**
     * 计算下次重连需要等待的时间
     *
     * @return
     */
    private long calculateRetryTime() {
        return 3000;
    }

    /**
     * 监听回调
     */
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
            MQTTUtils.logD(TAG, "messageArrived");
            MQTTUtils.logD(TAG, "messageArrived detail,topic:" + topic +
                    ",Qos:" + message.getQos() + ",length:" + message.getPayload().length);
            //消息抵达
            if (messageReceiver != null) {
                messageReceiver.onReceive(topic, message.getPayload());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            MQTTUtils.logD(TAG, "deliveryComplete");
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
     * 重连任务 Runnable
     */
    private final Runnable retryRunning = () -> {
        try {
            getClient().connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    MQTTUtils.logD(TAG, "retryRunning reconnect success.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    MQTTUtils.logE(TAG,
                            "retryRunning reconnect failure.(" + exception.toString() + ")");
                    //继续重试
                    retryDelayed();
                }
            });
        } catch (MqttException e) {
            MQTTUtils.logE(TAG,
                    "retryRunning reconnect failure.(" + e.toString() + ")");
            //继续重试
            retryDelayed();
        }
    };

}
