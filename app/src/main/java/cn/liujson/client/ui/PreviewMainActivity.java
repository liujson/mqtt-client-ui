package cn.liujson.client.ui;


import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.room.EmptyResultSetException;
import androidx.viewpager2.widget.ViewPager2;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;

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

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityPreviewMainBinding;
import cn.liujson.client.ui.adapter.PageFragmentStateAdapter;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.fragments.LogPreviewFragment;
import cn.liujson.client.ui.fragments.PublishFragment;
import cn.liujson.client.ui.fragments.TopicsFragment;
import cn.liujson.client.ui.fragments.WorkingStatusFragment;


import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.client.ui.util.DoubleClickUtils;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.PreviewMainViewModel;

import cn.liujson.client.ui.widget.popup.LoadingTipPopupView;
import cn.liujson.client.ui.widget.popup.interfaces.OnPopupClickListener;
import cn.liujson.client.ui.widget.retry.RxReconnectDelayFlowable;
import cn.liujson.lib.mqtt.api.ConnectionParams;


import cn.ubains.android.ublogger.LogUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

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

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ToastHelper.showToast(getApplicationContext(), "MQTT服务绑定成功");
            viewModel.fieldConnectEnable.set(true);
            viewModel.fieldDisconnectEnable.set(false);
            //查询数据库，是否包含标记为星号的连接项，存在则尝试对其进行连接
            if (viewModel != null) {
                final RxReconnectDelayFlowable rxRetry = new RxReconnectDelayFlowable();
                rxRetry.setOnRetrying((retryCount, nextDelay) -> {
                    LogUtils.d("MQTT 初始化连接失败，正在重试，第" + retryCount + "次,下次重试延时：" + nextDelay + "ms");
                    runOnUi(() -> {
                        showLoading("正在进行第" + retryCount + "次重连");
                    });
                });
                firstAutoReconnectDisposable = viewModel.initStarProfileConnect()
                        .doOnSubscribe(disposable -> {
                            runOnUi(() -> {
                                showLoading("正在连接中");
                            });
                        })
                        .retryWhen(rxRetry)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            hideLoading();
                            ToastHelper.showToast(CustomApplication.getApp(), "初始化连接成功");
                            viewModel.fieldConnectEnable.set(false);
                            viewModel.fieldDisconnectEnable.set(true);
                            LogUtils.d("MQTT 初始化连接成功");
                        }, throwable -> {
                            hideLoading();
                            if (throwable instanceof EmptyResultSetException) {
                                //do anything
                                return;
                            }
                            ToastHelper.showToast(CustomApplication.getApp(), "初始化连接失败");
                            viewModel.fieldConnectEnable.set(true);
                            viewModel.fieldDisconnectEnable.set(false);
                            LogUtils.e("MQTT 初始化连接失败：" + throwable.toString());
                        });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ToastHelper.showToast(getApplicationContext(), "MQTT服务绑定失败");
        }
    };

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


        //把MQTT绑定到这个服务
        MqttMgr.instance().bindToActivity(this, serviceConnection);
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
        //解除服务绑定
        MqttMgr.instance().unbindService(this, serviceConnection);
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
        WorkingStatusFragment workingStatusFragment = WorkingStatusFragment.newInstance();
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
            if (connectingDisposable != null) {
                ToastHelper.showToast(this, "正在连接中,请稍后...");
                return;
            }
            final ConnectionProfile profile = oriDataList.get(viewDataBinding.spinner.getSelectedIndex());
            final ConnectionParams params = viewModel.profile2Params(profile);
            //执行连接逻辑
            connectingDisposable = viewModel.setupAndConnect(params)
                    .doFinally(() -> {
                        connectingDisposable = null;
                    })
                    .subscribe(() -> {
                        ToastHelper.showToast(this, "连接成功");
                        viewModel.fieldConnectEnable.set(false);
                        viewModel.fieldDisconnectEnable.set(true);
                        LogUtils.d("MQTT 第一次连接成功");
                    }, throwable -> {
                        ToastHelper.showToast(this, "连接失败");
                        viewModel.fieldConnectEnable.set(true);
                        viewModel.fieldDisconnectEnable.set(false);
                        LogUtils.e("MQTT 第一次连接失败：" + throwable.toString());
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
}