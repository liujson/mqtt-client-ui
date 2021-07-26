package com.ubains.lib.mqtt.mod.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ubains.android.ubutil.comm.LogUtil;

import io.reactivex.disposables.Disposable;

/**
 * MQTT 连接管理服务。
 * 一个服务就相当于一个后台连接。
 *
 * @author liujson
 * @date 2021/3/3.
 */
public class ConnectionService extends Service {

    private static final String TAG = "ConnectionService";

    private final ConnectionBinder binder = new ConnectionBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //直接通过绑定启动后台服务很快就会被系统杀死
        LogUtil.d(TAG, "==onBind==");
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "==onCreate==");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "==onStartCommand==");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "==onDestroy==");
        if (binder.isInstalled()) {
            //尝试关闭并释放资源
            final Disposable rxCloseSafety = binder.getClient()
                    .closeForcibly(1000, 500)
                    .subscribe(() -> {
                        LogUtil.d(TAG, "==disconnectForcibly success==");
                    }, throwable -> {
                        LogUtil.d(TAG, "==disconnectForcibly failure:" + throwable.toString());
                    });
        }
    }
}
