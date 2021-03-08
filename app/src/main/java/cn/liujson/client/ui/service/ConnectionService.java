package cn.liujson.client.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.locks.ReentrantLock;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.PahoMqttV3Impl;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
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

    private final ConnectionServiceBinder binder = new ConnectionServiceBinder();
    /**
     * MQTT 连接操作对象
     */
    private PahoMqttV3Impl imqtt;

    /**
     * 安装和释放资源时需要用的锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //直接通过绑定启动后台服务很快就会被系统杀死
        Log.d(TAG, "==onBind==");
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "==onCreate==");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "==onStartCommand==");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "==onDestroy==");
        final Disposable disconnectForcibly = disconnectForcibly()
                .subscribe(() -> {
                    Log.d(TAG, "==disconnectForcibly success==");
                }, throwable -> {
                    Log.d(TAG, "==disconnectForcibly failure:" + throwable.toString());
                });
    }


    /**
     * 安装 MQTT 到此服务
     */
    public IMQTT setup(IMQTTBuilder builder) throws WrapMQTTException {
        if (this.imqtt == null) {
            if (lock.tryLock()) {
                try {
                    this.imqtt = new PahoMqttV3Impl(builder);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new WrapMQTTException("尝试安装IMQTT失败，请不要频繁调用此方法");
            }
        } else {
            throw new WrapMQTTException("已经安装IMQTT，不要重复绑定");
        }
        return this.imqtt;
    }

    /**
     * 强制接收并释放资源
     * 超时操作会堵塞UI
     *
     * @return
     */
    public Completable disconnectForcibly() {
        //PahoMqttV3Impl 默认的实现，超时时间是40秒，请耐心等待。
        return Completable.create(emitter -> {
            if (imqtt == null) {
                throw new WrapMQTTException("未安装IMQTT释放资源");
            }
            if (lock.tryLock()) {
                try {
                    imqtt.disconnectForcibly();
                    imqtt = null;
                    emitter.onComplete();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new WrapMQTTException("获取锁失败，请等待锁释放");
            }
        });
    }

    /**
     * Binder
     */
    public class ConnectionServiceBinder extends ConnectionBinder {

        /**
         * 安装配置
         * @param builder
         * @return
         */
        @Override
        public Single<IMQTT> setup(IMQTTBuilder builder) {
            return null;
        }

        /**
         * 安全关闭
         * @return
         */
        @Override
        public Completable closeSafety() {
            return null;
        }

        /**
         * 强制结束并释放资源
         * 需要等待其完成
         *
         * @return Completable
         */
        @Override
        public Completable closeForcibly() {
            return ConnectionService.this.disconnectForcibly();
        }
    }

    //----------------------------------------------------------------------
}
