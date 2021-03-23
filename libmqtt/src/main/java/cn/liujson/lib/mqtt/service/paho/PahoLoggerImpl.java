package cn.liujson.lib.mqtt.service.paho;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.logging.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author liujson
 * @date 2021/3/19.
 */
public class PahoLoggerImpl implements Logger {

    private static final String TAG = "PahoLoggerImpl";

    @Override
    public void initialise(ResourceBundle messageCatalog, String loggerID, String resourceName) {

    }

    @Override
    public void setResourceName(String logContext) {

    }

    @Override
    public boolean isLoggable(int level) {
        //打印所有
        return true;
    }

    @Override
    public void severe(String sourceClass, String sourceMethod, String msg) {
        Log.wtf(TAG, "severe--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.wtf(TAG, "severe--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Log.wtf(TAG, "severe--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(thrown));
    }

    @Override
    public void warning(String sourceClass, String sourceMethod, String msg) {
        Log.w(TAG, "warning--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.w(TAG, "warning--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Log.w(TAG, "warning--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(thrown));
    }

    @Override
    public void info(String sourceClass, String sourceMethod, String msg) {
        Log.i(TAG, "info--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.i(TAG, "info--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Log.i(TAG, "info--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(thrown));
    }

    @Override
    public void config(String sourceClass, String sourceMethod, String msg) {
        Log.e(TAG, "config--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.e(TAG, "config--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Log.e(TAG, "config--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(thrown));
    }

    @Override
    public void fine(String sourceClass, String sourceMethod, String msg) {
        Log.d(TAG, "fine--" + sourceClass + "--" + sourceMethod + "--" + msg);
        // 追踪客户端关闭
        if (Objects.equals("close", sourceMethod)) {

        }
        // 追踪连接池关闭
        if(Objects.equals("shutdownConnection",sourceMethod)){

        }
    }

    @Override
    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.d(TAG, "fine--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Log.d(TAG, "fine--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(ex));
    }

    @Override
    public void finer(String sourceClass, String sourceMethod, String msg) {
        Log.d(TAG, "finer--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.d(TAG, "finer--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Log.d(TAG, "finer--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(ex));
    }

    @Override
    public void finest(String sourceClass, String sourceMethod, String msg) {
        Log.d(TAG, "finest--" + sourceClass + "--" + sourceMethod + "--" + msg);
    }

    @Override
    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        Log.d(TAG, "finest--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts));
    }

    @Override
    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Log.d(TAG, "finest--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(ex));
    }

    @Override
    public void log(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Log.v(TAG, "log--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(thrown));
    }

    @Override
    public void trace(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Log.v(TAG, "trace--" + sourceClass + "--" + sourceMethod + "--" + msg + "--" + Arrays.toString(inserts) + "--" + throw2String(ex));
    }

    @Override
    public String formatMessage(String msg, Object[] inserts) {
        return msg + "--" + Arrays.toString(inserts);
    }

    @Override
    public void dumpTrace() {

    }


    private String throw2String(Throwable throwable) {
        return throwable != null ? throwable.toString() : "\t";
    }
}

