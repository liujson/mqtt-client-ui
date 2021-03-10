package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.liujson.client.databinding.FragmentTopicsBinding;
import cn.liujson.client.ui.viewmodel.PublishViewModel;
import cn.liujson.client.ui.viewmodel.TopicsViewModel;
import cn.liujson.client.ui.widget.OnSingleCheckedListener;

/**
 * 主题订阅/取消订阅/查看订阅页面
 * A simple {@link Fragment} subclass.
 * Use the {@link TopicsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicsFragment extends Fragment {

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

    }
}