package cn.liujson.lib.mqtt.service.rx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.NetworkModuleService;

import java.io.Serializable;
import java.util.Objects;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MQTTUtils;

/**
 * MQTT 客户端的连接参数
 * 需要使用者根据自身产品的使用常见动态调整连接各项参数
 *
 * @author liujson
 * @date 2021/3/17.
 */
public class ConnectionParams implements Serializable {
    /**
     * 客户端与服务端默认心跳时间间隔（单位秒）
     */
    public static final int KEEP_ALIVE_INTERVAL_DEFAULT = 15;
    /**
     * 默认打开 cleanSession
     */
    public static final boolean CLEAN_SESSION_DEFAULT = true;
    /**
     * 默认连接超时间（单位秒）
     */
    public static final int CONNECTION_TIMEOUT_DEFAULT = 10;
    /**
     * 默认自动重连最大时间延时（单位毫秒）
     */
    public static final int MAX_RECONNECT_DELAY_DEFAULT = 128000;
    /**
     * 我们可以在未收到确认的情况下发送多少条消息。可以在不收到确认的情况下发送最大消息限制。
     */
    public static final int MAX_INFLIGHT_DEFAULT = 10;
    /**
     * 默认MqtVersion是第3.1.1，如果失败，则回落至3.1
     */
    public static final int MQTT_VERSION_DEFAULT = 0;
    /**
     * Mqtt Version 3.1
     */
    public static final int MQTT_VERSION_3_1 = 3;
    /**
     * Mqtt Version 3.1.1
     */
    public static final int MQTT_VERSION_3_1_1 = 4;


    private final String clientId;
    private final boolean cleanSession;
    private final int keepAlive;
    private final String username;
    private final String password;
    private final boolean automaticReconnect;
    private final int maxReconnectDelay;
    private final String willTopic;
    private final Message willMessage;
    private final int connectionTimeout;
    private final String[] serverURIs;
    private final int mqttVersion;

