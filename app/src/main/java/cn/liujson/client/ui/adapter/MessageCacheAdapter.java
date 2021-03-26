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
 * @date 2021/3/24.
 */
public class MessageCacheAdapter extends BaseQuickAdapter<MessageCacheAdapter.MqttMsg, BaseViewHolder> {

    public MessageCacheAdapter(@Nullable List<MqttMsg> data) {
        super(R.layout.item_rv_message_cache, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MqttMsg item) {
        holder.setText(R.id.tv_message_topic, item.topic);
        holder.setText(R.id.tv_message_qos, item.qos.qoSName());
    }

    public static class MqttMsg {
        public String topic;
        public byte[] body;
        public QoS qos;
        public long receiveTime;
    }
}
