package cn.liujson.client.ui.service;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;

/**
 * @author liujson
 * @date 2021/3/5.
 */
public class MqttServiceManager {

    private MqttServiceManager() {

    }

    public static MqttServiceManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final MqttServiceManager INSTANCE = new MqttServiceManager();
    }

    /**
     * MQTT 连接数据
     */
    private ConnectionService.ConnectionServiceBinder mBinder;

    /**
     * 使其运行在前台
     */
    public void runOnForeground(@NonNull Context context) {
        final Intent serviceIntent = getServiceIntent(context);
        //后台启动服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            runOnBackground(context, serviceIntent);
        }
    }

    /**
     * 后台运行服务
     */
    public void runOnBackground(@NonNull Context context, @NonNull Intent serviceIntent) {
        context.startService(serviceIntent);
    }

    /**
     * 绑定服务到 Application
     */
    public void bindToApplication(@NonNull Application application) {
        bindService(application, mServiceConnection);
    }


    public void bindService(@NonNull Context context, @NonNull ServiceConnection serviceConnection) {
        context.bindService(getServiceIntent(context), serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private Intent getServiceIntent(@NonNull Context context) {
        return new Intent(context, ConnectionService.class);
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectionService.ConnectionServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            //Service 断开连接了
        }
    };

    /**
     * 获取Binder
     *
     * @return
     */
    public ConnectionService.ConnectionServiceBinder getServiceBinder() {
        return mBinder;
    }
}
