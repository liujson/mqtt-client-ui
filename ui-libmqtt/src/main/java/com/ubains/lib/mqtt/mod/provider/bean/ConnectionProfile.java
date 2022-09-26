package com.ubains.lib.mqtt.mod.provider.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    /**
     * 1 server signed ; 2 client signed
     */
    public int certificateSigned;
    public boolean sslSecure;
    public String caFilePath;
    public String clientCertificateFilePath;
    public String clientKeyFilePath;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionProfile that = (ConnectionProfile) o;
        return brokerPort == that.brokerPort && cleanSession == that.cleanSession && connectionTimeout == that.connectionTimeout && keepAliveInterval == that.keepAliveInterval && autoReconnect == that.autoReconnect && maxReconnectDelay == that.maxReconnectDelay && willRetained == that.willRetained && certificateSigned == that.certificateSigned && sslSecure == that.sslSecure && Objects.equals(profileName, that.profileName) && Objects.equals(brokerAddress, that.brokerAddress) && Objects.equals(clientID, that.clientID) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(willTopic, that.willTopic) && Objects.equals(willMessage, that.willMessage) && willQoS == that.willQoS && Objects.equals(defineTopics, that.defineTopics) && Objects.equals(updateTime, that.updateTime) && Objects.equals(caFilePath, that.caFilePath) && Objects.equals(clientCertificateFilePath, that.clientCertificateFilePath) && Objects.equals(clientKeyFilePath, that.clientKeyFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileName, brokerAddress, brokerPort, clientID, cleanSession, username, password, connectionTimeout, keepAliveInterval, autoReconnect, maxReconnectDelay, willTopic, willMessage, willQoS, willRetained, defineTopics, updateTime, certificateSigned, sslSecure, caFilePath, clientCertificateFilePath, clientKeyFilePath);
    }
}
