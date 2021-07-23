package com.ubains.lib.mqtt.mod.provider;


import android.util.Pair;

import androidx.annotation.Nullable;



import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;
import com.ubains.lib.mqtt.mod.service.ConnectionBinder;
import com.ubains.lib.mqtt.mod.service.MqttMgr;

import java.util.List;


import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MqttConnectionImpl implements MqttConnection {

    /**
     * 默认操作超时时间
     */
    public static final int DEFAULT_TIME_OUT = 10000;

    @Override
    public boolean storeProfile(ConnectionProfile connectionProfile) {
        final IConnectionProfileStore profileStore = MqttMgr.instance().getProfileStore();
        if (profileStore != null) {
            return profileStore.store(connectionProfile);
        }
        return false;
    }

    @Nullable
    @Override
    public ConnectionProfile loadProfile() {
        final IConnectionProfileStore profileStore = MqttMgr.instance().getProfileStore();
        if (profileStore != null) {
            return profileStore.load();
        }
        return null;
    }


    @Override
    public boolean isBind() {
        return MqttMgr.instance().isBind();
    }

    public ConnectionBinder binder() {
        return MqttMgr.instance().binder();
    }

    @Override
    public boolean isInstalled() {
        return MqttMgr.instance().isInstalled();
    }

    @Override
    public boolean isSame(Object object) {
        return MqttMgr.instance().isSame(object);
    }

    @Override
    public boolean isConnected() {
        return MqttMgr.instance().isConnected();
    }

    @Override
    public boolean isClosed() {
        return MqttMgr.instance().isClosed();
    }


    public Single<Boolean> rxIsInstalled() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (!isInstalled()) {
                throw new IllegalStateException("未安装Client为服务");
            }
            emitter.onSuccess(true);
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void install(RxPahoClient client) {
        binder().install(client);
    }

    @Override
    public void uninstall() {
        binder().uninstall();
    }

    @Override
    public List<Pair<String, QoS>> getSubList() {
        return binder().getClient().getActiveSubs();
    }

    @Override
    public RxPahoClient getClient() {
        return isBind() ? binder().getClient() : null;
    }


    @Override
    public Completable connect() {
        return rxIsInstalled().flatMapCompletable(isSetup ->
                binder().getClient().connect())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable subscribe(String topic, QoS qoS) {
        return binder().getClient()
                .subscribe(topic, qoS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable subscribe(String[] topic, QoS[] qoS) {
        return binder().getClient()
                .subscribe(topic, qoS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable unsubscribe(String topic) {
        return binder().getClient()
                .unsubscribe(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable publish(String topic, String message, QoS qoS, boolean retained) {
        return binder().getClient()
                .publish(topic, message.getBytes(),
                        qoS, retained)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable closeSafety() {
        return rxIsInstalled()
                .observeOn(Schedulers.io())
                .flatMapCompletable(isSetup ->
                        binder().getClient().closeSafety())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable closeForcibly() {
        return rxIsInstalled()
                .observeOn(Schedulers.io())
                .flatMapCompletable(isSetup ->
                        binder().getClient()
                                .closeForcibly(DEFAULT_TIME_OUT << 1, DEFAULT_TIME_OUT))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}