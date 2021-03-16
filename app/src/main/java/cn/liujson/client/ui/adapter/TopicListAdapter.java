package cn.liujson.client.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.liujson.client.R;

/**
 * @author liujson
 * @date 2021/3/16.
 */
public class TopicListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public TopicListAdapter(@Nullable List<String> data) {
        super(R.layout.item_sub_topic_list, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, String s) {
        holder.setText(R.id.tv_topic, s);
    }
}
