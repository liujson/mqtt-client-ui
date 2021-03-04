package cn.liujson.client.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.locks.ReentrantLock;

import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTConnectionBuilder;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.exception.WrapMQTTException;
import cn.liujson.lib.mqtt.service.PahoMqttV3Impl;

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
    /**
     * MQTT 连接操作对象
     */
    IMQTT imqtt;
    /**
     * 连接 Builder
     */
    IMQTTConnectionBuilder connectionBuilder;

    final ReentrantLock lock = new ReentrantLock();

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
    }


    /**
     * 安装 MQTT 到此服务
     */
    public void setup(IMQTTConnectionBuilder builder) throws WrapMQTTException {
        if (lock.tryLock()) {
            try {
                release();
                this.imqtt = new PahoMqttV3Impl(builder);
                this.connectionBuilder = builder;
            } finally {
                lock.unlock();
            }
        } else {
            throw new WrapMQTTException("尝试绑定IMQTT失败，请不要频繁调用");
        }
    }


    /**
     * 释放资源（端断开连接等等）
     */
    public void release() {
        if(imqtt!=null) {
            try {
                imqtt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Binder
     */
    public class ConnectionServiceBinder extends Binder implements IMQTT {

        public void setup(IMQTTConnectionBuilder builder) throws WrapMQTTException {
            ConnectionService.this.setup(builder);
        }


        @Override
        public void connect(IMQTTCallback<Void> callback) {
            imqtt.connect(callback);
        }

        @Override
        public void subscribe(String topic, QoS qoS, IMQTTCallback<byte[]> callback) {
            imqtt.subscribe(topic, qoS, callback);
        }

        @Override
        public void subscribe(String[] topics, QoS[] qoS, IMQTTCallback<byte[]> callback) {
            imqtt.subscribe(topics, qoS, callback);
        }

        @Override
        public void unsubscribe(String topic, IMQTTCallback<Void> callback) {
            imqtt.unsubscribe(topic, callback);
        }

        @Override
        public void unsubscribe(String[] topics, IMQTTCallback<Void> callback) {
            imqtt.unsubscribe(topics, callback);
        }

        @Override
        public void publish(String topic, byte[] payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
            imqtt.publish(topic, payload, qos, retained, callback);
        }

        @Override
        public void publish(String topic, String payload, QoS qos, boolean retained, IMQTTCallback<Void> callback) {
            imqtt.publish(topic, payload, qos, retained, callback);
        }

        @Override
        public void disconnect(IMQTTCallback<Void> callback) {
            imqtt.disconnect(callback);
        }

        @Override
        public void setMessageReceiver(IMQTTMessageReceiver messageReceiver) {
            imqtt.setMessageReceiver(messageReceiver);
        }

        @Override
        public void close() throws Exception {
            imqtt.close();
        }
    }


    //----------------------------------------------------------------------


}
