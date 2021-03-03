package cn.liujson.client.ui.viewmodel;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.util.MQTTUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
    public final ObservableBoolean fieldCleanSession = new ObservableBoolean(true);

    private Navigator navigator;

    Disposable insertProfileDisposable;

    public ProfileEditorViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
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
        fieldClientID.set(MQTTUtils.generateClientId());
    }

    /**
     * 保存配置到数据库
     */
    public final void applyProfile() {
        if (navigator == null || !navigator.checkApplyParam()) {
            return;
        }
        final ConnectionProfile connectionProfile = new ConnectionProfile();
        connectionProfile.profileName = fieldProfileName.get();
        connectionProfile.brokerAddress = fieldBrokerAddress.get();
        connectionProfile.brokerPort = Integer.parseInt(fieldBrokerPort.get());
        connectionProfile.clientID = fieldClientID.get();
        connectionProfile.username = fieldUsername.get();
        connectionProfile.password = fieldPassword.get();
        connectionProfile.cleanSession = fieldCleanSession.get();
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
                .subscribe(()->{
                    insertProfileDisposable = null;
                    navigator.applySuccess();
                },
                throwable -> {
                    insertProfileDisposable = null;
                    ToastHelper.showToast(CustomApplication.getApp(), "insert connection profiles failure.");
                });
    }

    @Override
    public void onRelease() {
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
         * 应用成功
         */
        void applySuccess();
    }
}
