package com.ubains.lib.mqtt.mod.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ubains.android.ubutil.comm.ToastUtils;
import com.ubains.lib.mqtt.mod.databinding.FragmentMqttSubscribedTopicsBinding;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSubscribedTopicsViewModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 已经订阅的主题列表
 *
 * @author liujson
 * @date 2022/10/21.
 */
public class MqttSubscribedTopicsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FragmentMqttSubscribedTopicsBinding binding;
    private Disposable refreshingDisposable;
    private ArrayAdapter<String> mArrayAdapter;

    public static MqttSubscribedTopicsFragment newInstance() {
        return new MqttSubscribedTopicsFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMqttSubscribedTopicsBinding.inflate(inflater, container, false);
        binding.setVm(new MqttSubscribedTopicsViewModel());
        binding.swipe.setOnRefreshListener(this);
        binding.listTopics.setAdapter(mArrayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                binding.getVm().dataList));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.getVm().refreshData(mArrayAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding.getVm() != null && mArrayAdapter != null) {
            binding.getVm().refreshData(mArrayAdapter);
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
                    if (binding.getVm() != null && mArrayAdapter != null) {
                        binding.getVm().refreshData(mArrayAdapter);
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
