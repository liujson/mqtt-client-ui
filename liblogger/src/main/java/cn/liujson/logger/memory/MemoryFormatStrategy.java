package cn.liujson.logger.memory;

import android.os.Handler;
import android.os.HandlerThread;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.FormatStrategy;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import cn.liujson.logger.LogUtils;

/**
 * 格式化策略
 *
 * @author liujson
 * @date 2021/3/15.
 */
public class MemoryFormatStrategy implements FormatStrategy {

    static final String DEFAULT_TAG = "NO_TAG";

    private static final String SEPARATOR = ",";

    private final HandlerThread ht;

    private final Handler handler;

    private final MemoryLogStrategy logStrategy;

    private final SimpleDateFormat dateFormat;

    private final Date date;

    public MemoryFormatStrategy() {
        ht = new HandlerThread("MemoryCacheLog");
        ht.start();
        handler = new MemoryLogStrategy.MemoryLogHandler(ht.getLooper());
        logStrategy = new MemoryLogStrategy(handler);

        //日期格式化再多线程情况下会有问题
        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK);
        date = new Date();
    }

    /**
     * 清除所有缓存
     */
    public void clearCache() {
        handler.sendMessage(handler.obtainMessage(MemoryLogStrategy.MSG_CLEAR));
    }

    /**
     * 结束缓存任务线程,安全退出
     */
    public void quitSafely() {
        ht.quitSafely();
    }

    public int logSize() {
        return logStrategy.logSize();
    }

    public LinkedList<String> cacheList() {
        return logStrategy.cacheList();
    }


    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {

        date.setTime(System.currentTimeMillis());

        StringBuilder builder = new StringBuilder();

        // machine-readable date/time
        builder.append(Long.toString(date.getTime()));

        // human-readable date/time
        builder.append(SEPARATOR);
        builder.append(dateFormat.format(date));

        // level
        builder.append(SEPARATOR);
        builder.append(LogUtils.Level.logLevelName(priority));

        // tag
        if (tag == null) {
            tag = DEFAULT_TAG;
        }
        builder.append(SEPARATOR);
        builder.append(tag);
        logStrategy.log(priority, tag, message);

        // message
        builder.append(SEPARATOR);
        builder.append(message);

        logStrategy.log(priority, tag, builder.toString());
    }


}
