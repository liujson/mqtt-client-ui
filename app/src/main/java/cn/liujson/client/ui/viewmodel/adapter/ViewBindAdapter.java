package cn.liujson.client.ui.viewmodel.adapter;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ViewBindAdapter {


    @BindingAdapter(value = {"recyclerAdapter", "recyclerLayoutManager", "recyclerData"})
    public static <T, VH extends BaseQuickAdapter<T, BaseViewHolder>>
    void bindRecyclerViewAdapter(RecyclerView recyclerView, VH adapter,
                                 RecyclerView.LayoutManager layoutManager,
                                 ObservableList<T> dataList) {
        if (adapter != null && dataList != null && recyclerView.getAdapter() == adapter) {
            if (adapter.getData() == dataList) {
                adapter.notifyDataSetChanged();
                return;
            }
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    @BindingAdapter("recyclerItemDecoration")
    public static void bindRecyclerItemDecoration(RecyclerView recyclerView,RecyclerView.ItemDecoration itemDecoration) {
        if (itemDecoration != null) {
            recyclerView.addItemDecoration(itemDecoration);
        }
    }



}
