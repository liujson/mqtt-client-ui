package cn.liujson.lib.mqtt.api.backruning;

import android.os.Binder;

import androidx.annotation.NonNull;

/**
 * @author liujson
 * @date 2021/3/18.
 */
public abstract class AbstractConnBuilder<C> extends Binder {

    /**
     * 获取MQTT客户端
     *
     * @return client
     * @throws NullPointerException 如果没安装抛出空指针异常
     */
    public abstract C getClient() throws NullPointerException;

    /**
     * 安装 MQTT 客户端到服务
     */
    public abstract void install(@NonNull C client);

    /**
     * 卸载 MQTT 客户端
     */
    public abstract void uninstall();

    /**
     * 该服务，是否已经安装MQTT 客户端
     */
    public abstract boolean isInstalled();

    /**
     * 此客户端的配置和传进来的是否相同
     */
    public abstract boolean isSame(Object client);



}
