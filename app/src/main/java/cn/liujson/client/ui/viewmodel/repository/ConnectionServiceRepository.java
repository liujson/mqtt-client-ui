package cn.liujson.client.ui.viewmodel.repository;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Pair;

import java.util.List;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.service.ConnectionBinder;

import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.lib.mqtt.api.QoS;


import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import io.reactivex.Completable;

import io.reactivex.Single;

import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * @author liujson
 * @date 2021/3/10.
 */
public class ConnectionServiceRepository {

    private ConnectionBinder serviceBinder;

    private final OnBindStatus onbindStatus;

    /**
     * 默认操作超时时间
     */
    public static final int DEFAULT_TIME_OUT = 10000;


    public ConnectionServiceRepository(OnBindStatus onbindStatus) {
        this.onbindStatus = onbindStatus;
    }

    public void bindConnectionService(Context context) {
        MqttMgr.instance().bindService(context, serviceConnection);
    }


    public void unbindConnectionService() {
        MqttMgr.instance().unbindService(CustomApplication.getApp(), serviceConnection);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBinder = (ConnectionBinder) service;
            if (onbindStatus != null) {
                onbindStatus.onBindSuccess(serviceBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBinder = null;
            if (onbindStatus != null) {
                onbindStatus.onBindFailure();
            }
        }
    };


    public boolean isBind() {
        return serviceBinder != null;
    }

    public boolean isInstalled() {
        return serviceBinder.isInstalled();
    }

    public boolean isSame(Object object) {
        return serviceBinder.isSame(object);
    }

    public boolean isConnected() {
        return serviceBinder.isInstalled() && serviceBinder.getClient().isConnected();
    }


    public Single<Boolean> rxIsInstalled() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (!isInstalled()) {
                throw new IllegalStateException("未安装Client为服务");
            }
            emitter.onSuccess(true);
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    public void install(RxPahoClient client) {
        serviceBinder.install(client);
    }

    public void uninstall() {
        serviceBinder.uninstall();
    }

    public List<Pair<String, QoS>> getSubList() {
        return serviceBinder.getClient().getActiveSubs();
    }

    public void addOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        serviceBinder.addOnRecMsgListener(recMsgListener);
    }

    public void removeOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        serviceBinder.removeOnRecMsgListener(recMsgListener);
    }

    public void addOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        serviceBinder.addOnConnectedListener(connectedListener);
    }

    public void removeOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        serviceBinder.removeOnConnectedListener(connectedListener);
    }

    public Completable connect() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                serviceBinder.getClient().connect())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable subscribe(String topic, QoS qoS) {
        return serviceBinder.getClient()
                .subscribe(topic, qoS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable unsubscribe(String topic) {
        return serviceBinder.getClient()
                .unsubscribe(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable publish(String topic, String message, QoS qoS, boolean retained) {
        return serviceBinder.getClient()
                .publish(topic, message.getBytes(),
                        qoS, retained)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeSafety() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                serviceBinder.getClient().closeSafety())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeForcibly() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                serviceBinder.getClient()
                        .closeForcibly(DEFAULT_TIME_OUT << 1, DEFAULT_TIME_OUT))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public interface OnBindStatus {

        void onBindSuccess(ConnectionBinder serviceBinder);

        void onBindFailure();
    }
}
