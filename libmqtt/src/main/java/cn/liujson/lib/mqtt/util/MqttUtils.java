package cn.liujson.lib.mqtt.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.ArrayList;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.Message;
import cn.liujson.lib.mqtt.util.random.RandomStringUtils;
import cn.liujson.lib.mqtt.util.random.UUIDUtils;

/**
 * @author liujson
 * @date 2021/2/21.
 */
public class MqttUtils {

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


    /**
     * 判断服务是否在运行
     * @param context
     * @param serviceName
     * @return
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(50);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ConnectionParams 转 MqttConnectOptions
     * @param params
     * @return
     */
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
        if (!TextUtils.isEmpty(params.getUsername())) {
            options.setUserName(params.getUsername());
        }
        if (!TextUtils.isEmpty(params.getPassword())) {
            assert params.getPassword() != null;
            options.setPassword(params.getPassword().toCharArray());
        }
        options.setCleanSession(params.isCleanSession());
        options.setMqttVersion(params.getMqttVersion());
        return options;
    }
}
