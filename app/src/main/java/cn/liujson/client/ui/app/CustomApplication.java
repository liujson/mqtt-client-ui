package cn.liujson.client.ui.app;

import android.app.Application;

import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.client.ui.util.LogManager;
import cn.liujson.lib.mqtt.service.paho.PahoLoggerImpl;
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


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化数据库
        DatabaseHelper.init(this);
        //日志打印库
        LogManager.getInstance().init(this);

        LogUtils.i("Start App");
        //Paho
        LoggerFactory.setLogger(PahoLoggerImpl.class.getName());

        //启动Mqtt服务
        MqttMgr.instance().init(this);
    }

    public static CustomApplication getApp() {
        return instance;
    }


    @Override
    public void onLowMemory() {
        LogUtils.i("onLowMemory");
        super.onLowMemory();
        LogManager.getInstance().lowMemory();
    }
}
