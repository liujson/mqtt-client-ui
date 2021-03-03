package cn.liujson.client.ui.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
}
