package cn.liujson.client.ui.fragments;


import android.content.Context;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.liujson.client.R;
import cn.liujson.client.databinding.FragmentPublishBinding;

import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.viewmodel.PublishViewModel;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.client.ui.widget.OnSingleCheckedListener;
import cn.liujson.lib.mqtt.api.QoS;

/**
 * 发布消息 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link PublishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublishFragment extends BaseFragment implements PublishViewModel.Navigator {

    private static final String TAG = "PublishFragment";

    FragmentPublishBinding binding;


    PublishViewModel viewModel;


    public PublishFragment() {
        // Required empty public constructor
    }


    public static PublishFragment newInstance() {
        PublishFragment fragment = new PublishFragment();
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
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.chipGroupTopicQos.setOnCheckedChangeListener(new OnSingleCheckedListener(binding.chipGroupTopicQos));
        binding.setVm(viewModel = new PublishViewModel(getLifecycle()));
        viewModel.setNavigator(this);
        viewModel.getRepository().bindConnectionService(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getRepository().unbindConnectionService();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectChangeEvent(ConnectChangeEvent event) {
        if (event.isConnected) {
            viewModel.fieldAllEnable.set(true);
        } else {
            viewModel.fieldAllEnable.set(false);
        }
        Log.d(TAG, "onConnectChangeEvent is connected:" + event.isConnected);
    }

    @Override
    public boolean checkPublishParam() {
        if (TextUtils.isEmpty(viewModel.fieldInputTopic.get())) {
            binding.etTopicInput.setError("can not be blank");
            return false;
        }

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

    @Override
    public boolean isRetained() {
        return binding.cbRetained.isChecked();
    }


}