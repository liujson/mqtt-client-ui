package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.liujson.client.databinding.FragmentPublishBinding;

/**
 * 发布消息 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link PublishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublishFragment extends Fragment {

     FragmentPublishBinding binding;

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


}