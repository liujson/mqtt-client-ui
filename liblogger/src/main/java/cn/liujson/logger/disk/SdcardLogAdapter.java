package cn.liujson.logger.disk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.orhanobut.logger.LogAdapter;


/**
 * 扩展的日志Sdcard存储
 * 默认日志会存储在{/sdcard/{packageName}/files/logger/}目录
 * @author liujson
 * @date 2021/3/16.
 */
public class SdcardLogAdapter implements LogAdapter {

    private final SdcardFormatStrategy logStrategy;

    public SdcardLogAdapter(Context context) {
        this.logStrategy = SdcardFormatStrategy.newBuilder(context).build();
    }

    public SdcardLogAdapter(SdcardFormatStrategy logStrategy) {
        this.logStrategy = logStrategy;
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return true;
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        logStrategy.log(priority, tag, message);
    }
}
