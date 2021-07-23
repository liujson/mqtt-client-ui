package cn.liujson.client.ui.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("DELETE FROM connection_profile WHERE id = :id")
    Completable deleteProfile(int id);

    @Query("SELECT * FROM connection_profile WHERE id = :id")
    Single<ConnectionProfile> queryProfileById(int id);

    @Query("SELECT * FROM connection_profile WHERE id = (SELECT connection_profile_id FROM connection_profile_star ORDER BY connection_profile_id DESC LIMIT 1)")
    ConnectionProfile getMarkedStarProfile();

    @Update
    Completable updateProfile(ConnectionProfile profile);


    @Query("SELECT * FROM connection_profile" +
            " INNER JOIN connection_profile_star " +
            "ON connection_profile.id = connection_profile_id")
    Single<List<ConnectionProfile>> queryStarProfileById();

    @Query("SELECT COUNT(*) FROM connection_profile")
    Single<Long> count();
}
