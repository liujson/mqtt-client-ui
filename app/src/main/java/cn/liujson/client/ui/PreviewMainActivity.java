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

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityPreviewMainBinding;
import cn.liujson.client.ui.adapter.PageFragmentStateAdapter;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.fragments.LogPreviewFragment;
import cn.liujson.client.ui.fragments.PublishFragment;
import cn.liujson.client.ui.fragments.TopicsFragment;
import cn.liujson.client.ui.fragments.WorkingStatusFragment;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.service.MqttSM;
import cn.liujson.client.ui.viewmodel.PreviewMainViewModel;
import cn.liujson.lib.mqtt.service.MqttBuilder;


public class PreviewMainActivity extends AppCompatActivity implements PreviewMainViewModel.Navigator {

    private static final String TAG = "PreviewMainActivity";

    public static final String[] mTitleList = new String[]{"Publish", "Topics", "Log", "Status"};

    private ActivityPreviewMainBinding viewDataBinding;

    private final List<String> dataList = new ArrayList<>();
    private final List<ConnectionProfile> oriDataList = new ArrayList<>();


    PreviewMainViewModel viewModel;

    private int count = 0;

    private ConnectionService.ConnectionServiceBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview_main);
        viewModel = new PreviewMainViewModel(getLifecycle());
        viewDataBinding.setVm(viewModel);

        initSpinner();
        initViewPager();

        // TODO: 2021/3/8
        //绑定 MQTT Service
        MqttSM.instance().bindToActivity(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // 安装MQTT
                binder = (ConnectionService.ConnectionServiceBinder) service;
                //setup

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadProfiles();
        }
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


    }

    /**
     * 失去连接按钮点击
     *
     * @param view
     */
    public void disconnectClick(View view) {

    }
}