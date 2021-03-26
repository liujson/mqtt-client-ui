package cn.liujson.client.ui.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import java.util.Date;

import cn.liujson.client.ui.db.entities.ConnectionProfileStar;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author liujson
 * @date 2021/3/26.
 */
@Dao
public interface ConnectionProfileStarDao {

    @Query("UPDATE connection_profile_star SET connection_profile_id = :profileId,define_topics = :topics,update_date=:updateDate")
    Completable markStar(int profileId, String topics,Date updateDate);

    @Insert()
    Completable insertStar(ConnectionProfileStar users);

    @Query("SELECT COUNT(*) FROM connection_profile_star")
    Single<Long> count();

    @Query("SELECT * FROM connection_profile_star  ORDER BY connection_profile_id DESC LIMIT 1")
    Single<ConnectionProfileStar> getMarkedStar();

    @Query("DELETE FROM connection_profile_star")
    Completable deleteStar();
}
