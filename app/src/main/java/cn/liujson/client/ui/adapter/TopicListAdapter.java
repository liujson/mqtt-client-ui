package cn.liujson.client.ui.adapter;

import android.util.Pair;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.liujson.client.R;
import cn.liujson.lib.mqtt.api.QoS;

/**
 * @author liujson
 * @date 2021/3/16.
 */
public class TopicListAdapter extends BaseQuickAdapter<Pair<String, QoS>, BaseViewHolder> {

    public TopicListAdapter(@Nullable List<Pair<String, QoS>> data) {
        super(R.layout.item_sub_topic_list, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, Pair<String, QoS> s) {
        holder.setText(R.id.tv_topic, s.first);
        holder.setText(R.id.tv_qos, s.second.qoSName());
    }
}
