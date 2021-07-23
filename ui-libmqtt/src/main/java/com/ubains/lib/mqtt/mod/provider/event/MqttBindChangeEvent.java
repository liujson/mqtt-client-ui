package com.ubains.lib.mqtt.mod.provider.event;

/**
 * mqtt 绑定事件
 *
 * @author liujson
 * @date 2021/5/11.
 */
public class MqttBindChangeEvent {

    private boolean bind;

    public MqttBindChangeEvent(boolean bind) {
        this.bind = bind;
    }

}
