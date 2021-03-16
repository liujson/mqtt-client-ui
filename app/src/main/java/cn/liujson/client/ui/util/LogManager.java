package cn.liujson.client.ui.util;

import android.content.Context;

import com.orhanobut.logger.FormatStrategy;

import java.util.List;

import cn.liujson.logger.LogUtils;
import cn.liujson.logger.disk.SdcardLogAdapter;
import cn.liujson.logger.logcat.LogcatLogAdapter;
import cn.liujson.logger.memory.MemoryLogAdapter;

/**
 * 日志管理
 *
 * @author liujson
 * @date 2021/3/16.
 */
public class LogManager {

    MemoryLogAdapter memoryLogAdapter;

    private LogManager() {

    }

    public static LogManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final LogManager INSTANCE = new LogManager();
    }

    public void init(Context context) {
        //原生Android 日志打印
        final LogcatLogAdapter logcatLogAdapter = new LogcatLogAdapter();
        //日志内存缓存
        memoryLogAdapter = new MemoryLogAdapter();
        //sdcard 日志存储
        final SdcardLogAdapter sdcardLogAdapter = new SdcardLogAdapter(context);
        LogUtils.initLogAdapters(logcatLogAdapter, memoryLogAdapter, sdcardLogAdapter);
    }

    public void lowMemory() {
        if (memoryLogAdapter != null) {
            //内存低时清除日志缓存
            memoryLogAdapter.clearCache();
        }
    }

    public void subscribeMemoryLog(FormatStrategy listener) {
        memoryLogAdapter.subscribeLogPrint(listener);
    }

    public void unsubscribeMemoryLog(FormatStrategy listener) {
        memoryLogAdapter.unsubscribeLogPrint(listener);
    }

    public List<String> memoryCacheLogList() {
        return memoryLogAdapter.cacheList();
    }
}
