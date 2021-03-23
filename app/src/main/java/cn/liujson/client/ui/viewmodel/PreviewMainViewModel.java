package cn.liujson.client.ui.viewmodel;


import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;


import org.greenrobot.eventbus.EventBus;


import java.util.List;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;

import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.service.ConnectionBinder;

import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;


import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import cn.liujson.logger.LogUtils;
import io.reactivex.Completable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/4.
 */
public class PreviewMainViewModel extends BaseViewModel implements
        ConnectionBinder.OnRecMsgListener, ConnectionBinder.OnConnectedListener {

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

        repository = new ConnectionServiceRepository();
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
        if (repository.isInstalled()) {
            getRepository().removeOnRecMsgListener(this);
            getRepository().removeOnConnectedListener(this);
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
                    LogUtils.e("load connection profiles failure." + throwable.toString());
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }

    public ConnectionParams profile2Params(ConnectionProfile profile) {
        // TODO: 2021/3/19  配置连接参数
        return ConnectionParams.newBuilder()
                .serverURI("tcp://" + profile.brokerAddress + ":" + profile.brokerPort)
                .cleanSession(profile.cleanSession)
                .automaticReconnect(profile.autoReconnect)
                .maxReconnectDelay(profile.maxReconnectDelay)
                .keepAlive(profile.keepAliveInterval)
                .connectionTimeout(profile.connectionTimeout)
                .clientId(profile.clientID)
                .username(profile.username)
                .password(profile.password)
                .build();
    }

    public RxPahoClient create(ConnectionParams params) {
        try {
            return new RxPahoClient(params);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 配置并且连接
     * 如果当前存在已配置的且和目标builder相同不重新配置，否则重新配置；
     * 如果旧的还在连接着先尝试安全关闭，如果失败，采取强制关闭
     *
     * @param params
     * @return
     */
    public Completable setupAndConnect(ConnectionParams params) {
        Completable actionCompletable = getRepository()
                .connect().observeOn(AndroidSchedulers.mainThread());
        //如果已经配置和已经连接上则断开连接然后再创建新的连接
        if (getRepository().isInstalled() && getRepository().isSame(params)) {
            if (getRepository().isConnected()) {
                actionCompletable = getRepository().closeSafety()
                        //如果安全断开失败则强制断开连接
                        .onErrorResumeNext(throwable -> getRepository().closeForcibly())
                        .andThen(actionCompletable);
            } else if (getRepository().isClosed()) {
                //如果client被关闭,表示客户端不再能用了，从Service卸载客户端
                getRepository().uninstall();
            }
        } else {
            //如果未安装则进行安装
            final RxPahoClient pahoClient = create(params);
            if (pahoClient != null) {
                getRepository().install(pahoClient);
                getRepository().addOnRecMsgListener(this);
                getRepository().addOnConnectedListener(this);
            }
        }
        return actionCompletable;
    }


    @Override
    public void onReceiveMessage(String topic, byte[] payload, QoS qoS) {

    }

    @Override
    public void onConnectComplete(boolean reconnect, String serverURI) {
        EventBus.getDefault().post(new ConnectChangeEvent(true));
    }


    public interface Navigator {
        /**
         * 通知更新
         */
        void notifyChangeSpinner(List<ConnectionProfile> data);
    }
}
