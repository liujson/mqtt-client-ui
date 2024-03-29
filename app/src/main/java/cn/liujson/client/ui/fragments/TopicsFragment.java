package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.liujson.client.R;
import cn.liujson.client.databinding.FragmentTopicsBinding;
import cn.liujson.client.ui.adapter.TopicListAdapter;
import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.bean.entity.SubTopicItem;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.util.InputMethodUtils;
import cn.liujson.client.ui.viewmodel.TopicsViewModel;
import cn.liujson.client.ui.widget.OnSingleCheckedListener;
import cn.liujson.lib.mqtt.api.QoS;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static me.everything.android.ui.overscroll.OverScrollDecoratorHelper.ORIENTATION_VERTICAL;


/**
 * 主题订阅/取消订阅/查看订阅页面
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicsFragment extends BaseFragment implements TopicsViewModel.Navigator {

    FragmentTopicsBinding binding;

    TopicsViewModel viewModel;

    public TopicsFragment() {
        // Required empty public constructor
    }


    public static TopicsFragment newInstance() {
        TopicsFragment fragment = new TopicsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTopicsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.chipGroupTopicQos.setOnCheckedChangeListener(new OnSingleCheckedListener(binding.chipGroupTopicQos));
        binding.setVm(viewModel = new TopicsViewModel(getLifecycle()));
        viewModel.setNavigator(this);
    }

    @Override
    public void onLazyInitView(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        OverScrollDecoratorHelper.setUpOverScroll(binding.rvTopicsList,ORIENTATION_VERTICAL);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        notifyTopicsChanged();
        if (viewModel != null) {
            if (viewModel.getRepository().isBind()
                    && viewModel.getRepository().isInstalled()
                    && viewModel.getRepository().isConnected()) {
                viewModel.fieldAllEnable.set(true);
                viewModel.fieldMessageEnable.set(true);
            } else {
                viewModel.fieldAllEnable.set(false);
                viewModel.fieldMessageEnable.set(false);
            }
        }
    }

    private void notifyTopicsChanged() {
        if (viewModel != null) {
            if (viewModel.getRepository().isInstalled()) {
                final List<Pair<String, QoS>> pairList = viewModel.getRepository().getSubList();
                final List<SubTopicItem> subTopicItems = new ArrayList<>();
                for (Pair<String, QoS> sPair : pairList) {
                    subTopicItems.add(new SubTopicItem(sPair.first, sPair.second));
                }
                viewModel.updateDataList(subTopicItems);
            } else {
                viewModel.updateDataList(Collections.emptyList());
            }
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectChangeEvent(ConnectChangeEvent event) {
        if (event.isConnected) {
            viewModel.fieldAllEnable.set(true);
            viewModel.fieldMessageEnable.set(true);
        } else {
            viewModel.fieldAllEnable.set(false);
            viewModel.fieldMessageEnable.set(false);
        }
        notifyTopicsChanged();
    }

    @Override
    public boolean checkParam() {
        if (TextUtils.isEmpty(viewModel.fieldInputTopic.get())) {
            binding.etTopicInput.setError("can not be null");
            return false;
        }
        InputMethodUtils.hideSoftInput(getActivity());
        return true;
    }

    @Override
    public QoS readQos() {
        int checkedChipId = binding.chipGroupTopicQos.getCheckedChipId();
        if (checkedChipId == R.id.chip_qos0) {
            return QoS.AT_MOST_ONCE;
        } else if (checkedChipId == R.id.chip_qos1) {
            return QoS.AT_LEAST_ONCE;
        } else if (checkedChipId == R.id.chip_qos2) {
            return QoS.EXACTLY_ONCE;
        }
        return QoS.AT_MOST_ONCE;
    }

    Handler mHandler = new Handler();

    @Override
    public void onReceiveMessage(String topic, String message, QoS qoS) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        mHandler.post(() -> {
            viewModel.fieldMessageTopic.set(topic);
            viewModel.fieldMessageQoS.set(qoS.qoSName());
            viewModel.fieldMessageTime.set(dateFormat.format(new Date()));
        });
    }
}