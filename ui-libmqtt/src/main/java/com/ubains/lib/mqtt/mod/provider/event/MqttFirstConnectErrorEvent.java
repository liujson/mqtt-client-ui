package com.ubains.lib.mqtt.mod.provider.event;

import androidx.annotation.NonNull;

/**
 * MQTT 首次连接异常
 *
 * @author liujson
 * @date 2021/6/1.
 */
public class MqttFirstConnectErrorEvent {

    private final Throwable throwable;

    public MqttFirstConnectErrorEvent(@NonNull Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
