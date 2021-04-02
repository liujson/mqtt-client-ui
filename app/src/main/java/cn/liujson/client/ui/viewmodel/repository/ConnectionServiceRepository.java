package cn.liujson.client.ui.viewmodel.repository;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Pair;

import java.util.List;
import java.util.Objects;

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

    /**
     * 默认操作超时时间
     */
    public static final int DEFAULT_TIME_OUT = 10000;


    public ConnectionServiceRepository() {

    }

    public boolean isBind() {
        return MqttMgr.instance().binder() != null;
    }


    public ConnectionBinder binder() {
        return MqttMgr.instance().binder();
    }

    public boolean isInstalled() {
        return binder().isInstalled();
    }

    public boolean isSame(Object object) {
        return binder().isSame(object);
    }

    public boolean isConnected() {
        return binder().getClient().isConnected();
    }

    public boolean isClosed() {
        return binder().getClient().isClosed();
    }

    public boolean isConnecting() {
        return binder().getClient().isConnecting();
    }

    public boolean isResting(){
        return binder().getClient().isResting();
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
        binder().install(client);
    }

    public void uninstall() {
        binder().uninstall();
    }

    public List<Pair<String, QoS>> getSubList() {
        return binder().getClient().getActiveSubs();
    }

    public void addOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        binder().addOnRecMsgListener(recMsgListener);
    }

    public void removeOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        binder().removeOnRecMsgListener(recMsgListener);
    }

    public void addOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        binder().addOnConnectedListener(connectedListener);
    }

    public void removeOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        binder().removeOnConnectedListener(connectedListener);
    }

    public Completable connect() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                binder().getClient().connect())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable subscribe(String topic, QoS qoS) {
        return binder().getClient()
                .subscribe(topic, qoS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable subscribe(String[] topic, QoS[] qoS) {
        return binder().getClient()
                .subscribe(topic, qoS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable unsubscribe(String topic) {
        return binder().getClient()
                .unsubscribe(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable publish(String topic, String message, QoS qoS, boolean retained) {
        return binder().getClient()
                .publish(topic, message.getBytes(),
                        qoS, retained)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeSafety() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                binder().getClient().closeSafety())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeForcibly() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                binder().getClient()
                        .closeForcibly(DEFAULT_TIME_OUT << 1, DEFAULT_TIME_OUT))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
