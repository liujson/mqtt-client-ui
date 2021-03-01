package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityPreviewMainBinding;
import cn.liujson.client.ui.adapter.PageFragmentStateAdapter;
import cn.liujson.client.ui.fragments.LogPreviewFragment;
import cn.liujson.client.ui.fragments.PublishFragment;
import cn.liujson.client.ui.fragments.TopicsFragment;


public class PreviewMainActivity extends AppCompatActivity {

    public static final String[] mTitleList = new String[]{"Publish", "Topics", "Log"};

    ActivityPreviewMainBinding viewDataBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview_main);

        initViewPager();
        initMagicIndicator();

        viewDataBinding.btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfigListActivity.class);

            startActivity(intent);
        });
    }

    private void initViewPager() {
        PublishFragment publishFragment = PublishFragment.newInstance();
        TopicsFragment topicsFragment = TopicsFragment.newInstance();
        LogPreviewFragment logPreviewFragment = LogPreviewFragment.newInstance();

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(publishFragment);
        fragmentList.add(topicsFragment);
        fragmentList.add(logPreviewFragment);

        viewDataBinding.mViewPager.setAdapter(new PageFragmentStateAdapter(getSupportFragmentManager(),getLifecycle(),fragmentList));
    }


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
}