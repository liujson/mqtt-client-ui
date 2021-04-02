package cn.liujson.client.ui.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.room.EmptyResultSetException;

import com.alibaba.fastjson.JSON;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import com.daimajia.swipe.SwipeLayout;
import com.lxj.xpopup.XPopup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;

import cn.liujson.client.R;


import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.app.runtime.RuntimeRequire;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.dao.ConnectionProfileStarDao;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.db.entities.ConnectionProfileStar;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.widget.popup.MarkStarPopupView;


import cn.ubains.android.ublogger.LogUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ConnectionProfilesAdapter extends BaseQuickAdapter<ConnectionProfilesAdapter.ItemProfile, BaseViewHolder> {


    public ConnectionProfilesAdapter(@Nullable List<ItemProfile> data) {
        super(R.layout.item_connection_profile, data);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void convert(@NotNull BaseViewHolder holder, ItemProfile itemProfile) {
        final SwipeLayout swipeLayout = holder.findView(R.id.swipe);
        holder.setText(R.id.tv_profile_name, itemProfile.profileName);
        holder.setText(R.id.tv_broker, itemProfile.brokerAddress + ":" + itemProfile.brokerPort);

        assert swipeLayout != null;
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.rl_actions));
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        final CheckBox cbStar = holder.findView(R.id.cb_star);
        assert cbStar != null;
        cbStar.setChecked(itemProfile.isStar);
//        cbStar.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            final int position = holder.getAdapterPosition();
//            final ItemProfile profile = getData().get(position);
//            getData().stream()
//                    .filter(itemProfile1 -> profile != itemProfile1)
//                    .forEach(item -> item.isStar = false);
//            profile.isStar = isChecked;
//
//            mHandler.post(this::notifyDataSetChanged);
//        });

        // TODO: 2021/3/26
        cbStar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (cbStar.isChecked()) {
                    //取消标记
                    return true;
                }
                //执行标记逻辑
                final MarkStarPopupView markStarPopupView = new MarkStarPopupView(v.getContext());
                markStarPopupView.setOnMarkBtnClickListener((d, view) -> {
                    final MarkStarPopupView markView = (MarkStarPopupView) d;

                    markStar(cbStar, markView.getTopics(), holder.getAdapterPosition());
                    d.dismiss();
                });
                markStarPopupView.setOnCancelBtnClickListener((d, view) -> {
                    d.dismiss();
                });
                new XPopup.Builder(v.getContext())
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(false)
                        .asCustom(markStarPopupView)
                        .show();
                return true;
            }
            return false;
        });
    }

    /**
     * 标记逻辑
     */
    private void markStar(final CheckBox checkBox,
                          final List<MarkStarPopupView.TopicWrapper> topics, final int position) {
        //数据库标记这个id
        final ConnectionProfileStarDao starDao = DatabaseHelper
                .getInstance()
                .starDao();
        final List<ItemProfile> dataList = getData();
        final ItemProfile curProfile = dataList.get(position);
        final Disposable subscribe = starDao.count()
                .flatMapCompletable(count -> {
                    if (count == 0) {
                        final ConnectionProfileStar connectionProfileStar = new ConnectionProfileStar();
                        connectionProfileStar.updateDate = new Date();
                        connectionProfileStar.connectionProfileId = curProfile.id;
                        connectionProfileStar.defineTopics = JSON.toJSONString(topics);
                        return starDao.insertStar(connectionProfileStar);
                    } else {
                        return starDao.markStar(curProfile.id, JSON.toJSONString(topics), new Date());
                    }
                })
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    ToastHelper.showToast(CustomApplication.getApp(), "Saved!");
                    //保存到Runtime
                    RuntimeRequire.getInstance().updateDefineTopics(topics);
                    checkBox.setChecked(true);
                }, throwable -> {
                    if (throwable instanceof EmptyResultSetException) {
                        return;
                    }
                    LogUtils.e(throwable, "failure!");
                });
    }

    public static class ItemProfile {

        public int id;
        public String profileName;
        public String brokerAddress;
        public int brokerPort;
        public boolean isStar;

        public static ItemProfile covert(@NonNull ConnectionProfile connectionProfile) {
            ItemProfile itemProfile = new ItemProfile();
            itemProfile.profileName = connectionProfile.profileName;
            itemProfile.id = connectionProfile.id;
            itemProfile.brokerAddress = connectionProfile.brokerAddress;
            itemProfile.brokerPort = connectionProfile.brokerPort;
            return itemProfile;
        }
    }
}
