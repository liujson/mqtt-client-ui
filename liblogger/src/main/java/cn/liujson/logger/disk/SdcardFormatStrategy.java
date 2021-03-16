package cn.liujson.logger.disk;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogStrategy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cn.liujson.logger.LogUtils;


/**
 * Sdcard 日志存储格式
 *
 * @author liujson
 * @date 2021/3/16.
 */
public class SdcardFormatStrategy implements FormatStrategy {

    private static final int MAX_BYTES = 500 * 1024; // 500K averages to a 4000 lines per file

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String NEW_LINE_REPLACEMENT = " <br> ";
    private static final String SEPARATOR = ",";
    private static final String CN_SEPARATOR = "，";

    private final Date date;
    private final SimpleDateFormat dateFormat;
    private final String tag;

    private LogStrategy logStrategy;

    public SdcardFormatStrategy(Builder builder) {
        HandlerThread ht = new HandlerThread("SdcardLogger." + builder.folder);
        ht.start();
        Handler handler = new SdcardLogStrategy.WriteHandler(ht.getLooper(), builder.folder, MAX_BYTES);
        logStrategy = new SdcardLogStrategy(handler);
        date = builder.date;
        dateFormat = builder.dateFormat;
        tag = builder.tag;
    }

    @Override
    public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
        Objects.requireNonNull((message));

        String tag = formatTag(onceOnlyTag);

        date.setTime(System.currentTimeMillis());

        StringBuilder builder = new StringBuilder();

        // machine-readable date/time
        builder.append(date.getTime());

        // human-readable date/time
        builder.append(SEPARATOR);
        builder.append(dateFormat.format(date));

        // level
        builder.append(SEPARATOR);
        builder.append(LogUtils.Level.logLevelName(priority));

        // tag
        builder.append(SEPARATOR);
        builder.append(tag);

        // message
        if (message.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            message = message.replaceAll(NEW_LINE, NEW_LINE_REPLACEMENT);
        }
        //英文逗号换成中文逗号
        if (message.contains(SEPARATOR)) {
            message = message.replaceAll(SEPARATOR, CN_SEPARATOR);
        }
        builder.append(SEPARATOR);
        builder.append(message);

        // new line
        builder.append(NEW_LINE);

        logStrategy.log(priority, tag, builder.toString());
    }

    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    @Nullable
    private String formatTag(@Nullable String tag) {
        if (!TextUtils.isEmpty(tag) && !Objects.equals(this.tag, tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    public static class Builder {

        Date date;
        SimpleDateFormat dateFormat;
        String tag = "NO_TAG";
        String folder;
        final Context context;

        private Builder(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        public SdcardFormatStrategy.Builder date(@Nullable Date val) {
            date = val;
            return this;
        }

        public SdcardFormatStrategy.Builder dateFormat(@Nullable SimpleDateFormat val) {
            dateFormat = val;
            return this;
        }

        public SdcardFormatStrategy.Builder tag(@Nullable String tag) {
            this.tag = tag;
            return this;
        }

        public SdcardFormatStrategy.Builder folder(@Nullable String folder) {
            this.folder = folder;
            return this;
        }

        public SdcardFormatStrategy build() {
            if (date == null) {
                date = new Date();
            }
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK);
            }
            if (folder == null) {
                //使用默认地址
                File externalFile = context.getExternalFilesDir("logger");
                folder = externalFile.getAbsolutePath();
            }
            return new SdcardFormatStrategy(this);
        }
    }
}