    public ConnectionParams(Builder builder) {
        this.clientId = builder.clientId;
        this.cleanSession = builder.cleanSession;
        this.keepAlive = builder.keepAlive;
        this.username = builder.username;
        this.password = builder.password;
        this.automaticReconnect = builder.automaticReconnect;
        this.maxReconnectDelay = builder.maxReconnectDelay;
        this.willTopic = builder.willTopic;
        this.willMessage = builder.willMessage;
        this.serverURIs = builder.serverURIs;
        this.connectionTimeout = builder.connectionTimeout;
        this.mqttVersion = builder.mqttVersion;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    @NonNull
    public String getClientId() {
        return clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    public int getMaxReconnectDelay() {
        return maxReconnectDelay;
    }

    @Nullable
    public String getWillTopic() {
        return willTopic;
    }

    @Nullable
    public Message getWillMessage() {
        return willMessage;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @NonNull
    public String[] getServerURIs() {
        return serverURIs;
    }

    public int getMqttVersion() {
        return mqttVersion;
    }

    public static class Builder {
        String clientId;
        boolean cleanSession = CLEAN_SESSION_DEFAULT;
        int keepAlive = KEEP_ALIVE_INTERVAL_DEFAULT;
        String username;
        String password;
        boolean automaticReconnect;
        int maxReconnectDelay = MAX_RECONNECT_DELAY_DEFAULT;
        String willTopic;
        Message willMessage;
        int connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
        String[] serverURIs = null;
        int mqttVersion = MQTT_VERSION_DEFAULT;
        //-------------------------------------------------------------
        /**
         * 我们可以在未收到确认的情况下发送多少条消息。可以在不收到确认的情况下发送最大消息限制。
         */
        int maxInflight = MAX_INFLIGHT_DEFAULT;
        /**
         * SocketFactory
         */
        SocketFactory socketFactory;
        /**
         * 连接的 SSL 属性
         */
        Properties sslClientProps = null;
        /**
         * Https Hostname Verification 是否启用
         */
        boolean httpsHostnameVerificationEnabled = true;
        /**
         * HostnameVerifier
         */
        HostnameVerifier sslHostnameVerifier = null;
        /**
         * 自定义的 Web SocketHeader
         */
        Properties customWebSocketHeaders = null;

        /**
         * 服务端Uri
         */
        public Builder serverURI(String serverURI) {
            setServerURIs(new String[]{serverURI});
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        /**
         * 设置“保持存活”间隔。
         *
         * @param keepAlive 客户端与服务端ping消息的时间间隔，单位是秒，设置为0时表示禁用
         * @return Builder
         */
        public Builder keepAlive(int keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * 自动重连（默认false）
         */
        public Builder automaticReconnect(boolean automaticReconnect) {
            this.automaticReconnect = automaticReconnect;
            return this;
        }

        /**
         * 重连的最大等待时间（单位ms），当设置了断线自动重连（automaticReconnect）后，
         * 会每个一段时间进行自动重连，如果连不上则进行延时后再尝试（延时算法：指数补偿），
         * 当达到最大时延（maxReconnectDelay）后这个过程会重新来，直到又达到最大时延，如此周期。
         *
         * @param maxReconnectDelay 默认 128000
         * @return Builder
         */
        public Builder maxReconnectDelay(int maxReconnectDelay) {
            this.maxReconnectDelay = maxReconnectDelay;
            return this;
        }

        /**
         * 设置遗嘱消息
         */
        public Builder setWill(@NonNull String willTopic, @NonNull Message willMessage) {
            Objects.requireNonNull(willTopic);
            Objects.requireNonNull(willMessage);
            this.willTopic = willTopic;
            this.willMessage = willMessage;
            return this;
        }

        /**
         * 设置遗嘱消息
         */
        public Builder setWill(@NonNull String willTopic, @NonNull byte[] payload, @NonNull QoS qos,
                               boolean retained) {
            Objects.requireNonNull(willTopic);
            Objects.requireNonNull(payload);
            Objects.requireNonNull(qos);
            this.willTopic = willTopic;
            this.willMessage = Message.newMessage()
                    .setPayload(payload)
                    .setQos(qos)
                    .setRetained(retained);
            return this;
        }

        /**
         * 它会覆盖MQTT client上设置的 serverURI
         * serverURIs 支持两种类型的连接｛tcp://localhost:1883,ssl://localhost:8883｝如果没有指定端口
         * tcp:// 默认使用1883 ，ssl:// 默认 8883
         * 客户端连接时会从第一个开始尝试，直到连接成功，或者所有都失败；如果其中地址指向不同的服务器，
         * cleanSession应当设置为false
         *
         * @param serverURIs
         */
        public Builder setServerURIs(String[] serverURIs) {
            for (String serverURI : serverURIs) {
                NetworkModuleService.validateURI(serverURI);
            }
            this.serverURIs = serverURIs.clone();
            return this;
        }

        /**
         * 设置连接超时时间（默认10秒）
         */
        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * 设置MQTT版本。默认操作是与版本 3.1.1 连接，
         * 如果失败，则返回到 3.1。
         * 版本 3.1.1 或 3.1 可以分别使用MQTT_VERSION_3_1_1或MQTT_VERSION_3_1选项来具体选择，无需回退。
         */
        public Builder mqttVersion(int mqttVersion) {
            if (mqttVersion != MQTT_VERSION_DEFAULT && mqttVersion != MQTT_VERSION_3_1
                    && mqttVersion != MQTT_VERSION_3_1_1) {
                throw new IllegalArgumentException(
                        "An incorrect version was used \"" + mqttVersion + "\". Acceptable version options are "
                                + MQTT_VERSION_DEFAULT + ", " + MQTT_VERSION_3_1 + " and " + MQTT_VERSION_3_1_1 + ".");
            }
            this.mqttVersion = mqttVersion;
            return this;
        }

        public ConnectionParams build() {
            if (serverURIs == null || serverURIs.length == 0) {
                throw new NullPointerException("serverURIs must can not be null.");
            }
            if (clientId == null) {
                clientId = MQTTUtils.generateClientId();
            }
            if (willTopic != null && willMessage != null) {
                validateWill(willTopic, willMessage.getPayload());
            }
            return new ConnectionParams(this);
        }


        private void validateWill(String dest, Object payload) {
            if ((dest == null) || (payload == null)) {
                throw new IllegalArgumentException();
            }

            MqttTopic.validate(dest, false/* wildcards NOT allowed */);
        }

    }
}
