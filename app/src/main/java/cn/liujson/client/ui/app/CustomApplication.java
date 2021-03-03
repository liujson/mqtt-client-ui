package cn.liujson.client.ui.app;

import android.app.Application;

import cn.liujson.client.ui.db.DatabaseHelper;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class CustomApplication extends Application {

    static CustomApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化数据库
        DatabaseHelper.init(this);
    }


    public static CustomApplication getApp() {
        return instance;
    }
}
