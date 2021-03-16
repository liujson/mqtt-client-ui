package cn.liujson.logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.Logger;


/**
 * 日志记录框架
 * 基于 https://github.com/orhanobut/logger 日志框架封装
 * （此类描述可能出现的日志打印格式）
 *
 * @author liujson
 * @date 2021/3/15.
 */
public class LogUtils {

    public static void initLogAdapters(LogAdapter... logAdapters) {
        for (LogAdapter logAdapter : logAdapters) {
            Logger.addLogAdapter(logAdapter);
        }
    }

    public static void clearLogAdapters() {
        Logger.clearLogAdapters();
    }

    //region with tag
    public static void d(String tag, String message) {
        Logger.t(tag).d(message, (Object[]) null);
    }

    public static void e(String tag, String message) {
        Logger.t(tag).e(message, (Object[]) null);
    }

    public static void w(String tag, String message) {
        Logger.t(tag).w(message, (Object[]) null);
    }

    public static void i(String tag, String message) {
        Logger.t(tag).i(message, (Object[]) null);
    }

    public static void v(String tag, String message) {
        Logger.t(tag).v(message, (Object[]) null);
    }

    public static void wtf(String tag, String message) {
        Logger.t(tag).wtf(message, (Object[]) null);
    }

    public static void xml(String tag, String xml) {
        Logger.t(tag).xml(xml);
    }

    public static void json(String tag, String json) {
        Logger.t(tag).json(json);
    }
    //endregion with tag

    //region no tag
    public static void d(String message) {
        Logger.d(message, (Object[]) null);
    }

    public static void e(String message) {
        Logger.e(message, (Object[]) null);
    }

    public static void w(String message) {
        Logger.w(message, (Object[]) null);
    }

    public static void i(String message) {
        Logger.i(message, (Object[]) null);
    }

    public static void v(String message) {
        Logger.v(message, (Object[]) null);
    }

    public static void wtf(String message) {
        Logger.wtf(message, (Object[]) null);
    }

    public static void xml(String xml) {
        Logger.xml(xml);
    }

    public static void json(String json) {
        Logger.json(json);
    }
    //endregion no tag

    //region other

    public static void d(Object object) {
        Logger.d(object);
    }

    public static void e(@Nullable Throwable throwable, @NonNull String message) {
        Logger.e(throwable, message);
    }

    public static void log(int priority, @Nullable String tag,
                           @Nullable String message, @Nullable Throwable throwable) {
        Logger.log(priority, tag, message, throwable);
    }

    //endregion other


    public enum Level {

        VERBOSE(Logger.VERBOSE), DEBUG(Logger.DEBUG), INFO(Logger.INFO),
        WARN(Logger.WARN), ERROR(Logger.ERROR), ASSERT(Logger.ASSERT),
        UNKNOWN(-1);

        private final int level;

        Level(int level) {
            this.level = level;
        }

        public String logLevel() {
            return name();
        }

        public int levelValue() {
            return this.level;
        }

        public static Level value2Level(int level) {
            switch (level) {
                case Logger.VERBOSE:
                    return VERBOSE;
                case Logger.DEBUG:
                    return DEBUG;
                case Logger.INFO:
                    return INFO;
                case Logger.WARN:
                    return WARN;
                case Logger.ERROR:
                    return ERROR;
                case Logger.ASSERT:
                    return ASSERT;
                default:
                    return UNKNOWN;
            }
        }

        public static String logLevelName(int level) {
            return value2Level(level).name();
        }
    }
}
