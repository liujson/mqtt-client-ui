package cn.liujson.client.ui.viewmodel;


import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;
import androidx.room.EmptyResultSetException;


import com.ubains.lib.mqtt.mod.provider.event.MqttConnectCompleteEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttConnectionLostEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttFirstConnectRetryEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;

import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.dao.ConnectionProfileDao;
import cn.liujson.client.ui.db.entities.ConnectionProfile;


import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;


import cn.liujson.client.ui.widget.popup.MarkStarPopupView;

import cn.liujson.lib.mqtt.api.QoS;

import cn.liujson.lib.mqtt.util.MqttUtils;

import cn.ubains.android.ublogger.LogUtils;
import io.reactivex.Completable;


import io.reactivex.CompletableSource;
import io.reactivex.Observable;

import io.reactivex.Single;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/4.
 */
public class PreviewMainViewModel extends BaseViewModel {

    public final ObservableBoolean fieldConnected = new ObservableBoolean(false);

    public final ObservableBoolean fieldConnectEnable = new ObservableBoolean(false);
    public final ObservableBoolean fieldDisconnectEnable = new ObservableBoolean(false);

    private Disposable loadProfilesDisposable;

    private Navigator navigator;


    private final ConnectionServiceRepository repository;

    /**
     * 初始化时加载的标星主题
     */
    private List<MarkStarPopupView.TopicWrapper> initStarTopics = new CopyOnWriteArrayList<>();

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public PreviewMainViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
        updateToDisabledState();
        repository = new ConnectionServiceRepository();
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
    }

    /**
     * 更新为不可用状态
     */
    public void updateToDisabledState() {
        fieldConnected.set(false);
        fieldConnectEnable.set(false);
        fieldDisconnectEnable.set(false);
    }

    /**
     * 更新连接状态
     *
     * @param isConnected
     */
    public void updateConnectionState(boolean isConnected) {
        fieldConnected.set(isConnected);
        EventBus.getDefault().post(new ConnectChangeEvent(isConnected));
    }

    /**
     * 加载连接属性列表
     */
    public void loadProfiles() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
        }
        final ConnectionProfileDao dao = DatabaseHelper
                .getInstance()
                .connectionProfileDao();
        loadProfilesDisposable = dao.count().flatMap(size -> {
            if (size > 0) {
                return dao.loadProfiles();
            }
            return Single.error(new EmptyResultSetException("Connection profile is empty"));
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    loadProfilesDisposable = null;
                    if (navigator != null) {
                        navigator.notifyChangeSpinner(data);
                    }
                }, throwable -> {
                    loadProfilesDisposable = null;
                    if (throwable instanceof EmptyResultSetException) {
                        navigator.notifyChangeSpinner(Collections.emptyList());
                        return;
                    }
                    LogUtils.e("load connection profiles failure." + throwable.toString());
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMqttConnectCompleteEvent(MqttConnectCompleteEvent event) {
        LogUtils.d("MQTT 连接成功,连接到：" + event.serverURI);
        updateConnectionState(true);
        if (event.reconnect) {
            //需要重新订阅主题
            final String[] topics = new String[initStarTopics.size()];
            final QoS[] qoSArr = new QoS[initStarTopics.size()];
            for (int i = 0; i < initStarTopics.size(); i++) {
                topics[i] = initStarTopics.get(i).topic;
                qoSArr[i] = MqttUtils.int2QoS(initStarTopics.get(i).qos);
            }
            if (topics.length > 0) {
                final Disposable subscribeDis = getRepository()
                        .subscribe(topics, qoSArr)
                        .subscribe(() -> LogUtils.d("MQTT 重连订阅成功"),
                                throwable -> LogUtils.e("MQTT 重连订阅失败：" + throwable));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMqttConnectionLostEvent(MqttConnectionLostEvent event) {
        LogUtils.e("MQTT 断开连接：" + event.cause);
        updateConnectionState(false);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttFirstConnectRetryEvent(MqttFirstConnectRetryEvent event) {
        LogUtils.e("MQTT 第一次连接失败,正在重试：" + event.getMessage());
    }


    public interface Navigator {
        /**
         * 通知更新
         */
        void notifyChangeSpinner(List<ConnectionProfile> data);
    }
}
