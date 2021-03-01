package cn.liujson.lib.mqtt.util;

import android.os.Build;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.random.RandomStringUtils;
import cn.liujson.lib.mqtt.util.random.UUIDUtils;

/**
 * @author liujson
 * @date 2021/2/21.
 */
public class MQTTUtils {

    private static final String TAG = "MQTTUtils";


    /**
     * 生成一个随机的ClientId(32位)
     *
     * @return
     */
    public static String generateClientId() {
        String uuid = UUIDUtils.uuid();
        String randomStr = RandomStringUtils.randomAscii(8);
        return MD5.encode(uuid + ":" + Build.DEVICE + ":" + randomStr);
    }

    /**
     * QoS convert to org.fusesource.mqtt.client.QoS
     *
     * @param qoS
     * @return
     */
    public static org.fusesource.mqtt.client.QoS convertQoS(QoS qoS) {
        switch (qoS) {
            case AT_MOST_ONCE:
                return org.fusesource.mqtt.client.QoS.AT_MOST_ONCE;
            case EXACTLY_ONCE:
                return org.fusesource.mqtt.client.QoS.EXACTLY_ONCE;
            case AT_LEAST_ONCE:
                return org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE;
            default:
                return org.fusesource.mqtt.client.QoS.AT_MOST_ONCE;
        }
    }

    public static int qoS2Int(QoS qoS) {
        return qoS == null ? 0 : qoS.ordinal();
    }

    public static QoS int2QoS(int qoS) {
        final QoS[] values = QoS.values();
        return qoS > values.length || qoS < 0 ? values[0] : values[qoS];
    }

    public static int[] qoS2IntArr(QoS[] qoSs) {
        int qosArr[] = new int[qoSs.length];
        for (int i = 0; i < qoSs.length; i++) {
            qosArr[i] = qoS2Int(qoSs[i]);
        }
        return qosArr;
    }

    /**
     * 桥接adapter
     *
     * @return
     */
    public static <T> org.fusesource.mqtt.client.Callback<T> adapterCallback(IMQTTCallback<T> iMQTTCallback) {
        return new org.fusesource.mqtt.client.Callback<T>() {
            @Override
            public void onSuccess(T value) {
                if (iMQTTCallback != null) {
                    iMQTTCallback.onSuccess(value);
                }
            }

            @Override
            public void onFailure(Throwable value) {
                if (iMQTTCallback != null) {
                    iMQTTCallback.onFailure(value);
                }
            }
        };
    }

    public static <T> IMqttActionListener adapterActionListener(IMQTTCallback<T> iMQTTCallback) {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                if (iMQTTCallback != null) {
                    iMQTTCallback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                if (iMQTTCallback != null) {
                    iMQTTCallback.onFailure(exception);
                }
            }
        };
    }


    public static void logD(String message) {
        Log.d(TAG, message);
    }

    public static void logE(String message) {
        Log.e(TAG, message);
    }

    public static void logD(String tag, String message) {
        Log.d(tag, message);
    }

    public static void logE(String tag, String message) {
        Log.e(tag, message);
    }

}
