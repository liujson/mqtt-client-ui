package cn.liujson.client.ui.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import cn.liujson.client.ui.db.dao.ConnectionProfileDao;
import cn.liujson.client.ui.db.entities.ConnectionProfile;

@Database(entities = {ConnectionProfile.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ConnectionProfileDao connectionProfileDao();
}