package cn.liujson.client.ui.viewmodel;


import android.graphics.Rect;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.liujson.client.R;
import cn.liujson.client.ui.adapter.MessageListAdapter;
import cn.liujson.client.ui.adapter.TopicListAdapter;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.bean.event.SystemLowMemoryEvent;
import cn.liujson.client.ui.service.ConnectionBinder;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.client.ui.widget.divider.DividerLinearItemDecoration;
import cn.liujson.lib.mqtt.api.QoS;

import cn.ubains.android.ublogger.LogUtils;
import io.reactivex.disposables.Disposable;

/**
 * 发布
 *
 * @author liujson
 * @date 2021/3/10.
 */
public class TopicsViewModel extends BaseViewModel implements
        ConnectionBinder.OnRecMsgListener {


    public final ObservableList<TopicListAdapter.SubTopicItem> dataList = new ObservableArrayList<>();
    public final TopicListAdapter adapter = new TopicListAdapter(dataList);
    public final LinearLayoutManager layoutManager = new LinearLayoutManager(CustomApplication.getApp());
    public final DividerLinearItemDecoration itemDecoration = new DividerLinearItemDecoration(CustomApplication.getApp(),
            DividerLinearItemDecoration.VERTICAL_LIST, 2, R.color.color_d6d6d6);

    public final List<MessageListAdapter.MsgItem> msgDataList = new LinkedList<>();
    public final MessageListAdapter msgListAdapter = new MessageListAdapter(msgDataList);
    public final LinearLayoutManager msgListManager = new LinearLayoutManager(CustomApplication.getApp());
    public final DividerItemDecoration msgDividerItemDecoration = new DividerItemDecoration();

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);
    public final ObservableField<CharSequence> fieldInputTopic = new ObservableField<>();

    public final ObservableField<CharSequence> fieldMessageTopic = new ObservableField<>();
    public final ObservableBoolean fieldMessageEnable = new ObservableBoolean();
    public final ObservableField<CharSequence> fieldMessageTime = new ObservableField<>();
    public final ObservableField<CharSequence> fieldMessageQoS = new ObservableField<>();

    /**
     * 消息缓存Map
     */
    private final Map<String, LinkedList<MessageListAdapter.MsgItem>> cacheLinkedMap = new LinkedHashMap<>();


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
        adapter.setOnItemClickListener((adapter, view, position) -> {
            final TopicListAdapter.SubTopicItem subTopicItem = dataList.get(position);
            notifyMsgListAdapter(subTopicItem.topic);
        });
        adapter.addChildClickViewIds(R.id.btn_unsubscribe);

        if (getRepository().isBind()) {
            repository.addOnRecMsgListener(this);
        }

        EventBus.getDefault().register(this);
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

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSystemLowMemoryEvent(SystemLowMemoryEvent event) {
        //低内存时清除缓存消息

    }


    public ConnectionServiceRepository getRepository() {
        return repository;
    }

    public void updateDataList(List<TopicListAdapter.SubTopicItem> subList) {
        if (subList.isEmpty()) {
            fieldMessageTopic.set("");
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
        //根据Topic区别，添加到消息缓存
        LinkedList<MessageListAdapter.MsgItem> msgList = cacheLinkedMap.get(topic);
        if (msgList == null) {
            msgList = new LinkedList<>();
        }
        final MessageListAdapter.MsgItem msgItem = new MessageListAdapter.MsgItem();
        msgItem.topic = topic;
        msgItem.message = new String(payload);
        msgItem.messageDate = System.currentTimeMillis();
        msgItem.qoS = qoS;
        msgList.add(msgItem);
        cacheLinkedMap.put(topic,msgList);
        //刷新左侧消息数量


        notifyMsgListAdapter(topic);
    }

    private void notifyMsgListAdapter(String topic) {
        //判断当前topic是否选中显示
        if(dataList.size()==1){
            msgDataList.clear();
            LinkedList<MessageListAdapter.MsgItem> msgList = cacheLinkedMap.get(topic);
            if (msgList == null) {
                msgList = new LinkedList<>();
            }
            msgDataList.addAll(msgList);
            msgListAdapter.notifyDataSetChanged();
            msgListAdapter.getRecyclerView().smoothScrollToPosition(msgDataList.size());
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


    /**
     * 分隔线
     */
    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
            outRect.set(0, 0, 0, 8);
        }
    }
}
