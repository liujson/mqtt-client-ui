package com.ubains.lib.mqtt.mod.ui.vm;

import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;

import com.ubains.lib.mqtt.mod.service.MqttMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;


/**
 * @author liujson
 * @date 2022/10/21.
 */
public class MqttSubscribedTopicsViewModel extends BaseObservable {

    public final ObservableBoolean noDataVisible = new ObservableBoolean(false);

    public final List<String> dataList = new ArrayList<>();



    public List<String> getTopics() {
        if (!MqttMgr.instance().isBind() ||
                !MqttMgr.instance().isInstalled() ||
                !MqttMgr.instance().isConnected()) {
            setNoDataVisible(true);
            return Collections.emptyList();
        }
        final RxPahoClient client = MqttMgr.instance().getClient();
        final List<Pair<String, QoS>> activeSubs = client.getActiveSubs();
        final List<String> collect = activeSubs.stream()
                .map(stringQoSPair -> stringQoSPair.first)
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            setNoDataVisible(true);
            return Collections.emptyList();
        }
        setNoDataVisible(false);
        return collect;
    }


    public void setNoDataVisible(boolean b) {
        noDataVisible.set(b);
    }

    /**
     * 刷新状态
     */
    public void refreshData(ArrayAdapter<String> simpleAdapter) {
        dataList.clear();
        final List<String> topics = getTopics();
        dataList.addAll(topics);
        simpleAdapter.notifyDataSetChanged();
    }
}
