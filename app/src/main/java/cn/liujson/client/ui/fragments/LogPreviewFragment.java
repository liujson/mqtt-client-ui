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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.liujson.client.databinding.FragmentLogPreviewBinding;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.util.LogManager;
import cn.liujson.client.ui.viewmodel.LogPreviewViewModel;
import cn.liujson.client.ui.widget.LogsPreviewView;
import cn.liujson.logger.LogRecord;
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

    final Handler handler = new Handler();

    LogsPreviewView logsPreview;

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
        logsPreview = binding.logsPreview;
        loadMemoryLogs();
        LogManager.getInstance().subscribeMemoryLog((priority, tag, message) -> {
            //这里千万不要再使用LogUtils打印日志，否则会无限循环
            handler.post(() -> {
                logsPreview.log(priority2Level(priority), tag, message);
            });
        });
    }

    private void loadMemoryLogs() {
        final List<LogRecord> logRecords = LogManager.getInstance().memoryCacheLogList();
        final int logQueueSize = logsPreview.getLogQueueSize();
        List<LogsPreviewView.LogRecord> logRecordList = new ArrayList<>();
        int size = Math.min(logRecords.size(), logQueueSize);
        for (int i = 0; i < size; i++) {
            LogRecord logRecord = logRecords.get(i);
            final LogsPreviewView.Level level = priority2Level(logRecord.getPriority());
            LogsPreviewView.LogRecord record = new LogsPreviewView.LogRecord(level, logRecord.getFormatMessage());
            logRecordList.add(record);
        }

        logsPreview.addLogs(logRecordList);
    }

    private LogsPreviewView.Level priority2Level(int priority) {
        switch (LogUtils.Level.value2Level(priority)) {
            case INFO:
            case DEBUG:
            case WARN:
                return LogsPreviewView.Level.D;
            case ERROR:
            case ASSERT:
                return LogsPreviewView.Level.E;
            default:
                return LogsPreviewView.Level.I;
        }
    }
}