package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.liujson.client.R;
import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import cn.liujson.logger.LogUtils;
import io.reactivex.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {

    RxPahoClient rxPahoClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        try {
            final ConnectionParams connectionParams = ConnectionParams.newBuilder()
                    .serverURI("tcp://192.168.1.193").build();
            rxPahoClient = new RxPahoClient(connectionParams);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用同步的方式调用
     *
     * @param view
     */
    public void connect(View view) {
        rxPahoClient.connect().subscribeOn(Schedulers.io())
                .subscribe(()->{
                    LogUtils.i("连接成功");
                },throwable -> {

                });
    }

    public void sendMsg(View view) {

    }

    public void subOneTopic(View view) {

    }

    public void unSubOneTopic(View view) {

    }

    public void disconnect(View view) {

    }

    public void disconnectForcibly(View view) {

    }

    public void reconnect(View view) {

    }


    public static void log(String message) {

    }

    public void printf(View view) {

    }


    public void close(View view) {

    }
}