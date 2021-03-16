package cn.liujson.client.ui.app;

import android.app.Application;

import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.logger.LogUtils;
import cn.liujson.logger.disk.SdcardLogAdapter;
import cn.liujson.logger.logcat.LogcatLogAdapter;
import cn.liujson.logger.memory.MemoryLogAdapter;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class CustomApplication extends Application {

    static CustomApplication instance;
    MemoryLogAdapter memoryLogAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化数据库
        DatabaseHelper.init(this);
        //日志打印库
        initLogger();


        //启动Mqtt服务
//        MqttServiceManager.getInstance().bindToApplication(this);
    }

    /**
     * 初始化日志打印
     */
    private void initLogger() {
        //原生Android 日志打印
        final LogcatLogAdapter logcatLogAdapter = new LogcatLogAdapter();
        //日志内存缓存
        memoryLogAdapter =new MemoryLogAdapter();
        //sdcard 日志存储
        final SdcardLogAdapter sdcardLogAdapter = new SdcardLogAdapter(this);
        LogUtils.initLogAdapters(logcatLogAdapter, memoryLogAdapter, sdcardLogAdapter);
    }

    public static CustomApplication getApp() {
        return instance;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (memoryLogAdapter != null) {
            //内存低时清除日志缓存
            memoryLogAdapter.clearCache();
        }
    }
}
