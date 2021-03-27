package cn.liujson.client.ui.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import cn.liujson.client.ui.db.converter.CommConverter;
import cn.liujson.client.ui.db.dao.ConnectionProfileDao;
import cn.liujson.client.ui.db.dao.ConnectionProfileStarDao;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.db.entities.ConnectionProfileStar;


@Database(entities = {ConnectionProfile.class,
        ConnectionProfileStar.class}, version = 1, exportSchema = false)
@TypeConverters(CommConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ConnectionProfileDao connectionProfileDao();

    public abstract ConnectionProfileStarDao starDao();
}