package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityPreviewMainBinding;
import cn.liujson.client.ui.adapter.PageFragmentStateAdapter;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.fragments.LogPreviewFragment;
import cn.liujson.client.ui.fragments.PublishFragment;
import cn.liujson.client.ui.fragments.TopicsFragment;
import cn.liujson.client.ui.fragments.WorkingStatusFragment;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.PreviewMainViewModel;
import cn.liujson.lib.mqtt.service.MqttBuilder;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;


public class PreviewMainActivity extends AppCompatActivity implements PreviewMainViewModel.Navigator {

    private static final String TAG = "PreviewMainActivity";

    public static final String[] mTitleList = new String[]{"Publish", "Topics", "Log", "Status"};

    private ActivityPreviewMainBinding viewDataBinding;

    private final List<String> dataList = new ArrayList<>();
    private final List<ConnectionProfile> oriDataList = new ArrayList<>();


    PreviewMainViewModel viewModel;

    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview_main);
        viewModel = new PreviewMainViewModel(getLifecycle());
        viewModel.setNavigator(this);
        viewDataBinding.setVm(viewModel);

        viewModel.getRepository().bindConnectionService(this);

        initSpinner();
        initViewPager();


        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadProfiles();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }

        viewModel.getRepository().unbindConnectionService();
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
        dataList.clear();
        oriDataList.clear();
        for (ConnectionProfile datum : data) {
            dataList.add(datum.profileName);
        }
        viewDataBinding.spinner.setItems(dataList);
        oriDataList.addAll(data);

        if (!data.isEmpty()) {
            viewModel.fieldConnectEnable.set(true);
        }
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        PublishFragment publishFragment = PublishFragment.newInstance();
        TopicsFragment topicsFragment = TopicsFragment.newInstance();
        LogPreviewFragment logPreviewFragment = LogPreviewFragment.newInstance();
        WorkingStatusFragment workingStatusFragment = WorkingStatusFragment.newInstance();

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(publishFragment);
        fragmentList.add(topicsFragment);
        fragmentList.add(logPreviewFragment);
        fragmentList.add(workingStatusFragment);

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
        Intent intent = new Intent(this, ConnectionProfilesActivity.class);
        startActivity(intent);
    }

    /**
     * 连接按钮点击
     *
     * @param view
     */
    public void connectClick(View view) {
        if (!dataList.isEmpty()) {
            final ConnectionProfile profile = oriDataList.get(viewDataBinding.spinner.getSelectedIndex());
            Completable actionCompletable = viewModel.getRepository()
                    .connect().observeOn(AndroidSchedulers.mainThread());
            //如果已经配置和已经连接上则断开连接然后再创建新的连接
            if (viewModel.getRepository().isSetup()) {
                if (viewModel.getRepository().isConnected()) {
                    actionCompletable = viewModel.getRepository().closeSafety()
                            //如果安全断开失败则强制断开连接
                            .onErrorResumeNext(throwable -> viewModel.getRepository().closeForcibly())
                            .andThen(actionCompletable);
                }
            } else {
                //如果未安装则进行安装
                final IMQTTWrapper<PahoV3MQTTClient> wrapper = viewModel.create(profile);
                if (wrapper != null) {
                    viewModel.getRepository().setup(wrapper);
                }
            }
            //执行连接逻辑
            Disposable subscribe = actionCompletable
                    .subscribe(() -> {
                        ToastHelper.showToast(this, "连接成功");
                        viewModel.fieldConnectEnable.set(false);
                        viewModel.fieldDisconnectEnable.set(true);
                        EventBus.getDefault().post(new ConnectChangeEvent(true));
                    }, throwable -> {
                        ToastHelper.showToast(this, "连接失败");
                        viewModel.fieldConnectEnable.set(true);
                        viewModel.fieldDisconnectEnable.set(false);
                        EventBus.getDefault().post(new ConnectChangeEvent(false));
                    });

            mCompositeDisposable.add(subscribe);
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
        final Disposable subscribe =
                viewModel.getRepository()
                        .closeSafety()
                        //如果安全断开失败则强制断开连接
                        .onErrorResumeNext(throwable -> viewModel.getRepository().closeForcibly())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            viewModel.fieldConnectEnable.set(true);
                            viewModel.fieldDisconnectEnable.set(false);
                            ToastHelper.showToast(this, "断开成功");
                        }, throwable -> {
                            ToastHelper.showToast(this, "断开失败");
                        });
        mCompositeDisposable.add(subscribe);
    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {
        ToastHelper.showToast(this, "绑定服务成功");
    }

    @Override
    public void onBindFailure() {
        ToastHelper.showToast(this, "绑定服务失败");
    }
}