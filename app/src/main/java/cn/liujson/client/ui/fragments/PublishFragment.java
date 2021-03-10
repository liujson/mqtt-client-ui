package cn.liujson.client.ui.fragments;


import android.content.Context;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.liujson.client.databinding.FragmentPublishBinding;

import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.client.ui.widget.OnSingleCheckedListener;

/**
 * 发布消息 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link PublishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublishFragment extends Fragment implements ConnectionServiceRepository.OnBindStatus{

    FragmentPublishBinding binding;


    ConnectionServiceRepository repository;

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
        // Inflate the layout for this fragment
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.chipGroupTopicQos.setOnCheckedChangeListener(new OnSingleCheckedListener(binding.chipGroupTopicQos));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        repository = new ConnectionServiceRepository(this);
        repository.bindConnectionService(getContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        repository.unbindConnectionService();
    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {
        //绑定成功
    }

    @Override
    public void onBindFailure() {
        //绑定失败
    }
}