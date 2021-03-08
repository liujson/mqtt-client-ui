package cn.liujson.client.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.liujson.client.R;
import cn.liujson.client.ui.viewmodel.WorkingStatusViewModel;

/**
 * 工作状态 Fragment
 */
public class WorkingStatusFragment extends Fragment {

    private WorkingStatusViewModel mViewModel;

    public static WorkingStatusFragment newInstance() {
        return new WorkingStatusFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_working_status, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WorkingStatusViewModel.class);

    }

}