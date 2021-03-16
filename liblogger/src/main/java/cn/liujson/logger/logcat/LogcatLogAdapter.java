package cn.liujson.logger.logcat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.LogcatLogStrategy;

/**
 * Android 原生形式的 Logcat 打印
 * @author liujson
 * @date 2021/3/16.
 */
public class LogcatLogAdapter implements LogAdapter {

    LogcatLogStrategy logStrategy;

    public LogcatLogAdapter() {
        this.logStrategy = new LogcatLogStrategy();
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return true;
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        this.logStrategy.log(priority, tag, message);
    }
}
