package cn.liujson.client.ui;


import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import androidx.viewpager2.widget.ViewPager2;


import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxj.xpopup.XPopup;

import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.ubains.lib.mqtt.mod.service.MqttMgr;
import com.ubains.lib.mqtt.mod.ui.MqttWorkingStatusFragment;

import net.lucode.hackware.magicindicator.FragmentContainerHelper;
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.WrapPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityPreviewMainBinding;
import cn.liujson.client.ui.adapter.PageFragmentStateAdapter;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.fragments.LogPreviewFragment;
import cn.liujson.client.ui.fragments.PublishFragment;
import cn.liujson.client.ui.fragments.TopicsFragment;


import cn.liujson.client.ui.util.DoubleClickUtils;

import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.PreviewMainViewModel;

import cn.liujson.client.ui.widget.popup.LoadingTipPopupView;


import cn.ubains.android.ublogger.LogUtils;


import io.reactivex.disposables.Disposable;


public class PreviewMainActivity extends BaseActivity implements PreviewMainViewModel.Navigator {

    private static final String TAG = "PreviewMainActivity";

    public static final String[] mTitleList = new String[]{"Publish", "Topics", "Status", "Log"};

    private ActivityPreviewMainBinding viewDataBinding;

    private final List<String> dataList = new ArrayList<>();
    private final List<ConnectionProfile> oriDataList = new ArrayList<>();

    private PreviewMainViewModel viewModel;

    private Disposable connectingDisposable, disconnectingDisposable, firstAutoReconnectDisposable;

