package cn.liujson.client.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.databinding.FragmentLogPreviewBinding;
import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.bean.event.PrintOneLogEvent;
import cn.liujson.client.ui.util.LogManager;
import cn.liujson.client.ui.viewmodel.LogPreviewViewModel;
import cn.liujson.client.ui.widget.LogsPreviewView;

import cn.ubains.android.ublogger.LogRecord;
import cn.ubains.android.ublogger.LogUtils;

/**
 * 日志查看 Fragment
 * A simple {@link Fragment} subclass.
 * Use the {@link LogPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogPreviewFragment extends BaseFragment {

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
        loadCacheLogs();
    }

    private void loadCacheLogs() {
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

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrintOneLogEvent(PrintOneLogEvent event) {
        logsPreview.log(priority2Level(event.priority), event.tag, event.message);
    }

    private LogsPreviewView.Level priority2Level(int priority) {
        switch (LogUtils.Level.value2Level(priority)) {
            case VERBOSE:
                return LogsPreviewView.Level.V;
            case DEBUG:
                return LogsPreviewView.Level.D;
            case WARN:
                return LogsPreviewView.Level.W;
            case ERROR:
            case ASSERT:
                return LogsPreviewView.Level.E;
            case INFO:
            default:
                return LogsPreviewView.Level.I;
        }
    }
}