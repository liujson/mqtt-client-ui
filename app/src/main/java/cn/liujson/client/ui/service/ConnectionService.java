package cn.liujson.client.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.liujson.lib.mqtt.api.IMQTTBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.refactor.IMQTTWrapper;
import cn.liujson.lib.mqtt.service.refactor.service.PahoV3MQTTClient;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * MQTT 连接管理服务。
 * 一个服务就相当于一个后台连接。
 *
 * @author liujson
 * @date 2021/3/3.
 */
public class ConnectionService extends Service {

    private static final String TAG = "ConnectionService";

    private final ConnectionServiceBinder binder = new ConnectionServiceBinder();

    private IMQTTWrapper<PahoV3MQTTClient> mqttClient;

    final Object lockObject = new Object();

    final List<IMQTTMessageReceiver> receiverList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //直接通过绑定启动后台服务很快就会被系统杀死
        Log.d(TAG, "==onBind==");
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "==onCreate==");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "==onStartCommand==");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "==onDestroy==");
        //尝试关闭并释放资源
        if (mqttClient != null) {
            mqttClient.destroy();
            final Disposable rxCloseSafety = binder.closeForcibly()
                    .subscribe(() -> {
                        Log.d(TAG, "==disconnectForcibly success==");
                    }, throwable -> {
                        Log.d(TAG, "==disconnectForcibly failure:" + throwable.toString());
                    });
        }
        //清空集合元素
        receiverList.clear();
    }


    /**
     * 安装 MQTT Client服务
     */
    public IMQTTWrapper<PahoV3MQTTClient> setup(IMQTTWrapper<PahoV3MQTTClient> clientWrapper) {
        //如果锁不可用，那么当前线程被阻塞，休眠一直到该锁可以获取
        if (mqttClient == null) {
            synchronized (lockObject) {
                if (mqttClient == null) {
                    mqttClient = clientWrapper;
                    clientWrapper.setMessageReceiver(new MessageReceiver());
                }
            }
        }
        return mqttClient;
    }

    /**
     * 移除安装（移除安装前请先断开连接）
     */
    public void unset() {
        synchronized (lockObject) {
            mqttClient = null;
            receiverList.clear();
        }
    }


    public IMQTTWrapper<PahoV3MQTTClient> getClientWrapper() {
        return mqttClient;
    }

    /**
     * Binder
     */
    public class ConnectionServiceBinder extends ConnectionBinder<PahoV3MQTTClient> {
        /**
         * 安装配置
         *
         * @param imqttWrapper
         * @return
         */
        @Override
        public IMQTTWrapper<PahoV3MQTTClient> setup(IMQTTWrapper<PahoV3MQTTClient> imqttWrapper) {
            return ConnectionService.this.setup(imqttWrapper);
        }

        @Override
        public boolean isSetup() {
            return this.getWrapper() != null;
        }

        @Override
        public boolean isSame(IMQTTBuilder builder) {
            return Objects.equals(mqttClient.getBuilder(), builder);
        }

        @Override
        public void registerMessageReceiver(IMQTTMessageReceiver messageReceiver) {
            if (!receiverList.contains(messageReceiver)) {
                receiverList.add(messageReceiver);
            }
        }

        @Override
        public void unregisterMessageReceiver(IMQTTMessageReceiver messageReceiver) {
            receiverList.remove(messageReceiver);
        }

        @Override
        public IMQTTWrapper<PahoV3MQTTClient> getWrapper() {
            return getClientWrapper();
        }

        /**
         * 安全关闭(先尝试断开连接然后关闭)
         *
         * @return
         */
        @Override
        public Completable closeSafety() {
            if (mqttClient == null) {
                return Completable.error(new WrapMQTTException("请先setup后关闭"));
            }
            return mqttClient.getClient()
                    .rxCloseSafety()
                    .doOnComplete(() -> {
                        unset();
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /**
         * 强制结束并释放资源
         * 需要等待其完成
         * (默认10秒超时，设置timeout建议不要小于此时间)
         *
         * @return Completable
         */
        @Override
        public Completable closeForcibly() {
            if (mqttClient == null) {
                return Completable.error(new WrapMQTTException("请先setup后关闭"));
            }
            return mqttClient.getClient()
                    .rxCloseForcibly(6000, 3000)
                    .timeout(10, TimeUnit.SECONDS)
                    .doFinally(() -> {
                        unset();
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public class MessageReceiver implements IMQTTMessageReceiver {

        @Override
        public void onReceive(String topic, byte[] body) throws Exception {
            final Iterator<IMQTTMessageReceiver> it = receiverList.iterator();
            while (it.hasNext()) {
                final IMQTTMessageReceiver next = it.next();
                next.onReceive(topic, body);
            }
        }
    }
    //----------------------------------------------------------------------
}
