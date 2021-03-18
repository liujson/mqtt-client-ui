package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.orhanobut.logger.FormatStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.liujson.client.databinding.FragmentLogPreviewBinding;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.util.LogManager;
import cn.liujson.client.ui.viewmodel.LogPreviewViewModel;
import cn.liujson.logger.LogUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 日志查看 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link LogPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogPreviewFragment extends Fragment {

    FragmentLogPreviewBinding binding;
    LogPreviewViewModel viewModel;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setVm(viewModel = new LogPreviewViewModel(getLifecycle()));
        final Handler handler = new Handler();
        LogManager.getInstance().subscribeMemoryLog(new FormatStrategy() {
            @Override
            public void log(int priority, @Nullable String tag, @NonNull String message) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(dateFormat.format(new Date()));
                stringBuilder.append('\t');
                stringBuilder.append(LogUtils.Level.logLevelName(priority));
                stringBuilder.append(" --- ");
                stringBuilder.append(tag);
                stringBuilder.append(" :");
                stringBuilder.append(message);
                //这里千万不要再使用LogUtils打印日志，否则会无限循环
                handler.post(() -> {
                    refresh(stringBuilder.toString());
                });
            }
        });
    }


    public void refresh(String result) {
        binding.tvLog.append(result + "\n\n");
        //let text view to move to the last line.
        int offset = binding.tvLog.getLineCount() * binding.tvLog.getLineHeight();
        if (offset > binding.tvLog.getHeight()) {
            binding.tvLog.scrollTo(0, offset - binding.tvLog.getHeight());
        }
    }

}