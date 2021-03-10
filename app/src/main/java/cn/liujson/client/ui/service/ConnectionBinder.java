package cn.liujson.client.ui.service;

import android.os.Binder;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public abstract class ConnectionBinder<C> extends Binder {

    /**
     * 配置Client成服务运行，服务运行会使其生命周期变长，请小心内存泄露
     * setup 后消息接收监听器会被清除，请重新调用设置监听器 registerMessageReceiver
     *
     * @return
     */
    public abstract IMQTTWrapper<C> setup(IMQTTWrapper<C> imqttWrapper);

    /**
     * 是否已经配置
     */
    public abstract boolean isSetup();


    /**
     * 目标与已安装的是同一个（参数一致）
     *
     * @return
     */
    public abstract boolean isSame(IMQTTBuilder builder);

    /**
     * 注册消息接收监听器
     */
    public abstract void registerMessageReceiver(IMQTTMessageReceiver messageReceiver);

    /**
     * 取消注册消息接收监听器
     */
    public abstract void unregisterMessageReceiver(IMQTTMessageReceiver messageReceiver);

    /**
     * 获取 ClientWrapper
     */
    public abstract IMQTTWrapper<C> getWrapper();

    //region Rx 方法

    /**
     * 安全关闭
     */
    public abstract Completable closeSafety();

    /**
     * 强制关闭
     */
    public abstract Completable closeForcibly();

    //region Rx 方法
}
