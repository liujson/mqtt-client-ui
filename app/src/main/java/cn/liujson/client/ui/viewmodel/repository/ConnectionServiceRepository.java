package cn.liujson.client.ui.viewmodel.repository;


import android.util.Pair;

import com.ubains.lib.mqtt.mod.provider.MqttConnection;
import com.ubains.lib.mqtt.mod.provider.MqttConnectionImpl;
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
        return MqttMgr.instance().isInstalled();
    }

    public boolean isSame(Object object) {
        return MqttMgr.instance().isSame(object);
    }

    public boolean isConnected() {
        return MqttMgr.instance().isConnected();
    }

    public boolean isClosed() {
        return MqttMgr.instance().isClosed();
    }

    public boolean isConnecting() {
        return MqttMgr.instance().getClient().isConnecting();
    }

    public boolean isResting() {
        return MqttMgr.instance().getClient().isResting();
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
        return rxIsInstalled()
                .observeOn(Schedulers.io())
                .flatMapCompletable(isSetup ->
                        binder().getClient().closeSafety())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

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
