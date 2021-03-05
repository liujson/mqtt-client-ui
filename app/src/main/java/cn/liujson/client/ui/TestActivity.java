package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.UnsupportedEncodingException;

import cn.liujson.client.R;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.api.IMQTT;
import cn.liujson.lib.mqtt.api.IMQTTCallback;
import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.MqttBuilder;
import cn.liujson.lib.mqtt.service.PahoMqttV3Impl;

public class TestActivity extends AppCompatActivity implements IMQTTMessageReceiver {

    private final String testSendTopic = "testSendTopic";
    private final String testSubTopic = "testSubTopic";

    IMQTT imqtt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        try {
            imqtt = new PahoMqttV3Impl(
                    new MqttBuilder().host("tcp://192.168.1.193:1883")
                            .cleanSession(true));
            imqtt.setMessageReceiver(this);
            log("IMQTT实例化成功==");
        } catch (Exception e) {
            log("实例化失败：" + e.toString());
        }
    }

    public void connect(View view) {
        imqtt.connect(new IMQTTCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log("connect连接成功：" + imqtt.toString());
            }

            @Override
            public void onFailure(Throwable value) {
                log("connect连接失败：" + value.toString());
            }
        });
    }

    public void sendMsg(View view) {
        imqtt.publish(testSendTopic, String.valueOf(System.currentTimeMillis()), QoS.AT_MOST_ONCE, false, new IMQTTCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log("sendMsg 消息发布成功");
            }

            @Override
            public void onFailure(Throwable value) {
                log("sendMsg 消息发布失败：" + value.toString());
            }
        });
    }

    public void subOneTopic(View view) {
        imqtt.subscribe(testSubTopic, QoS.AT_MOST_ONCE, new IMQTTCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                log("subOneTopic 主题订阅成功");
            }

            @Override
            public void onFailure(Throwable value) {
                log("subOneTopic 主题订阅失败：" + value.toString());
            }
        });
    }

    public void unSubOneTopic(View view) {
        imqtt.unsubscribe(testSubTopic, new IMQTTCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log("unSubOneTopic 主题取消订阅成功");
            }

            @Override
            public void onFailure(Throwable value) {
                log("unSubOneTopic 主题取消订阅失败：" + value.toString());
            }
        });
    }

    public void disconnect(View view) {
        imqtt.disconnect(new IMQTTCallback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log("disconnect 断开连接成功");
            }

            @Override
            public void onFailure(Throwable value) {
                log("disconnect 断开失败：" + value.toString());
            }
        });
    }

    public void disconnectForcibly(View view) {
        try {
            imqtt.disconnectForcibly();
        } catch (Exception e) {
            log("强制断开连接出现异常：" + e.toString());
        }
    }

    public void reconnect(View view) {
        ToastHelper.showToast(getApplicationContext(), "暂无");
    }


    public static void log(String message) {
        Log.d("TEST_MQTT", message);
    }

    public void printf(View view) {
        log("log:" + imqtt.toString());
    }

    @Override
    public void onReceive(String topic, byte[] body) {
        String gbk = "字符转码错误";
        try {
            gbk = new String(body, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log("onReceive 收到消息，topic：" + topic + ",body" + gbk);
    }
}