package cn.liujson.client.ui.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cn.liujson.client.ui.db.entities.ConnectionProfile;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author liujson
 * @date 2021/3/2.
 */
@Dao
public interface ConnectionProfileDao {

    @Query("SELECT * FROM connection_profile")
    List<ConnectionProfile> getAll();

    @Insert
    long[] insertAll(ConnectionProfile... connectionProfiles);

    @Insert
    long insert(ConnectionProfile connectionProfile);

    @Delete
    void delete(ConnectionProfile connectionProfile);


    @Insert()
    Completable insertProfile(ConnectionProfile users);

    @Query("SELECT * FROM connection_profile")
    Single<List<ConnectionProfile>> loadProfiles();
}
