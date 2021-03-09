package cn.liujson.client.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.PahoMqttV3Impl;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

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

    private IMQTTWrapper<PahoV3MQTTClient> mqttClient;

    private AtomicBoolean registered = new AtomicBoolean(false);

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
        //尝试关闭并释放资源
        if (mqttClient != null) {
            mqttClient.destroy();
            final Disposable rxCloseSafety = mqttClient.getClient()
                    .rxCloseForcibly()
                    .subscribe(() -> {
                        Log.d(TAG, "==disconnectForcibly success==");
                    }, throwable -> {
                        Log.d(TAG, "==disconnectForcibly failure:" + throwable.toString());
                    });
        }
    }


    /**
     * 安装 MQTT Client服务
     */
    public IMQTTWrapper<PahoV3MQTTClient> register(IMQTTBuilder builder) throws WrapMQTTException {
        if (Looper.myLooper() != getMainLooper()) {
            throw new WrapMQTTException("只允许在Main线程调用此方法");
        }
        if (mqttClient == null) {
            mqttClient = new PahoV3MQTTWrapper(builder);
            registered.set(true);
        } else {
            throw new WrapMQTTException("已经安装IMQTT，不要重复绑定");
        }
        return mqttClient;
    }


    public void unregister() {
        registered.set(false);
    }


    public boolean isRegistered() {
        return registered.get();
    }

    /**
     * Binder
     */
    public class ConnectionServiceBinder extends ConnectionBinder<PahoV3MQTTClient> {

        /**
         * 安装配置
         *
         * @param builder 参数
         * @return
         */
        @Override
        public Single<IMQTTWrapper<PahoV3MQTTClient>> setup(final IMQTTBuilder builder) {
            return Single.create((SingleOnSubscribe<IMQTTWrapper<PahoV3MQTTClient>>) emitter -> {
                emitter.onSuccess(ConnectionService.this.register(builder));
            }).subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /**
         * 安全关闭(先尝试断开连接然后关闭)
         *
         * @return
         */
        @Override
        public Completable closeSafety() {
            if (mqttClient == null) {
                return Completable.error(new WrapMQTTException("请先setup后关闭"));
            }
            return mqttClient.getClient()
                    .rxCloseSafety()
                    .doOnComplete(() -> {
                        mqttClient = null;
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /**
         * 强制结束并释放资源
         * 需要等待其完成
         *
         * @return Completable
         */
        @Override
        public Completable closeForcibly() {
            if (mqttClient == null) {
                return Completable.error(new WrapMQTTException("请先setup后关闭"));
            }
            return mqttClient.getClient()
                    .rxCloseForcibly()
                    .doFinally(() -> {
                        mqttClient = null;
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }
    //----------------------------------------------------------------------
}
