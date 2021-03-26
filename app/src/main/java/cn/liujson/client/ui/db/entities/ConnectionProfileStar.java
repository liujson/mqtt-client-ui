package cn.liujson.client.ui.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

/**
 * 星号标记表
 *
 * @author liujson
 * @date 2021/3/26.
 */
@Entity(tableName = "connection_profile_star",
        foreignKeys = @ForeignKey(entity = ConnectionProfile.class, parentColumns = "id", childColumns = "connection_profile_id", onDelete = CASCADE)
        , indices = @Index(value = {"connection_profile_id"}, unique = true))
public class ConnectionProfileStar implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    /**
     * 预定义的Topic列表，json格式存储
     */
    @ColumnInfo(name = "define_topics")
    public String defineTopics;
    @ColumnInfo(name = "connection_profile_id")
    public int connectionProfileId;
    @ColumnInfo(name = "update_date")
    public Date updateDate;
}
