package cn.liujson.logger.memory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.liujson.logger.LogRecord;

/**
 * 内存缓存日志 Adapter
 * 日志会被记录到内存中暂时存储
 *
 * @author liujson
 * @date 2021/3/15.
 */
public class MemoryLogAdapter implements LogAdapter {

    @NonNull
    private final MemoryFormatStrategy formatStrategy;

    private List<FormatStrategy> logPrintListener = new ArrayList<>();

    public MemoryLogAdapter() {
        formatStrategy = new MemoryFormatStrategy();
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return true;
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        formatStrategy.log(priority, tag, message);
        final Iterator<FormatStrategy> it = logPrintListener.iterator();
        while (it.hasNext()) {
            it.next().log(priority, tag, message);
        }
    }

    public void clearCache() {
        formatStrategy.clearCache();
    }

    public void quitSafely() {
        formatStrategy.quitSafely();
    }

    public LinkedList<LogRecord> cacheList() {
        return formatStrategy.cacheList();
    }

    /**
     * 添加日志打印监听者
     */
    public void subscribeLogPrint(FormatStrategy logPrintListener) {
        this.logPrintListener.add(logPrintListener);
    }

    /**
     * 客户端传入的FormatStrategy中千万不要再使用LogUtil.x打印日志否则停不下来
     * @param logPrintListener
     */
    public void unsubscribeLogPrint(FormatStrategy logPrintListener) {
        this.logPrintListener.remove(logPrintListener);
    }
}
