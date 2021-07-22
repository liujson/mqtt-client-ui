package cn.liujson.client.ui.app;

import android.app.Application;

import com.ubains.lib.mqtt.mod.service.MqttMgr;

import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.greenrobot.eventbus.EventBus;

import cn.liujson.client.ui.bean.event.SystemLowMemoryEvent;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.util.LogManager;
import cn.liujson.client.ui.util.MqttProfileStoreImpl;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.service.paho.PahoLoggerImpl;
import cn.ubains.android.ublogger.LogUtils;
import xcrash.XCrash;


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
        //Xcrash 初始化
        XCrash.init(this);
        //初始化数据库
        DatabaseHelper.init(this);
        //日志打印库
        LogManager.getInstance().init(this);

        LogUtils.i("Start App");
        //Paho
        LoggerFactory.setLogger(PahoLoggerImpl.class.getName());

        //启动Mqtt服务
        MqttMgr.instance().setProfileStore(new MqttProfileStoreImpl());
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
        ToastHelper.showToast(this,"系统内存不足...");
        LogUtils.d("系统内存不足...");
        EventBus.getDefault().post(new SystemLowMemoryEvent());
    }
}
