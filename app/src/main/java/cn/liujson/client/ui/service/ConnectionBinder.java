package cn.liujson.client.ui.service;

import android.os.Binder;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public abstract class ConnectionBinder extends Binder {

    /**
     * 配置
     */
    public abstract Single<IMQTT> setup(IMQTTBuilder builder);

    /**
     * 安全关闭
     */
    public abstract Completable closeSafety();

    /**
     * 强制关闭
     */
    public abstract Completable closeForcibly();

}
