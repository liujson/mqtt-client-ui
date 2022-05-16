package com.ubains.lib.mqtt.mod.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ubains.android.ubutil.comm.ToastUtils;
import com.ubains.lib.mqtt.mod.databinding.FragmentMqttWorkingStatusBinding;
import com.ubains.lib.mqtt.mod.ui.vm.MqttWorkingStatusViewModel;

import java.util.concurrent.TimeUnit;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * 工作状态 Fragment
 *
 * @author liujson
 */
public class MqttWorkingStatusFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private MqttWorkingStatusViewModel viewModel;

    private Disposable refreshingDisposable;

    FragmentMqttWorkingStatusBinding binding;

    public static MqttWorkingStatusFragment newInstance() {
        return new MqttWorkingStatusFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMqttWorkingStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this,new ViewModelProvider.NewInstanceFactory()).get(MqttWorkingStatusViewModel.class);
        binding.setVm(viewModel);
        binding.swipe.setOnRefreshListener(this);
        final Observer<MqttWorkingStatusViewModel.WorkingStatus> networkConnectedObserver = status -> {
            MqttWorkingStatusViewModel.bindWorkingStatus(binding.ivNetworkConnectedStatus, status);
        };
        viewModel.getFieldNetworkConnectedStatus().observe(getViewLifecycleOwner(), networkConnectedObserver);
        final Observer<MqttWorkingStatusViewModel.WorkingStatus> serviceBindObserver = status -> {
            MqttWorkingStatusViewModel.bindWorkingStatus(binding.ivServiceBindStatus, status);
        };
        viewModel.getFieldServiceBindStatus().observe(getViewLifecycleOwner(), serviceBindObserver);
        final Observer<MqttWorkingStatusViewModel.WorkingStatus> installObserver = status -> {
            MqttWorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientInstallStatus, status);
        };
        viewModel.getFieldClientInstalledStatus().observe(getViewLifecycleOwner(), installObserver);
        final Observer<MqttWorkingStatusViewModel.WorkingStatus> connectedObserver = status -> {
            MqttWorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientConnectedStatus, status);
        };
        viewModel.getFieldClientConnectedStatus().observe(getViewLifecycleOwner(), connectedObserver);
        final Observer<MqttWorkingStatusViewModel.WorkingStatus> closedObserver = status -> {
            MqttWorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientClosedStatus, status);
        };
        viewModel.getFieldClientClosedStatus().observe(getViewLifecycleOwner(), closedObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshStatus();
        }
    }

    @Override
    public void onRefresh() {
        ToastUtils.showToast("Refreshing...");
        refreshingDisposable = Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    refreshingDisposable = null;
                    binding.swipe.setRefreshing(false);
                    if (viewModel != null) {
                        viewModel.refreshStatus();
                    }
                }, throwable -> {
                    refreshingDisposable = null;
                    ToastUtils.showToast("Refresh failure.");
                    binding.swipe.setRefreshing(false);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (refreshingDisposable != null) {
            refreshingDisposable.dispose();
        }
    }
}