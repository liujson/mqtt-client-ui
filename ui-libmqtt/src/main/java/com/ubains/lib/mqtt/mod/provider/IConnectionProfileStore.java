package com.ubains.lib.mqtt.mod.provider;

import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;

/**
 * MQTT 连接属性存储接口
 *
 * @author liujson
 * @date 2021/7/15.
 */
public interface IConnectionProfileStore {
    /**
     * 保存
     */
    boolean store(ConnectionProfile connectionProfile);


    /**
     * 加载
     */
    ConnectionProfile load();
}
