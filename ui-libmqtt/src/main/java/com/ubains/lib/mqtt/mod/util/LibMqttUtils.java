package com.ubains.lib.mqtt.mod.util;

import android.text.TextUtils;


import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;


import org.eclipse.paho.client.mqttv3.MqttException;


import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;


/**
 * @author liujson
 * @date 2021/4/22.
 */
public class LibMqttUtils {

    public static ConnectionParams profile2Params(ConnectionProfile profile) {
        // TODO: 2021/3/19  配置连接参数
        ConnectionParams.Builder builder = ConnectionParams.newBuilder()
                .serverURI(profile.brokerAddress + ":" + profile.brokerPort)
                .cleanSession(profile.cleanSession)
                .automaticReconnect(profile.autoReconnect)
                .maxReconnectDelay(profile.maxReconnectDelay)
                .keepAlive(profile.keepAliveInterval)
                .connectionTimeout(profile.connectionTimeout)
                .clientId(profile.clientID)
                .username(profile.username)
                .password(profile.password);
        if (!TextUtils.isEmpty(profile.willTopic) && !TextUtils.isEmpty(profile.willMessage)) {
            builder.setWill(profile.willTopic, profile.willMessage.getBytes(), profile.willQoS, profile.willRetained);
        }
        return builder.build();
    }
}
