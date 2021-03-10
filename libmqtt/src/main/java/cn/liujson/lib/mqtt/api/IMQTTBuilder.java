package cn.liujson.lib.mqtt.api;

/**
 * MQTT 连接参数配置类
 * 默认使用UTF-8编码
 *
 * @author liujson
 * @date 2021/2/21.
 */
public interface IMQTTBuilder {
    /**
     * Host 主机地址 例如: tcp://192.168.0.193:1883
     *
     * @return
     */
    String getHost();

    /**
     * ClientId（这是MQTT服务器用来标识Session的，正在使用的会话的东西），最好ID不得超过23个字符
     *
     * @return ClientId
     */
    String getClientId();

    /**
     * 如果希望MQTT服务器跨客户端会话持久化主题订阅和ack位置，则将其设置为false。默认值为true。
     *
     * @return
     */
    boolean isCleanSession();

    /**
     * 配置Keep Alive定时器(以秒为单位)。
     * 定义从客户端接收消息之间的最大时间间隔。
     * 它使服务器能够检测到到客户机的网络连接已经断开，而不必等待漫长的TCP/IP超时。
     *
     * @return KeepAlive
     */
    int getKeepAlive();

    /**
     * 设置用于针对服务器进行身份验证的用户名。
     *
     * @return UserName 默认 null
     */
    String getUserName();

    /**
     * 设置用于针对服务器进行身份验证的密码。
     *
     * @return UserName 默认 null
     */
    String getPassword();

    /**
     * 遗嘱Topic 如果设置，如果客户端有意外的断开连接，服务器将发布客户端的will消息到指定的主题。
     *
     * @return 默认 null
     */
    String getWillTopic();

    /**
     * 遗嘱消息 Defaults to a zero length message
     *
     * @return 默认发送长度为0消息
     */
    String getWillMessage();

    /**
     * 遗嘱消息的消息质量 Defaults to QoS.AT_MOST_ONCE.
     *
     * @return 默认为 QoS.AT_MOST_ONCE
     */
    QoS getWillQos();

    /**
     * 是否自动断线重连(重连最大周期是1000-12800毫秒)
     */
    boolean isAutoReconnect();
}
