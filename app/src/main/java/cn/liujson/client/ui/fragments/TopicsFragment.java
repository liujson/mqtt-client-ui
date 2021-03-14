package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.liujson.client.databinding.FragmentTopicsBinding;
import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
import cn.liujson.client.ui.viewmodel.PublishViewModel;
import cn.liujson.client.ui.viewmodel.TopicsViewModel;
import cn.liujson.client.ui.widget.OnSingleCheckedListener;

/**
 * 主题订阅/取消订阅/查看订阅页面
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicsFragment extends BaseFragment {

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
    }
}