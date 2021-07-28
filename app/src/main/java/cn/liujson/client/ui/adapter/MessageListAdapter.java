package cn.liujson.client.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.liujson.client.R;
import cn.liujson.client.ui.bean.entity.MsgItem;
import cn.liujson.lib.mqtt.api.QoS;

/**
 * @author liujson
 * @date 2021/4/8.
 */
public class MessageListAdapter extends BaseQuickAdapter<MsgItem, BaseViewHolder> {

    final SimpleDateFormat format;
    final Date date;

    public MessageListAdapter(@Nullable List<MsgItem> data) {
        super(R.layout.item_topic_rv_message, data);

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        date = new Date();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MsgItem msgItem) {
        StringBuilder builder = new StringBuilder();
        date.setTime(msgItem.messageDate);
        builder.append("> ");
        builder.append(format.format(date));
        builder.append(" -- ");
        builder.append(msgItem.qoS.qoSName()).append(",").append("message:").append(msgItem.message);
        holder.setText(R.id.tv_log_line, builder.toString());
    }


}
