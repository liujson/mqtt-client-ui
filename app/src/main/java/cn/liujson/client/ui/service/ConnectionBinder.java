package cn.liujson.client.ui.service;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.api.backruning.AbstractPahoConnServiceBinder;
import cn.liujson.lib.mqtt.util.MqttUtils;
import cn.liujson.logger.LogUtils;


/**
 * @author liujson
 * @date 2021/3/8.
 */
public class ConnectionBinder extends AbstractPahoConnServiceBinder {


    final List<OnRecMsgListener> recMsgListenerList = new ArrayList<>();
    final List<OnConnectedListener> connectedListenerList = new ArrayList<>();

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogUtils.d("MQTT 消息抵达，topic:" + topic + ",message:" + new String(message.getPayload()));
        //接受到消息会回调这里
        final Iterator<OnRecMsgListener> it = recMsgListenerList.iterator();
        while (it.hasNext()) {
            it.next().onReceiveMessage(topic, message.getPayload(), MqttUtils.int2QoS(message.getQos()));
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        LogUtils.d("MQTT 连接完成，是否重连：" + reconnect + ",server uri:" + serverURI);
        if (reconnect) {
            // 如果cleanSession是true,重连后需要重新订阅topic
        }
        final Iterator<OnConnectedListener> it = connectedListenerList.iterator();
        while (it.hasNext()) {
            it.next().onConnectComplete(reconnect, serverURI);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        super.connectionLost(cause);
        LogUtils.d("MQTT 失去连接：" + cause.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        super.deliveryComplete(token);
        try {
            LogUtils.d("MQTT 发布成功,topic:" + Arrays.toString(token.getTopics()) +
                    ",message:" + new String(token.getMessage().getPayload()));
        } catch (Exception e) {
            LogUtils.d("MQTT 消息发布成功");
        }
    }


    public void addOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        if (!recMsgListenerList.contains(recMsgListener)) {
            recMsgListenerList.add(recMsgListener);
        }
    }

    public void removeOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        recMsgListenerList.remove(recMsgListener);
    }

    public void addOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        if (!connectedListenerList.contains(connectedListener)) {
            connectedListenerList.add(connectedListener);
        }
    }

    public void removeOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        connectedListenerList.remove(connectedListener);
    }

    //-------------------------------------------------------------------------------------------

    public interface OnRecMsgListener {
        /**
         * 接收到消息
         */
        void onReceiveMessage(String topic, byte[] payload, QoS qoS);
    }

    public interface OnConnectedListener {
        /**
         * 连接成功
         */
        void onConnectComplete(boolean reconnect, String serverURI);
    }

    //-------------------------------------------------------------------------------------------
}
