package cn.liujson.client.ui.adapter;


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
public class TopicListAdapter extends BaseQuickAdapter<TopicListAdapter.SubTopicItem, BaseViewHolder> {

    public TopicListAdapter(@Nullable List<SubTopicItem> data) {
        super(R.layout.item_sub_topic_list, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SubTopicItem s) {
        holder.setText(R.id.tv_topic, s.topic);
        holder.setText(R.id.tv_qos, s.qos.qoSName());
        holder.setText(R.id.tv_message_num, String.valueOf(s.msgCount));
    }


    public static class SubTopicItem {
        public String topic;
        public QoS qos;
        public int msgCount;

        public SubTopicItem(String topic, QoS qos) {
            this.topic = topic;
            this.qos = qos;
        }
    }
}
