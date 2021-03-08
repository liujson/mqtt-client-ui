package cn.liujson.lib.mqtt.service.refactor.service;

import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;

import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.util.MQTTUtils;

/**
 * Paho MQTT Wrapper
 *
 * @author liujson
 * @date 2021/3/8.
 */
public class PahoV3MQTTWrapper implements IMQTTWrapper<PahoV3MQTTClient> {
    /**
     * 连接超时时间
     */
    public static final int CONNECTION_TIMEOUT = 20;

    private final PahoV3MQTTClient pahoV3MQTTClient;

    private final IMQTTBuilder mBuilder;

    private final MqttConnectOptions connOpts;

    public PahoV3MQTTWrapper(final IMQTTBuilder builder) throws WrapMQTTException {

        Objects.requireNonNull(builder.getHost());
        this.mBuilder = builder;
        String clientId = builder.getClientId();
        //如果clientId是空或者null，生成一个随机的clientId
        if (TextUtils.isEmpty(clientId)) {
            clientId = MQTTUtils.generateClientId();
        }
        try {
            /**
             * MemoryPersistence设置clientid的保存形式，默认为以内存保存
             */
            MemoryPersistence persistence = new MemoryPersistence();
            pahoV3MQTTClient = new PahoV3MQTTClient(builder.getHost(),
                    clientId, persistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(builder.isCleanSession());

            if (!TextUtils.isEmpty(builder.getUserName())) {
                connOpts.setUserName(builder.getUserName());
            }
            if (!TextUtils.isEmpty(builder.getPassword())) {
                connOpts.setPassword(builder.getPassword().toCharArray());
            }
            if (!TextUtils.isEmpty(builder.getWillTopic())
                    && !TextUtils.isEmpty(builder.getWillMessage())
                    && builder.getWillQos() != null) {
                connOpts.setWill(builder.getWillTopic(),
                        builder.getWillMessage().getBytes(),
                        MQTTUtils.qoS2Int(builder.getWillQos()), false);
            }
            //连接超时时间（秒）
            connOpts.setConnectionTimeout(CONNECTION_TIMEOUT);
            //保持存活间隔（秒）
            connOpts.setKeepAliveInterval(builder.getKeepAlive());

            //自动重连
            connOpts.setAutomaticReconnect(true);

        } catch (MqttException e) {
            throw new WrapMQTTException(e);
        }
    }

    @Override
    public PahoV3MQTTClient getClient() {
        return pahoV3MQTTClient;
    }



}
