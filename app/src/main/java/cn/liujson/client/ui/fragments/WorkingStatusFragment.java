package cn.liujson.client.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import cn.liujson.client.databinding.FragmentWorkingStatusBinding;
import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.WorkingStatusViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * 工作状态 Fragment
 */
public class WorkingStatusFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private WorkingStatusViewModel viewModel;
    private FragmentWorkingStatusBinding binding;

    private Disposable refreshingDisposable;

    public static WorkingStatusFragment newInstance() {
        return new WorkingStatusFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkingStatusBinding.inflate(inflater, container, false);
        binding.swipe.setOnRefreshListener(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WorkingStatusViewModel.class);
        binding.setVm(viewModel);
        final Observer<WorkingStatusViewModel.WorkingStatus> networkConnectedObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivNetworkConnectedStatus, status);
        };
        viewModel.getFieldNetworkConnectedStatus().observe(this, networkConnectedObserver);
        final Observer<WorkingStatusViewModel.WorkingStatus> serviceBindObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivServiceBindStatus, status);
        };
        viewModel.getFieldServiceBindStatus().observe(this, serviceBindObserver);
        final Observer<WorkingStatusViewModel.WorkingStatus> installObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientInstallStatus, status);
        };
        viewModel.getFieldClientInstalledStatus().observe(this, installObserver);
        final Observer<WorkingStatusViewModel.WorkingStatus> connectedObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientConnectedStatus, status);
        };
        viewModel.getFieldClientConnectedStatus().observe(this, connectedObserver);
        final Observer<WorkingStatusViewModel.WorkingStatus> closedObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivServiceClientClosedStatus, status);
        };
        viewModel.getFieldClientClosedStatus().observe(this, closedObserver);
        final Observer<WorkingStatusViewModel.WorkingStatus> checkPingObserver = status -> {
            WorkingStatusViewModel.bindWorkingStatus(binding.ivServiceCheckPingStatus, status);
        };
        viewModel.getFieldCheckPingStatus().observe(this, checkPingObserver);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectChangeEvent(ConnectChangeEvent event) {
        if (viewModel != null) {
            viewModel.refreshStatus();
        }
    }

    @Override
    public void onRefresh() {
        ToastHelper.showToast(this.getContext(), "Refreshing...");
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
                    ToastHelper.showToast(this.getContext(), "Refresh failure.");
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