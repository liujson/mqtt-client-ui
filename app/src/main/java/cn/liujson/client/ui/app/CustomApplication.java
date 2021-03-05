package cn.liujson.client.ui.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.service.MqttServiceManager;

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
        //启动Mqtt服务
        MqttServiceManager.getInstance().bindToApplication(this);
    }

    public static CustomApplication getApp() {
        return instance;
    }
}
