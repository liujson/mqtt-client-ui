package cn.liujson.client.ui.viewmodel;

import android.util.Pair;
import android.view.View;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.liujson.client.R;
import cn.liujson.client.ui.adapter.TopicListAdapter;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.service.ConnectionBinder;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.client.ui.widget.divider.DividerLinearItemDecoration;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.logger.LogUtils;
import io.reactivex.disposables.Disposable;

/**
 * 发布
 *
 * @author liujson
 * @date 2021/3/10.
 */
public class TopicsViewModel extends BaseViewModel implements
        ConnectionServiceRepository.OnBindStatus, ConnectionBinder.OnRecMsgListener {

    public final ObservableList<Pair<String, QoS>> dataList = new ObservableArrayList<>();
    public final TopicListAdapter adapter = new TopicListAdapter(dataList);
    public final LinearLayoutManager layoutManager = new LinearLayoutManager(CustomApplication.getApp());
    public final DividerLinearItemDecoration itemDecoration = new DividerLinearItemDecoration(CustomApplication.getApp(),
            DividerLinearItemDecoration.VERTICAL_LIST, 2, R.color.color_d6d6d6);

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);
    public final ObservableField<CharSequence> fieldInputTopic = new ObservableField<>();

    public final ObservableField<CharSequence> fieldMessageTopic = new ObservableField<>();
    public final ObservableField<CharSequence> fieldMessageContent = new ObservableField<>();
    public final ObservableBoolean fieldMessageEnable = new ObservableBoolean();
    public final ObservableField<CharSequence> fieldMessageTime = new ObservableField<>();
    public final ObservableField<CharSequence> fieldMessageQoS = new ObservableField<>();

    /**
     * 接收到消息的列表
     */
    private final Map<String, List<?>> receiveMessageList = new HashMap<>();

    private final ConnectionServiceRepository repository;

    private Disposable subscribeDisposable, unsubscribeDisposable;

    private Navigator navigator;

    private volatile boolean unsubscribe_ing = false;


    public TopicsViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);

        repository = new ConnectionServiceRepository(this);

        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (adapter instanceof TopicListAdapter) {
                if (view.getId() == R.id.btn_unsubscribe) {
                    final List<Pair<String, QoS>> data = ((TopicListAdapter) adapter).getData();
                    unsubscribe(data.get(position).first);
                }
            }
        });
        adapter.addChildClickViewIds(R.id.btn_unsubscribe);
    }

    @Override
    public void onRelease() {
        if (subscribeDisposable != null) {
            subscribeDisposable.dispose();
        }
        if (unsubscribeDisposable != null) {
            unsubscribeDisposable.dispose();
        }
        repository.removeOnRecMsgListener(this);
    }


    public ConnectionServiceRepository getRepository() {
        return repository;
    }

    public void updateDataList(List<Pair<String, QoS>> subList) {
        dataList.clear();
        dataList.addAll(subList);
        adapter.notifyDataSetChanged();
    }

    /**
     * 订阅主题
     *
     * @param view
     */
    public void subscribe(View view) {
        if (navigator == null || !navigator.checkParam()) {
            return;
        }
        final CharSequence topic = fieldInputTopic.get();
        subscribeDisposable = repository.subscribe(topic.toString(), navigator.readQos())
                .doOnSubscribe(disposable -> {
                    view.setEnabled(false);
                })
                .subscribe(() -> {
                    view.setEnabled(true);
                    updateDataList(getRepository().getSubList());
                    ToastHelper.showToast(CustomApplication.getApp(), "订阅成功");
                    LogUtils.i("MQTT 订阅成功，topic:" + topic);
                }, throwable -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "订阅失败");
                    LogUtils.i("MQTT 订阅失败，topic:" + topic);
                });
    }

    /**
     * 取消订阅主题
     *
     * @param topic
     */
    public void unsubscribe(String topic) {
        if (unsubscribe_ing) {
            ToastHelper.showToast(CustomApplication.getApp(), "请稍后再试");
            return;
        }
        unsubscribeDisposable = repository.unsubscribe(topic)
                .doOnSubscribe(disposable -> {
                    unsubscribe_ing = true;
                })
                .subscribe(() -> {
                    unsubscribe_ing = false;
                    updateDataList(getRepository().getSubList());
                    ToastHelper.showToast(CustomApplication.getApp(), "取消订阅成功");
                    LogUtils.i("MQTT 取消订阅成功，topic:" + topic);
                    updateDataList(getRepository().getSubList());
                }, throwable -> {
                    unsubscribe_ing = false;
                    ToastHelper.showToast(CustomApplication.getApp(), "取消订阅失败");
                    LogUtils.i("MQTT 取消订阅失败，topic:" + topic);
                });
    }


    @Override
    public void onBindSuccess(ConnectionBinder serviceBinder) {
        if (serviceBinder.isInstalled()) {
            if (serviceBinder.getClient().isConnected()) {
                EventBus.getDefault().post(new ConnectChangeEvent(true));
            } else {
                EventBus.getDefault().post(new ConnectChangeEvent(false));
            }
            repository.addOnRecMsgListener(this);
        }
    }

    @Override
    public void onBindFailure() {

    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onReceiveMessage(String topic, byte[] payload, QoS qoS) {
        if (navigator != null) {
            navigator.onReceiveMessage(topic, new String(payload), qoS);
        }
    }


    public interface Navigator {
        /**
         * 检查发布参数
         *
         * @return
         */
        boolean checkParam();


        /**
         * 读取 qos
         */
        QoS readQos();

        /**
         * 接收到消息
         */
        void onReceiveMessage(String topic, String message, QoS qoS);
    }


    public static class MqttMsg {
        public String topic;
        public byte[] body;
    }
}
