package cn.liujson.lib.mqtt.service.refactor.service;

import android.os.Handler;
import android.os.HandlerThread;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.adapter.SimpleMqttCallback;
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

    public PahoV3MQTTWrapper(final IMQTTBuilder builder) throws WrapMQTTException {
        try {
            pahoV3MQTTClient = new PahoV3MQTTClient(builder);
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
        pahoV3MQTTClient.setMessageReceiver(messageReceiver);
    }

    @Override
    public void destroy() {
//        stopRetryTask();
        pahoV3MQTTClient.setMessageReceiver(null);
        pahoV3MQTTClient.setCallback(null);
    }

    @Override
    public String toString() {
        return "PahoV3MQTTWrapper{" + getClient().toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PahoV3MQTTWrapper that = (PahoV3MQTTWrapper) o;
        return pahoV3MQTTClient == that.pahoV3MQTTClient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pahoV3MQTTClient, retryHandlerThread, retryHandle, retryRunning);
    }


    //region ============自定义重连相关=====================================
    /**
     * 重连线程和Handler
     */
    private HandlerThread retryHandlerThread;
    private Handler retryHandle;

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


    //endregion ============自定义重连相关=====================================

}
