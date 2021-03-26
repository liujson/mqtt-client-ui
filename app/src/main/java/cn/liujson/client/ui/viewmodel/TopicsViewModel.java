package cn.liujson.client.ui.viewmodel;


import android.util.Pair;
import android.view.View;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import cn.liujson.client.R;
import cn.liujson.client.ui.adapter.MessageCacheAdapter;
import cn.liujson.client.ui.adapter.TopicListAdapter;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.service.ConnectionBinder;
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
        ConnectionBinder.OnRecMsgListener {
    /**
     * 每个主题最大缓存消息数 50 条
     */
    public static final int TOPIC_CACHE_MESSAGE_SIZE = 50;


    public final ObservableList<TopicListAdapter.SubTopicItem> dataList = new ObservableArrayList<>();
    public final TopicListAdapter adapter = new TopicListAdapter(dataList);
    public final LinearLayoutManager layoutManager = new LinearLayoutManager(CustomApplication.getApp());
    public final DividerLinearItemDecoration itemDecoration = new DividerLinearItemDecoration(CustomApplication.getApp(),
            DividerLinearItemDecoration.VERTICAL_LIST, 2, R.color.color_d6d6d6);

    public final List<MessageCacheAdapter.MqttMsg> msgDataList = new CopyOnWriteArrayList<>();
    public final MessageCacheAdapter msgAdapter = new MessageCacheAdapter(msgDataList);
    public final LinearLayoutManager msgLayoutManager = new LinearLayoutManager(CustomApplication.getApp());

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);
    public final ObservableField<CharSequence> fieldInputTopic = new ObservableField<>();

    public final ObservableField<CharSequence> fieldMessageTopic = new ObservableField<>();
    public final ObservableField<CharSequence> fieldMessageContent = new ObservableField<>();
    public final ObservableBoolean fieldMessageEnable = new ObservableBoolean();
    public final ObservableField<CharSequence> fieldMessageTime = new ObservableField<>();
    public final ObservableField<CharSequence> fieldMessageQoS = new ObservableField<>();


    private final ConnectionServiceRepository repository;

    private Disposable subscribeDisposable, unsubscribeDisposable;

    private Navigator navigator;

    private volatile boolean unsubscribe_ing = false;


    public TopicsViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);

        repository = new ConnectionServiceRepository();

        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (adapter instanceof TopicListAdapter) {
                if (view.getId() == R.id.btn_unsubscribe) {
                    final List<TopicListAdapter.SubTopicItem> data = ((TopicListAdapter) adapter).getData();
                    unsubscribe(data.get(position).topic);
                }
            }
        });
        adapter.addChildClickViewIds(R.id.btn_unsubscribe);

        if (getRepository().isBind()) {
            repository.addOnRecMsgListener(this);
        }
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

    public void updateDataList(List<TopicListAdapter.SubTopicItem> subList) {
        if (subList.isEmpty()) {
            msgDataList.clear();
            msgAdapter.notifyDataSetChanged();

            fieldMessageTopic.set("");
            fieldMessageContent.set("");
            fieldMessageTime.set("");
            fieldMessageQoS.set("");
        }
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
                    final List<Pair<String, QoS>> pairList = getRepository().getSubList();
                    final List<TopicListAdapter.SubTopicItem> subTopicItems = new ArrayList<>();
                    for (Pair<String, QoS> sPair : pairList) {
                        subTopicItems.add(new TopicListAdapter.SubTopicItem(sPair.first, sPair.second));
                    }
                    updateDataList(subTopicItems);
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
                    ToastHelper.showToast(CustomApplication.getApp(), "取消订阅成功");
                    LogUtils.i("MQTT 取消订阅成功，topic:" + topic);
                    final List<Pair<String, QoS>> pairList = getRepository().getSubList();
                    final List<TopicListAdapter.SubTopicItem> subTopicItems = new ArrayList<>();
                    for (Pair<String, QoS> sPair : pairList) {
                        subTopicItems.add(new TopicListAdapter.SubTopicItem(sPair.first, sPair.second));
                    }
                    updateDataList(subTopicItems);
                }, throwable -> {
                    unsubscribe_ing = false;
                    ToastHelper.showToast(CustomApplication.getApp(), "取消订阅失败");
                    LogUtils.i("MQTT 取消订阅失败，topic:" + topic);
                });
    }


    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onReceiveMessage(String topic, byte[] payload, QoS qoS) {
        if (navigator != null) {
            navigator.onReceiveMessage(topic, new String(payload), qoS);
        }
        //添加到消息缓存
        if (msgDataList.size() > TOPIC_CACHE_MESSAGE_SIZE) {
            msgDataList.remove(0);
        }
        MessageCacheAdapter.MqttMsg mqttMsg = new MessageCacheAdapter.MqttMsg();
        mqttMsg.topic = topic;
        mqttMsg.body = payload;
        mqttMsg.qos = qoS;
        mqttMsg.receiveTime = System.currentTimeMillis();
        msgDataList.add(mqttMsg);

        final Optional<TopicListAdapter.SubTopicItem> first = dataList.stream().filter(item -> Objects.equals(item.topic, topic)).findFirst();
        if (first.isPresent()) {
            final long count = msgDataList.stream().filter(item -> Objects.equals(item.topic, topic)).count();
            first.get().msgCount = (int) count;
            adapter.notifyDataSetChanged();
        }

        msgAdapter.notifyDataSetChanged();
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
}
