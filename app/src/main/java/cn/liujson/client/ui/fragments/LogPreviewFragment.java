package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import cn.liujson.client.databinding.FragmentLogPreviewBinding;

/**
 * 日志查看 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link LogPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogPreviewFragment extends Fragment {

    FragmentLogPreviewBinding binding;
    public LogPreviewFragment() {
        // Required empty public constructor
    }


    public static LogPreviewFragment newInstance() {
        LogPreviewFragment fragment = new LogPreviewFragment();

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
        binding = FragmentLogPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}