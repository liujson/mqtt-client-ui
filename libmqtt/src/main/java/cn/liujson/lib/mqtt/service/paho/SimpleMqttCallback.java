package cn.liujson.lib.mqtt.service.paho;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 提供一个简单的空实现
 * @author liujson
 * @date 2021/3/10.
 */
public class SimpleMqttCallback implements MqttCallbackExtended {

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        //do anything
    }

    @Override
    public void connectionLost(Throwable cause) {
        //do anything
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //do anything
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //do anything
    }
}
