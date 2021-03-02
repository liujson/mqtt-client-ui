package cn.liujson.client.ui.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cn.liujson.client.ui.db.entities.ConnectionProfile;

/**
 * @author liujson
 * @date 2021/3/2.
 */
@Dao
public interface ConnectionProfileDao {

    @Query("SELECT * FROM connection_profile")
    List<ConnectionProfile> getAll();

    @Insert
    void insertAll(ConnectionProfile... connectionProfiles);

    @Delete
    void delete(ConnectionProfile connectionProfile);


}
