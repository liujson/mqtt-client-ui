package cn.liujson.lib.mqtt.service.refactor.service;


import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

import cn.liujson.lib.mqtt.api.QoS;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public class PahoV3MQTTClient extends MqttAsyncClient {

    /**
     * 已经订阅的topic
     */
    private final HashMap<String, QoS> activeSubs = new HashMap<>();

    public PahoV3MQTTClient(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
    }

    public PahoV3MQTTClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
    }

    public PahoV3MQTTClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender) throws MqttException {
        super(serverURI, clientId, persistence, pingSender);
    }

    public PahoV3MQTTClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender, ScheduledExecutorService executorService) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService);
    }

    public PahoV3MQTTClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender, ScheduledExecutorService executorService, HighResolutionTimer highResolutionTimer) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService, highResolutionTimer);
    }


}
