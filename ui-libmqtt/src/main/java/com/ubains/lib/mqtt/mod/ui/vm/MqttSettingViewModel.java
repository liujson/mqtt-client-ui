package com.ubains.lib.mqtt.mod.ui.vm;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.ubains.android.ubutil.touch.DoubleClickUtils;
import com.ubains.lib.mqtt.mod.provider.MqttConnection;
import com.ubains.lib.mqtt.mod.provider.MqttConnectionImpl;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;
import com.ubains.lib.mqtt.mod.service.MqttMgr;

import java.util.Calendar;
import java.util.Objects;


import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/4/12.
 */
public class MqttSettingViewModel {

    private Navigator navigator;

    MqttConnection mqttConnection;

    Disposable saveDisposable;

    final MqttSettingObservableEntity entity;

    public MqttSettingViewModel(MqttSettingObservableEntity entity) {
        this.entity = entity;
        mqttConnection = new MqttConnectionImpl();
        loadProfile();
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }


    public void loadProfile() {
        final ConnectionProfile connectionProfile = mqttConnection.loadProfile();
        if (connectionProfile != null) {
            entity.fieldBrokerAddress.set(connectionProfile.brokerAddress);
            entity.fieldBrokerPort.set(String.valueOf(connectionProfile.brokerPort));
            entity.fieldClientID.set(connectionProfile.clientID);
            entity.fieldUsername.set(connectionProfile.username);
            entity.fieldPassword.set(connectionProfile.password);
            entity.fieldKeepAliveInterval.set(String.valueOf(connectionProfile.keepAliveInterval));
            entity.fieldConnectionTimeout.set(String.valueOf(connectionProfile.connectionTimeout));
            entity.fieldMaxReconnectDelay.set(String.valueOf(connectionProfile.maxReconnectDelay));
            entity.fieldCleanSession.set(connectionProfile.cleanSession);
            entity.fieldAutoReconnect.set(connectionProfile.autoReconnect);
            entity.fieldLwtTopic.set(connectionProfile.willTopic);
            entity.fieldLwtMessage.set(connectionProfile.willMessage);
            entity.fieldLwtRetained.set(connectionProfile.willRetained);
        }
    }


    public final void applyClick(View view) {
        if (DoubleClickUtils.isFastDoubleClick(view.getId())) {
            return;
        }
        if (navigator != null) {
            navigator.showApplyConfirm(view);
        }
    }

    /**
     * 保存配置到数据库
     */
    public final void applyProfile() {
        if (navigator == null || !navigator.checkApplyParam()) {
            return;
        }
        final ConnectionProfile connectionProfile = new ConnectionProfile();
        connectionProfile.profileName = entity.fieldProfileName.get();
        final String schema = navigator.readSchema();
        connectionProfile.brokerAddress = schema + entity.fieldBrokerAddress.get();
        connectionProfile.brokerPort = Integer.parseInt(Objects.requireNonNull(entity.fieldBrokerPort.get()));
        connectionProfile.clientID = entity.fieldClientID.get();
        connectionProfile.username = entity.fieldUsername.get();
        connectionProfile.password = entity.fieldPassword.get();
        connectionProfile.cleanSession = entity.fieldCleanSession.get();
        connectionProfile.connectionTimeout = Integer.parseInt(Objects.requireNonNull(entity.fieldConnectionTimeout.get()));
        connectionProfile.keepAliveInterval = Integer.parseInt(Objects.requireNonNull(entity.fieldKeepAliveInterval.get()));
        connectionProfile.autoReconnect = entity.fieldAutoReconnect.get();
        if (entity.fieldAutoReconnect.get()) {
            connectionProfile.maxReconnectDelay = Integer.parseInt(Objects.requireNonNull(entity.fieldMaxReconnectDelay.get()));
        }

        if (!TextUtils.isEmpty(entity.fieldLwtTopic.get()) && !TextUtils.isEmpty(entity.fieldLwtMessage.get())) {
            connectionProfile.willTopic = entity.fieldLwtTopic.get();
            connectionProfile.willMessage = entity.fieldLwtMessage.get();
            connectionProfile.willQoS = navigator.readWillQos();
            connectionProfile.willRetained = navigator.isWillRetained();
        }

        connectionProfile.updateTime = Calendar.getInstance().getTime();
        save(connectionProfile);
    }


    /**
     * 保存
     *
     * @param connectionProfile
     */
    private void save(final ConnectionProfile connectionProfile) {
        if (navigator == null) {
            return;
        }
        final Observable<String> storeProfileObservable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            emitter.onNext("请不要关闭，正在保存连接配置...");
            SystemClock.sleep(500);
            if (mqttConnection.storeProfile(connectionProfile)) {
                emitter.onNext("保存连接配置成功");
                emitter.onComplete();
            } else {
                emitter.onError(new RuntimeException("保存连接配置失败"));
            }
        });

        saveDisposable = Observable.concat(MqttMgr.instance().connectTo(connectionProfile, 500),
                storeProfileObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    if (navigator != null) {
                        navigator.showLoading();
                    }
                })
                .doFinally(() -> {
                    if (navigator != null) {
                        navigator.hideLoading();
                    }
                })
                .subscribe(info -> {
                    if (navigator != null) {
                        navigator.applyProcess(info);
                    }
                }, throwable -> {
                    if (navigator != null) {
                        navigator.applyFailure(throwable);
                    }
                }, () -> {
                    if (navigator != null) {
                        //发送连接消息成功
                        navigator.applySuccess();
                    }
                });
    }


    public void release() {
        navigator = null;
    }


    public interface Navigator {
        /**
         * 检查参数
         *
         * @return
         */
        boolean checkApplyParam();

        /**
         * 读取 qos
         */
        QoS readWillQos();

        /**
         * 获取协议
         * 目前只有两种 tcp://、ssl://
         */
        String readSchema();

        /**
         * 是否勾选 retained
         */
        boolean isWillRetained();

        /**
         * 应用成功
         */
        void applySuccess();

        /**
         * 应用失败
         */
        void applyFailure(Throwable throwable);

        /**
         * 应用过程信息
         *
         * @param processMsg
         */
        void applyProcess(String processMsg);

        /**
         * 显示弹框
         */
        void showLoading();

        /**
         * 隐藏弹框
         */
        void hideLoading();

        /**
         * 显示保存配置确认弹框
         */
        void showApplyConfirm(View view);
    }


}
