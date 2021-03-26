package cn.liujson.client.ui.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class DatabaseHelper {


    private DatabaseHelper() {

    }

    private static AppDatabase instance;

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app-database")
                    .build();
        }
    }

    public static AppDatabase getInstance() {
        return instance;
    }
}
