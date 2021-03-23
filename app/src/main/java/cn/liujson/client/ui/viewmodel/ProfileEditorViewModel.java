package cn.liujson.client.ui.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.liujson.client.ui.ProfileEditorActivity;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.util.MqttUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlinx.coroutines.Delay;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ProfileEditorViewModel extends BaseViewModel {

    public final ObservableField<String> fieldProfileName = new ObservableField<>();
    public final ObservableField<String> fieldBrokerAddress = new ObservableField<>();
    public final ObservableField<String> fieldBrokerPort = new ObservableField<>();
    public final ObservableField<String> fieldClientID = new ObservableField<>();
    public final ObservableField<String> fieldUsername = new ObservableField<>();
    public final ObservableField<String> fieldPassword = new ObservableField<>();
    public final ObservableField<String> fieldKeepAliveInterval = new ObservableField<>("60");
    public final ObservableField<String> fieldConnectionTimeout = new ObservableField<>("30");
    public final ObservableField<String> fieldMaxReconnectDelay
            = new ObservableField<>(String.valueOf(ConnectionParams.MAX_RECONNECT_DELAY_DEFAULT));
    public final ObservableBoolean fieldCleanSession = new ObservableBoolean(true);
    public final ObservableBoolean fieldAutoReconnect = new ObservableBoolean(true);

    private Navigator navigator;

    private Disposable insertProfileDisposable, queryProfileByIdDisposable, updateProfileDisposable;

    private final ProfileEditorActivity.Mode openMode;
    private final long profileID;

    public ProfileEditorViewModel(Lifecycle mLifecycle, ProfileEditorActivity.Mode openMode, long profileID) {
        super(mLifecycle);
        this.openMode = openMode;
        this.profileID = profileID;
        initNewProfile();
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }


    public final void initNewProfile() {
        fieldCleanSession.set(true);
        fieldBrokerPort.set(String.valueOf(1883));
        fieldProfileName.set("New Profile");
        generate();
    }

    /**
     * 生成随机 ClientID
     */
    public final void generate() {
        fieldClientID.set(MqttUtils.generateClientId());
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
        connectionProfile.profileName = fieldProfileName.get();
        connectionProfile.brokerAddress = fieldBrokerAddress.get();
        connectionProfile.brokerPort = Integer.parseInt(Objects.requireNonNull(fieldBrokerPort.get()));
        connectionProfile.clientID = fieldClientID.get();
        connectionProfile.username = fieldUsername.get();
        connectionProfile.password = fieldPassword.get();
        connectionProfile.cleanSession = fieldCleanSession.get();
        connectionProfile.connectionTimeout = Integer.parseInt(Objects.requireNonNull(fieldConnectionTimeout.get()));
        connectionProfile.keepAliveInterval = Integer.parseInt(Objects.requireNonNull(fieldKeepAliveInterval.get()));
        connectionProfile.autoReconnect = fieldAutoReconnect.get();
        if(fieldAutoReconnect.get()){
            connectionProfile.maxReconnectDelay = Integer.parseInt(Objects.requireNonNull(fieldMaxReconnectDelay.get()));
        }

        if (openMode == ProfileEditorActivity.Mode.NEW) {
            save(connectionProfile);
        } else if (openMode == ProfileEditorActivity.Mode.EDIT) {
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
