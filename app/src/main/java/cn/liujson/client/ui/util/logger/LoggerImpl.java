package cn.liujson.client.ui.util.logger;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import static com.orhanobut.logger.Logger.DEBUG;
import static com.orhanobut.logger.Logger.ERROR;
import static com.orhanobut.logger.Logger.INFO;
import static com.orhanobut.logger.Logger.VERBOSE;
import static com.orhanobut.logger.Logger.WARN;

/**
 * @author liujson
 * @date 2021/3/14.
 */
public class LoggerImpl implements ILoggers {

    private final String mTag;

    static {
        //初始化
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    public LoggerImpl(String tag) {
        this.mTag = tag;
    }

    public LoggerImpl() {
        this.mTag = null;
    }


    @Override
    public void d(String tag, String message) {
        Logger.log(DEBUG, tag, message, null);
    }

    @Override
    public void e(String tag, String message) {
        Logger.log(ERROR, tag, message, null);
    }

    @Override
    public void w(String tag, String message) {
        Logger.log(WARN, tag, message, null);
    }

    @Override
    public void v(String tag, String message) {
        Logger.log(VERBOSE, tag, message, null);
    }

    @Override
    public void i(String tag, String message) {
        Logger.log(INFO, tag, message, null);
    }

    @Override
    public void d(String message) {
        if (mTag != null) {
            d(mTag, message);
        } else {
            Logger.d(message);
        }
    }

    @Override
    public void e(String message) {
        if (mTag != null) {
            e(mTag, message);
        } else {
            Logger.e(message);
        }
    }

    @Override
    public void w(String message) {
        if (mTag != null) {
            w(mTag, message);
        } else {
            Logger.w(message);
        }
    }

    @Override
    public void v(String message) {
        if (mTag != null) {
            v(mTag, message);
        } else {
            Logger.v(message);
        }
    }

    @Override
    public void i(String message) {
        if (mTag != null) {
            i(mTag, message);
        } else {
            Logger.i(message);
        }
    }
}
