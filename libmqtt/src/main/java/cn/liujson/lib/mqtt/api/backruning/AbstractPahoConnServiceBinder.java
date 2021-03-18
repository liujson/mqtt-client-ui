package cn.liujson.lib.mqtt.api.backruning;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

import java.util.Objects;

import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;

/**
 * 后台运行的Client 操作 Binder
 *
 * @author liujson
 * @date 2021/3/18.
 */
public abstract class AbstractPahoConnServiceBinder extends AbstractConnBuilder<RxPahoClient>
        implements MqttCallbackExtended {

    protected RxPahoClient mClient;
    /**
     * 安装状态
     */
    private InstallState insState = InstallState.UNINSTALLED;
    /**
     * 状态锁
     */
    private final Object insLock = new Object();


    @Override
    public final RxPahoClient getClient() throws NullPointerException {
        return Objects.requireNonNull(mClient);
    }

    /**
     * 安装后会替换掉之前设置的callback，如果存在
     *
     * @param client
     */
    @Override
    public final void install(@Nullable RxPahoClient client) {
        Objects.requireNonNull(client);
        synchronized (insLock) {
            this.mClient = client;
            this.mClient.setCallback(this);
            insState = InstallState.INSTALLED;
        }
    }

    @Override
    public final void uninstall() {
        synchronized (insLock) {
            this.mClient = null;
            insState = InstallState.UNINSTALLED;
        }
    }

    @Override
    public final boolean isInstalled() {
        synchronized (insLock) {
            return insState == InstallState.INSTALLED;
        }
    }

    @Override
    public final boolean isSame(Object client) {
        if (this.mClient == null) {
            return false;
        }
        if (this.mClient == client) {
            return true;
        }
        if (client == null) {
            return false;
        }
        final Class<?> aClass = client.getClass();
        if (aClass == this.mClient.getParams().getClass()) {
            return Objects.equals(this.mClient.getParams(), client);
        }
        if (this.mClient.getClass() != aClass) {
            return false;
        }
        RxPahoClient that = (RxPahoClient) client;
        return Objects.equals(this.mClient.getParams(), that.getParams());
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        /*
         * 连接成功时回调此方法
         */
    }

    @Override
    public void connectionLost(Throwable cause) {
        /*
         * 第一次连接成功后的情况下，连接丢失时回调此方法
         */
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        /*
         * 消息送达时回调此方法
         */
    }


    public enum InstallState {
        /**
         * 未安装
         */
        UNINSTALLED,
        /**
         * 已安装
         */
        INSTALLED
    }
}
