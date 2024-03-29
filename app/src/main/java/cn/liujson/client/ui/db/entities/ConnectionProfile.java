package cn.liujson.client.ui.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import cn.liujson.lib.mqtt.api.QoS;

/**
 * MQTT 连接配置信息
 *
 * @author liujson
 * @date 2021/3/2.
 */
@Entity(tableName = "connection_profile")
public class ConnectionProfile {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "profile_name")
    public String profileName;
    @ColumnInfo(name = "broker_address")
    public String brokerAddress;
    @ColumnInfo(name = "broker_port")
    public int brokerPort;
    @ColumnInfo(name = "client_id")
    public String clientID;
    @ColumnInfo(name = "clean_session")
    public boolean cleanSession;
    @ColumnInfo(name = "username")
    public String username;
    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "connection_timeout")
    public int connectionTimeout;
    @ColumnInfo(name = "keep_alive_interval")
    public int keepAliveInterval;
    @ColumnInfo(name = "auto_reconnect")
    public boolean autoReconnect;
    @ColumnInfo(name = "max_reconnect_delay", defaultValue = "128000")
    public int maxReconnectDelay;

    @ColumnInfo(name = "create_date")
    public Date createDate;
    @ColumnInfo(name = "update_date")
    public Date updateDate;

    @ColumnInfo(name = "will_topic")
    public String willTopic;
    @ColumnInfo(name = "will_message")
    public String willMessage;
    @ColumnInfo(name = "will_qos")
    public QoS willQoS;
    @ColumnInfo(name = "will_retained")
    public boolean willRetained;
    /**
     * 1 server signed ; 2 client signed
     */
    @ColumnInfo(name = "certificate_signed")
    public int certificateSigned;
    @ColumnInfo(name = "ssl_secure")
    public boolean sslSecure;
    @ColumnInfo(name = "ca_file_path")
    public String caFilePath;
    @ColumnInfo(name = "client_certificate_file_path")
    public String clientCertificateFilePath;
    @ColumnInfo(name = "client_key_file_path")
    public String clientKeyFilePath;


}
