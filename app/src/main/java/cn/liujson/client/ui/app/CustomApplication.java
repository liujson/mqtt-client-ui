package cn.liujson.client.ui.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.service.ConnectionService;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class CustomApplication extends Application {

    static CustomApplication instance;

    static ConnectionService.ConnectionServiceBinder binder;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化数据库
        DatabaseHelper.init(this);

        testService();
    }

    private void testService() {
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("aaa", "onServiceConnected: ");

                binder = (ConnectionService.ConnectionServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("aaa", "onServiceDisconnected: ");
            }
        }, BIND_AUTO_CREATE);
    }

    public static ConnectionService.ConnectionServiceBinder getTestBinder() {
        return binder;
    }


    public static CustomApplication getApp() {
        return instance;
    }
}
