package com.ubains.lib.mqtt.mod.service;


import com.ubains.android.ubutil.comm.LogUtil;
import com.ubains.lib.mqtt.mod.provider.event.MqttConnectCompleteEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttConnectionLostEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttMessageArrivedEvent;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;


import java.util.Arrays;


import cn.liujson.lib.mqtt.api.backruning.AbstractPahoConnServiceBinder;


/**
 * @author liujson
 * @date 2021/3/8.
 */
public class ConnectionBinder extends AbstractPahoConnServiceBinder {

    private static final String TAG = "ConnectionBinder";

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String msg = new String(message.getPayload());
        LogUtil.d(TAG, "MQTT 消息抵达，topic:" + topic + ",message:" + msg);
        EventBus.getDefault().post(new MqttMessageArrivedEvent(topic, msg, message.getQos()));
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        final String msg = "MQTT 连接完成，是否重连：" + reconnect + ",server uri:" + serverURI;
        LogUtil.d(TAG, msg);
        //发送粘性事件
        EventBus.getDefault().postSticky(new MqttConnectCompleteEvent(reconnect, serverURI));
    }

    @Override
    public void connectionLost(Throwable cause) {
        super.connectionLost(cause);
        final String msg = "MQTT 失去连接:" + cause.toString();
        LogUtil.e(TAG, msg);
        //发送粘性事件
        EventBus.getDefault().postSticky(new MqttConnectionLostEvent(cause));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        super.deliveryComplete(token);
        try {
            LogUtil.d(TAG, "MQTT 发布成功,topic:" + Arrays.toString(token.getTopics()) +
                    ",message:" + new String(token.getMessage().getPayload()));
        } catch (Exception e) {
            LogUtil.d(TAG, "MQTT 消息发布成功");
        }
    }
}
