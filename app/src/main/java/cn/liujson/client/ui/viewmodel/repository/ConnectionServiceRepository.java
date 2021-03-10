package cn.liujson.client.ui.viewmodel.repository;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import cn.liujson.lib.mqtt.util.MQTTUtils;
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

    private ConnectionService.ConnectionServiceBinder serviceBinder;

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
            serviceBinder = (ConnectionService.ConnectionServiceBinder) service;
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

    public void registerReceiver(IMQTTMessageReceiver receiver) {
        if (serviceBinder != null) {
            serviceBinder.registerMessageReceiver(receiver);
        }
    }

    public void unregisterReceiver(IMQTTMessageReceiver receiver) {
        if (serviceBinder != null) {
            serviceBinder.unregisterMessageReceiver(receiver);
        }
    }


    public boolean isBind() {
        return serviceBinder != null;
    }

    public boolean isSetup() {
        return serviceBinder.isSetup();
    }

    public boolean isConnected() {
        return isSetup() && serviceBinder.getWrapper().getClient().isConnected();
    }

    public Single<Boolean> rxIsConnected() {
        return rxIsSetup().flatMap(aBoolean ->
                Single.just(serviceBinder.getWrapper().getClient().isConnected()));
    }

    public Single<Boolean> rxIsSetup() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (!isBind()) {
                throw new WrapMQTTException("serviceBinder 服务连接失败");
            }
            if (!isSetup()) {
                throw new WrapMQTTException("未安装Client为服务");
            }
            emitter.onSuccess(true);
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    public void setup(IMQTTWrapper<PahoV3MQTTClient> clientIMQTTWrapper) {
        serviceBinder.setup(clientIMQTTWrapper);
    }

    public Completable connect() {
        return rxIsSetup().flatMapCompletable(isSetup ->
                serviceBinder.getWrapper().getClient().rxConnect());
    }

    public Completable subscribe(String topic, QoS qoS) {
        return Completable.create(emitter -> {
            serviceBinder.getWrapper().getClient()
                    .subscribe(topic, MQTTUtils.qoS2Int(qoS)).waitForCompletion(DEFAULT_TIME_OUT);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable unsubscribe(String topic) {
        return Completable.create(emitter -> {
            serviceBinder.getWrapper().getClient()
                    .unsubscribe(topic).waitForCompletion(DEFAULT_TIME_OUT);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable publish(String topic, String message, QoS qoS, boolean retained) {
        return Completable.create(emitter -> {
            serviceBinder.getWrapper().getClient()
                    .publish(topic, message.getBytes(),
                            MQTTUtils.qoS2Int(qoS), retained).waitForCompletion(DEFAULT_TIME_OUT);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeSafety() {
        return rxIsSetup().flatMapCompletable(isSetup ->
                serviceBinder.closeSafety());
    }

    public Completable closeForcibly() {
        return rxIsSetup().flatMapCompletable(isSetup ->
                serviceBinder.closeForcibly());
    }

    public interface OnBindStatus {

        void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder);

        void onBindFailure();
    }


}
