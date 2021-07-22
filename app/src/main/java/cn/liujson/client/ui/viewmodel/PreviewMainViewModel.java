package cn.liujson.client.ui.viewmodel;


import android.text.TextUtils;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;
import androidx.room.EmptyResultSetException;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ubains.lib.mqtt.mod.provider.event.MqttConnectCompleteEvent;

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
                return dao
                        .loadProfiles();
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


    /**
     * 初始化自动订阅
     *
     * @return
     */
    private Completable autoSubscribe() {
        return Observable.just(0)
                .flatMapCompletable(new Function<Integer, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@NonNull Integer o) throws Exception {
                        //订阅初始化需要的主题
                        final String[] topics = new String[initStarTopics.size()];
                        final QoS[] qoSArr = new QoS[initStarTopics.size()];
                        for (int i = 0; i < initStarTopics.size(); i++) {
                            topics[i] = initStarTopics.get(i).topic;
                            qoSArr[i] = MqttUtils.int2QoS(initStarTopics.get(i).qos);
                        }
                        if (topics.length > 0) {
                            return getRepository().subscribe(topics, qoSArr);
                        } else {
                            return Completable.complete();
                        }
                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttConnectCompleteEvent(MqttConnectCompleteEvent event) {
        EventBus.getDefault().post(new ConnectChangeEvent(true));
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


    public interface Navigator {
        /**
         * 通知更新
         */
        void notifyChangeSpinner(List<ConnectionProfile> data);
    }
}
