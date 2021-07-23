package com.ubains.lib.mqtt.mod.ui.vm;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import cn.liujson.lib.mqtt.util.MqttUtils;

/**
 * @author liujson
 * @date 2021/7/22.
 */
public class MqttSettingObservableEntity {

    public final ObservableBoolean fieldProfileVisible = new ObservableBoolean(false);
    public final ObservableField<String> fieldProfileName = new ObservableField<>("");
    public final ObservableField<String> fieldBrokerAddress = new ObservableField<>();
    public final ObservableField<String> fieldBrokerPort = new ObservableField<>("1883");
    public final ObservableField<String> fieldClientID = new ObservableField<>(MqttUtils.generateClientId());
    public final ObservableField<String> fieldUsername = new ObservableField<>();
    public final ObservableField<String> fieldPassword = new ObservableField<>();
    public final ObservableField<String> fieldKeepAliveInterval = new ObservableField<>("60");
    public final ObservableField<String> fieldConnectionTimeout = new ObservableField<>("15");
    public final ObservableField<String> fieldMaxReconnectDelay = new ObservableField<>(String.valueOf(128000));

    public final ObservableBoolean fieldCleanSession = new ObservableBoolean(true);
    public final ObservableBoolean fieldAutoReconnect = new ObservableBoolean(true);

    public final ObservableField<String> fieldLwtTopic = new ObservableField<>();
    public final ObservableField<String> fieldLwtMessage = new ObservableField<>();
    public final ObservableBoolean fieldLwtRetained = new ObservableBoolean();


    /**
     * 生成随机 ClientID
     */
    public final void generate() {
       fieldClientID.set(MqttUtils.generateClientId());
    }
}
