package cn.liujson.client.ui.viewmodel;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;

import java.util.List;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.MqttBuilder;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTWrapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/4.
 */
public class PreviewMainViewModel extends BaseViewModel implements ConnectionServiceRepository.OnBindStatus {

    public final ObservableBoolean fieldConnectEnable = new ObservableBoolean(false);
    public final ObservableBoolean fieldDisconnectEnable = new ObservableBoolean(false);

    private Disposable loadProfilesDisposable;

    private Navigator navigator;


    private final ConnectionServiceRepository repository;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public PreviewMainViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);

        repository = new ConnectionServiceRepository(this);
    }

    public ConnectionServiceRepository getRepository() {
        return repository;
    }


    @Override
    public void onRelease() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
            loadProfilesDisposable = null;
        }
    }


    /**
     * 加载连接属性列表
     */
    public void loadProfiles() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
        }
        loadProfilesDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .loadProfiles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    loadProfilesDisposable = null;
                    if (navigator != null) {
                        navigator.notifyChangeSpinner(data);
                    }
                }, throwable -> {
                    loadProfilesDisposable = null;
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }

    public IMQTTBuilder profile2MQTTBuilder(ConnectionProfile profile) {
        final MqttBuilder builder = new MqttBuilder();
        builder.host("tcp://" + profile.brokerAddress + ":" + profile.brokerPort);
        builder.cleanSession(profile.cleanSession);
        return builder;
    }

    public IMQTTWrapper<PahoV3MQTTClient> create(IMQTTBuilder builder) {
        try {
            return new PahoV3MQTTWrapper(builder);
        } catch (WrapMQTTException e) {
            return null;
        }
    }

    /**
     * 配置并且连接
     * 如果当前存在已配置的且和目标builder相同不重新配置，否则重新配置；
     * 如果旧的还在连接着先尝试安全关闭，如果失败，采取强制关闭
     *
     * @param builder
     * @return
     */
    public Completable setupAndConnect(IMQTTBuilder builder) {
        Completable actionCompletable = getRepository()
                .connect().observeOn(AndroidSchedulers.mainThread());
        //如果已经配置和已经连接上则断开连接然后再创建新的连接
        if (getRepository().isSetup() && getRepository().isSame(builder)) {
            if (getRepository().isConnected()) {
                actionCompletable = getRepository().closeSafety()
                        //如果安全断开失败则强制断开连接
                        .onErrorResumeNext(throwable -> getRepository().closeForcibly())
                        .andThen(actionCompletable);
            }
        } else {
            //如果未安装则进行安装
            final IMQTTWrapper<PahoV3MQTTClient> wrapper = create(builder);
            if (wrapper != null) {
                getRepository().setup(wrapper);
            }
        }
        return actionCompletable;
    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {
        if (navigator != null) {
            navigator.onBindSuccess(serviceBinder);
        }
    }

    @Override
    public void onBindFailure() {
        if (navigator != null) {
            navigator.onBindFailure();
        }
    }


    public interface Navigator extends ConnectionServiceRepository.OnBindStatus {
        /**
         * 通知更新
         */
        void notifyChangeSpinner(List<ConnectionProfile> data);
    }
}
