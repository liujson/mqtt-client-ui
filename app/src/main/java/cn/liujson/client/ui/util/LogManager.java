package cn.liujson.client.ui.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.orhanobut.logger.LogAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.liujson.client.ui.bean.event.PrintOneLogEvent;

import cn.ubains.android.ublogger.LogRecord;
import cn.ubains.android.ublogger.LogUtils;

/**
 * 日志管理
 *
 * @author liujson
 * @date 2021/3/16.
 */
public class LogManager implements LogAdapter {


    private LogManager() {

    }

    public static LogManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return true;
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        EventBus.getDefault().post(new PrintOneLogEvent(priority, tag, message));
    }

    private static final class Holder {
        private static final LogManager INSTANCE = new LogManager();
    }

    public void init(Context context) {
        LogUtils.init(context)
                //原生Android 日志打印
                .enableLogcat()
                //日志内存缓存
                .enableCacheRecord()
                //sdcard 日志存储
                .enableDiskRecord()
                //实时日志中转Adapter
                .addAdapter(this)
                //本地日志保存七天
                .maxSaveDays(7)
                //启用配置
                .configure();
    }

    public void lowMemory() {
        //内存低时清除日志缓存
        LogUtils.getCacheQueuePerformer().clear();
    }


    public List<LogRecord> memoryCacheLogList() {
        return LogUtils.getCacheQueuePerformer().logRecordCacheList();
    }
}
