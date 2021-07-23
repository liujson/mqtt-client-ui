package com.ubains.lib.mqtt.mod.provider.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cn.liujson.lib.mqtt.api.QoS;


/**
 * MQTT 连接配置信息
 *
 * @author liujson
 * @date 2021/3/2.
 */

public class ConnectionProfile implements Serializable {
    /**
     * 配置名称
     */
    public String profileName;
    /**
     * mqtt broker Address
     */
    public String brokerAddress;

    public int brokerPort;

    public String clientID;

    public boolean cleanSession;

    public String username;

    public String password;
    /**
     * 连接超时时间
     */
    public int connectionTimeout;
    /**
     * 保持存活间隔时间
     */
    public int keepAliveInterval;
    /**
     * 是否自动重连
     */
    public boolean autoReconnect;
    /**
     * 最大重连延时 默认 128000
     */
    public int maxReconnectDelay;
    /**
     * 遗嘱 topic
     */
    public String willTopic;
    /**
     * 遗嘱 message
     */
    public String willMessage;
    /**
     * 遗嘱 Qos
     */
    public QoS willQoS;
    /**
     * 遗嘱消息是否保留
     */
    public boolean willRetained;
    /**
     * 预定义需要订阅的 topicList
     */
    public List<SimpleTopic> defineTopics;

    /**
     * 配置修改时间
     */
    public Date updateTime;
}
