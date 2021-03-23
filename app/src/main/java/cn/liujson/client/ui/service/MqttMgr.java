package cn.liujson.client.ui.service;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.util.ToastHelper;

/**
 * MQTTServiceManager
 *
 * @author liujson
 * @date 2021/3/5.
 */
public class MqttMgr {

    private Context mContext;

    private MqttMgr() {

    }

    public static MqttMgr instance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final MqttMgr INSTANCE = new MqttMgr();
    }

    /**
     * MQTT 连接数据
     */
    private ConnectionBinder mBinder;

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
     * 初始化 MQTT 服务
     *
     * @param context
     */
    public void init(@NonNull Context context) {
        Objects.requireNonNull(context);
        this.mContext = context;
        bindToApplication(context.getApplicationContext());
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
    private boolean bindToApplication(@NonNull Context application) {
        return bindService(application.getApplicationContext(), mServiceConnection);
    }

    private void unbindToApplication(@NonNull Context context) {
        unbindService(context, mServiceConnection);
    }

    /**
     * 与 Activity 绑定（所有绑定的Activity生命周期结束，服务结束）
     */
    public void bindToActivity(@NonNull Activity activity, @NonNull ServiceConnection serviceConnection) {
        bindService(activity, serviceConnection);
    }


    public boolean bindService(@NonNull Context context, @NonNull ServiceConnection serviceConnection) {
        return context.bindService(getServiceIntent(context), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(@NonNull Context context, @NonNull ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }


    private Intent getServiceIntent(@NonNull Context context) {
        return new Intent(context, ConnectionService.class);
    }

    /**
     * 获取Binder(只有通过绑定Application启动才有这个值)
     *
     * @return
     */
    @Nullable
    public ConnectionBinder binder() {
        return mBinder;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectionBinder) service;
            ToastHelper.showToast(CustomApplication.getApp(), "MQTT后台服务绑定成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            ToastHelper.showToast(CustomApplication.getApp(), "MQTT后台服务绑定失败");
        }
    };
}
