package cn.liujson.client.ui.viewmodel;

import android.text.TextUtils;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingObservableEntity;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.liujson.client.ui.ProfileEditorActivity;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.fragments.ProfileEditorFragment;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ProfileEditorViewModel extends BaseViewModel {


    private Navigator navigator;

    private Disposable insertProfileDisposable, queryProfileByIdDisposable, updateProfileDisposable;

    private final ProfileEditorFragment.Mode openMode;
    private final long profileID;

    MqttSettingObservableEntity entity;

    public ProfileEditorViewModel(Lifecycle mLifecycle, MqttSettingObservableEntity entity, ProfileEditorFragment.Mode openMode, long profileID) {
        super(mLifecycle);
        this.entity = entity;
        this.openMode = openMode;
        this.profileID = profileID;
        initNewProfile();
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }


    public final void initNewProfile() {
        entity.fieldCleanSession.set(true);
        entity.fieldBrokerPort.set(String.valueOf(1883));
        entity.fieldProfileName.set("New Profile");
        generate();
    }

    /**
     * 生成随机 ClientID
     */
    public final void generate() {
        entity.fieldClientID.set(MqttUtils.generateClientId());
    }

    /**
     * 保存配置到数据库
     */
    public final void applyProfile() {
        if (navigator == null || !navigator.checkApplyParam()) {
            return;
        }
        final ConnectionProfile connectionProfile = new ConnectionProfile();
        connectionProfile.id = (int) profileID;
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

        if (openMode == ProfileEditorFragment.Mode.NEW) {
            connectionProfile.createDate = Calendar.getInstance().getTime();
            connectionProfile.updateDate = connectionProfile.createDate;
            save(connectionProfile);
        } else if (openMode == ProfileEditorFragment.Mode.EDIT) {
            connectionProfile.updateDate = Calendar.getInstance().getTime();
            update(connectionProfile);
        } else {
            throw new RuntimeException("error data openMode = " + openMode);
        }
    }


    private final void save(ConnectionProfile connectionProfile) {
        if (insertProfileDisposable != null) {
            insertProfileDisposable.dispose();
            insertProfileDisposable = null;
        }
        //插入数据
        insertProfileDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .insertProfile(connectionProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            insertProfileDisposable = null;
                            navigator.applySuccess();
                        },
                        throwable -> {
                            insertProfileDisposable = null;
                            ToastHelper.showToast(CustomApplication.getApp(), "insert connection profiles failure.");
                        });
    }

    private final void update(ConnectionProfile connectionProfile) {
        if (updateProfileDisposable != null) {
            updateProfileDisposable.dispose();
            updateProfileDisposable = null;
        }
        //插入数据
        updateProfileDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .updateProfile(connectionProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            updateProfileDisposable = null;
                            navigator.applySuccess();
                        },
                        throwable -> {
                            updateProfileDisposable = null;
                            ToastHelper.showToast(CustomApplication.getApp(), "update connection profiles failure.");
                        });
    }

    /**
     * 通过ID查询
     *
     * @param id
     * @return
     */
    public final void queryProfileById(long id) {
        queryProfileByIdDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .queryProfileById((int) id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(data -> {
                    if (navigator != null) {
                        navigator.onEditingQueryProfile(data);
                    }
                }, throwable -> {
                    if (navigator != null) {
                        navigator.onEditingQueryProfileFail(throwable);
                    }
                });
    }

    @Override
    public void onRelease() {
        navigator = null;
        if (queryProfileByIdDisposable != null) {
            queryProfileByIdDisposable.dispose();
            queryProfileByIdDisposable = null;
        }
        if (insertProfileDisposable != null) {
            insertProfileDisposable.dispose();
            insertProfileDisposable = null;
        }
        if (updateProfileDisposable != null) {
            updateProfileDisposable.dispose();
            updateProfileDisposable = null;
        }
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
         * 编辑时查询参数
         */
        void onEditingQueryProfile(ConnectionProfile connectionProfile);

        /**
         * 编辑时查询参数错误
         */
        void onEditingQueryProfileFail(Throwable throwable);
    }
}