    LoadingTipPopupView loadingTipPopupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview_main);
        viewModel = new PreviewMainViewModel(getLifecycle());
        viewModel.setNavigator(this);
        viewDataBinding.setVm(viewModel);

        initSpinner();
        initViewPager();

        viewDataBinding.mViewPager.setUserInputEnabled(false);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadProfiles();
            if (viewModel.getRepository().isBind() && viewModel.getRepository().isInstalled()) {
                if (viewModel.getRepository().isConnected()) {
                    viewModel.fieldConnectEnable.set(false);
                    viewModel.fieldDisconnectEnable.set(true);
                } else {
                    viewModel.fieldConnectEnable.set(true);
                    viewModel.fieldDisconnectEnable.set(false);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectingDisposable != null) {
            connectingDisposable.dispose();
            connectingDisposable = null;
        }
        if (disconnectingDisposable != null) {
            disconnectingDisposable.dispose();
            disconnectingDisposable = null;
        }
        if (firstAutoReconnectDisposable != null) {
            firstAutoReconnectDisposable.dispose();
            firstAutoReconnectDisposable = null;
        }
        EventBus.getDefault().unregister(this);
        //解除服务绑定
        MqttMgr.instance().unbindToApplication(this);
    }

    /**
     * 初始化下拉菜单
     */
    private void initSpinner() {
        viewDataBinding.spinner.setOnItemSelectedListener((view, position, id, item) -> {

        });
        viewDataBinding.spinner.setOnNothingSelectedListener(spinner -> {

        });
        viewDataBinding.spinner.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.loadProfiles();
            }
        });

        if (viewModel != null) {
            viewModel.loadProfiles();
        }
    }


    /**
     * 通知spinner数据改变
     */
    @Override
    public void notifyChangeSpinner(List<ConnectionProfile> data) {
        LogUtils.d(TAG, "通知spinner数据改变");
        dataList.clear();
        oriDataList.clear();
        for (ConnectionProfile datum : data) {
            dataList.add(datum.profileName);
        }
        viewDataBinding.spinner.setItems(dataList);
        oriDataList.addAll(data);

        if (data.isEmpty()) {
            viewModel.fieldConnectEnable.set(false);
            viewModel.fieldDisconnectEnable.set(false);
        } else if (!viewModel.getRepository().isBind()) {
            viewModel.fieldConnectEnable.set(false);
            viewModel.fieldDisconnectEnable.set(false);
        } else if (viewModel.getRepository().isBind() &&
                viewModel.getRepository().isInstalled() &&
                (viewModel.getRepository().isConnected()
                        || viewModel.getRepository().isConnecting()
                        || viewModel.getRepository().isResting())) {
            viewModel.fieldConnectEnable.set(false);
            viewModel.fieldDisconnectEnable.set(true);
        } else {
            viewModel.fieldConnectEnable.set(true);
            viewModel.fieldDisconnectEnable.set(false);
        }
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        PublishFragment publishFragment = PublishFragment.newInstance();
        TopicsFragment topicsFragment = TopicsFragment.newInstance();
        MqttWorkingStatusFragment workingStatusFragment = MqttWorkingStatusFragment.newInstance();
        LogPreviewFragment logPreviewFragment = LogPreviewFragment.newInstance();

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(publishFragment);
        fragmentList.add(topicsFragment);
        fragmentList.add(workingStatusFragment);
        fragmentList.add(logPreviewFragment);

        viewDataBinding.mViewPager.setAdapter(new PageFragmentStateAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList));

        //指示器
        initMagicIndicator();
    }

    /**
     * 初始化Indicator指示器
     */
    private void initMagicIndicator() {

        MagicIndicator magicIndicator = viewDataBinding.magicIndicator;
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setScrollPivotX(0.35f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return mTitleList.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(0xFF323232);
                simplePagerTitleView.setSelectedColor(0xFFFFFFFF);
                simplePagerTitleView.setText(mTitleList[index]);
                simplePagerTitleView.setTextSize(getResources().getDimensionPixelSize(R.dimen.menu_text_size));
                simplePagerTitleView.setOnClickListener(v -> viewDataBinding.mViewPager.setCurrentItem(index));
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                WrapPagerIndicator indicator = new WrapPagerIndicator(context);
                indicator.setFillColor(Color.parseColor("#3e99f9"));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        LinearLayout titleContainer = commonNavigator.getTitleContainer(); // must after setNavigator
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleContainer.setDividerDrawable(new ColorDrawable() {
            @Override
            public int getIntrinsicWidth() {
                return UIUtil.dip2px(getApplication(), 10);
            }
        });

        final FragmentContainerHelper fragmentContainerHelper = new FragmentContainerHelper(magicIndicator);
        fragmentContainerHelper.setInterpolator(new OvershootInterpolator(2.0f));
        fragmentContainerHelper.setDuration(300);
        viewDataBinding.mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

    /**
     * 设置图标点击
     *
     * @param view
     */
    public void settingClick(View view) {
        if (DoubleClickUtils.isFastDoubleClick(view.getId())) {
            return;
        }
        Intent intent = new Intent(this, ConnectionProfilesActivity.class);
        startActivity(intent);
    }

    /**
     * 连接按钮点击
     *
     * @param view
     */
    public void connectClick(View view) {
        if (DoubleClickUtils.isFastDoubleClick(view.getId())) {
            return;
        }
        if (!dataList.isEmpty()) {
            final ConnectionProfile profile = oriDataList.get(viewDataBinding.spinner.getSelectedIndex());
            final JSONObject jsonObject = (JSONObject) JSON.toJSON(profile);
            connectingDisposable = MqttMgr.instance()
                    .connectTo(jsonObject.toJavaObject(com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile.class), 0)
                    .doFinally(() -> {
                        connectingDisposable = null;
                        hideLoading();
                    })
                    .subscribe(s -> {
                        showLoading(s);
                        LogUtils.d(s);
                    }, throwable -> {
                        ToastHelper.showToast(this, "连接失败");
                        viewModel.fieldConnectEnable.set(true);
                        viewModel.fieldDisconnectEnable.set(false);
                        LogUtils.e("MQTT 第一次连接失败：" + throwable.toString());
                    }, () -> {
                        ToastHelper.showToast(this, "连接成功");
                        viewModel.fieldConnectEnable.set(false);
                        viewModel.fieldDisconnectEnable.set(true);
                        LogUtils.d("MQTT 第一次连接成功");
                    });
        } else {
            ToastHelper.showToast(this, "请先配置连接参数");
        }
    }

    /**
     * 失去连接按钮点击
     *
     * @param view
     */
    public void disconnectClick(View view) {
        if (disconnectingDisposable != null) {
            ToastHelper.showToast(this, "正在断开连接,请稍后...");
            return;
        }
        disconnectingDisposable =
                viewModel.getRepository()
                        .closeSafety()
                        //如果安全断开失败则强制断开连接
                        .onErrorResumeNext(throwable -> {
                            LogUtils.d("MQTT，安全断开连接失败，执行强制断开连接");
                            return viewModel.getRepository().closeForcibly();
                        })
                        .doFinally(() -> {
                            disconnectingDisposable = null;
                        })
                        .subscribe(() -> {
                            viewModel.fieldConnectEnable.set(true);
                            viewModel.fieldDisconnectEnable.set(false);
                            viewModel.getRepository().uninstall();
                            ToastHelper.showToast(this, "断开成功");
                            EventBus.getDefault().post(new ConnectChangeEvent(false));
                            LogUtils.d("MQTT 断开连接成功");
                        }, throwable -> {
                            ToastHelper.showToast(this, "MQTT 断开连接失败");
                            LogUtils.e("MQTT 断开连接失败：" + throwable.toString());

                        });
    }

    /**
     * 显示对话框
     *
     * @param message
     */
    protected void showLoading(String message) {
        if (loadingTipPopupView == null) {
            loadingTipPopupView = (LoadingTipPopupView) new XPopup.Builder(this)
                    .dismissOnTouchOutside(false)
                    .dismissOnBackPressed(false)
                    .hasShadowBg(false)
                    .setPopupCallback(new SimpleCallback() {
                        @Override
                        public void onDismiss(BasePopupView popupView) {
                            loadingTipPopupView = null;
                        }
                    })
                    .asCustom(new LoadingTipPopupView(this, message))
                    .show();
            loadingTipPopupView.setOnCloseBtnClickListener((popupView, v) -> {
                if (firstAutoReconnectDisposable != null) {
                    firstAutoReconnectDisposable.dispose();
                    firstAutoReconnectDisposable = null;
                }
                LogUtils.d("MQTT首次重连，用户手动取消重连功能");
            });
        } else {
            loadingTipPopupView.setTvContent(message);
            loadingTipPopupView.show();
        }
    }

    /**
     * 隐藏对话框
     */
    protected void hideLoading() {
        if (loadingTipPopupView != null) {
            loadingTipPopupView.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectChangeEvent(ConnectChangeEvent event) {
        if (event.isConnected) {
            viewModel.fieldConnectEnable.set(false);
            viewModel.fieldDisconnectEnable.set(true);
        } else {
            viewModel.fieldConnectEnable.set(true);
            viewModel.fieldDisconnectEnable.set(false);
        }
    }
}