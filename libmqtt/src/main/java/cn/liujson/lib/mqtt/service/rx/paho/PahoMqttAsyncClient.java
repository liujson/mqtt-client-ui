package cn.liujson.lib.mqtt.service.rx.paho;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * MqttAsyncClient 继承为方便扩展
 * @author liujson
 * @date 2021/3/18.
 * @see org.eclipse.paho.client.mqttv3.MqttAsyncClient
 * @see org.eclipse.paho.client.mqttv3.IMqttAsyncClient
 */
public class PahoMqttAsyncClient extends MqttAsyncClient {

    public PahoMqttAsyncClient(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
    }

    public PahoMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
    }

    public PahoMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender) throws MqttException {
        super(serverURI, clientId, persistence, pingSender);
    }

    public PahoMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender, ScheduledExecutorService executorService) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService);
    }

    public PahoMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender, ScheduledExecutorService executorService, HighResolutionTimer highResolutionTimer) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService, highResolutionTimer);
    }


    @Override
    protected MqttTopic getTopic(String topic) {
        return super.getTopic(topic);
    }
}
