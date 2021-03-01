package cn.liujson.lib.mqtt.service;

import cn.liujson.lib.mqtt.api.IMQTTConnectionBuilder;
import cn.liujson.lib.mqtt.api.QoS;

/**
 * 连接参数配置
 *
 * @author liujson
 * @date 2021/2/21.
 */
public class MqttBuilder implements IMQTTConnectionBuilder {
    /**
     * host为主机名和端口
     */
    private String host;
    /**
     * clientid即连接MQTT的客户端ID，一般以唯一标识符表示
     * 服务器必须允许长度在1到23个UTF-8编码字节之间的clientid（可以更长），并且只包含字符
     * "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" [MQTT-3.1.3-5]。
     */
    private String clientId;
    /**
     * 设置是否清空session,false表示服务器会保留客户端的连接记录，true表示每次连接到服务器都以新的身份连接
     */
    private boolean cleanSession = true;
    /**
     * 设置会话心跳时间 单位为秒 服务器会每隔(1.5*keepAlive)秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
     */
    private int keepAlive = 10;
    /**
     * 连接的用户名
     */
    private String userName;
    /**
     * 连接的密码
     */
    private String password;
    /**
     * 遗嘱消息相关===================================
     */
    private String willTopic;
    private String willMessage;
    private QoS willQos = QoS.AT_MOST_ONCE;

    public MqttBuilder host(String host) {
        this.host = host;
        return this;
    }

    public MqttBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public MqttBuilder cleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public MqttBuilder cleanSession(int keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public MqttBuilder userName(String userName) {
        this.userName = userName;
        return this;
    }

    public MqttBuilder password(String password) {
        this.password = password;
        return this;
    }

    public MqttBuilder willTopic(String willTopic) {
        this.willTopic = willTopic;
        return this;
    }

    public MqttBuilder willMessage(String willMessage) {
        this.willMessage = willMessage;
        return this;
    }

    public MqttBuilder willQos(QoS willQos) {
        this.willQos = willQos;
        return this;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public boolean isCleanSession() {
        return cleanSession;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getWillTopic() {
        return willTopic;
    }

    @Override
    public String getWillMessage() {
        return willMessage;
    }

    @Override
    public QoS getWillQos() {
        return willQos;
    }
}