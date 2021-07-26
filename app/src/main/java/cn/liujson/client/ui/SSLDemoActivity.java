package cn.liujson.client.ui;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cn.liujson.client.R;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.util.DoubleClickUtils;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class SSLDemoActivity extends BaseActivity implements View.OnClickListener, MqttCallbackExtended {

    private static final String TAG = "SSLDemo";

    RxPahoClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        findViewById(R.id.btn_4).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        int id = v.getId();
        try {
            if (id == R.id.btn_1) {
                //连接
                connect();
            } else if (id == R.id.btn_2) {
                //订阅
                if (client != null && client.isConnected()) {
                    client.subscribe("AAAAAA", QoS.AT_LEAST_ONCE).blockingAwait();
                    ToastHelper.showToast(this, "订阅主题成功");
                }
            } else if (id == R.id.btn_3) {
                //发送
                if (client != null && client.isConnected()) {
                    client.publish("AAAAAA", "Hello!".getBytes(), QoS.AT_LEAST_ONCE, false).blockingAwait();
                    ToastHelper.showToast(this, "消息发送成功");
                }
            } else if (id == R.id.btn_4) {
                //断开
                if (client != null && client.isConnected()) {
                    client.disconnect().blockingAwait();
                    ToastHelper.showToast(this, "连接已断开");
                }
            } else {
                throw new IllegalStateException("Unexpected value: " + v.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    private void connect() throws Exception {
        if (client == null) {
            client = new RxPahoClient(
                    ConnectionParams.newBuilder()
                            .username("admin")
                            .password("123456")
                            .serverURI("ssl://192.168.1.144:8883")
                            .socketFactory(getSocketFactory("ca.crt", "server.crt", "client.pem", "1424"))
                            .sslHostnameVerifier((hostname, session) -> true)
                            .build());
        }
        client.setCallback(this);
        client.connect().blockingAwait();
        Log.d(TAG, "连接成功");
        ToastHelper.showToast(this, "连接成功");
    }

    private SSLSocketFactory getSocketFactory(String caPath, String crtPath, String keyPath, String password) throws Exception {
        // CA certificate is used to authenticate server
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
//        FileInputStream caIn = new FileInputStream(caPath);
        InputStream caIn = getAssets().open(caPath);
        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", ca);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        caIn.close();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream crtIn = getAssets().open(crtPath);
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(crtIn);

        crtIn.close();
        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//      ks.load(caIn,password.toCharArray());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", caCert);
        ks.setKeyEntry("private-key", getPrivateKey(keyPath), password.toCharArray(),
                new java.security.cert.Certificate[]{caCert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());
//      keyIn.close();

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");

        context.init(kmf.getKeyManagers(), getTrustManager(), new SecureRandom());

        return context.getSocketFactory();
    }


    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    public PrivateKey getPrivateKey(String path) throws Exception {

        byte[] buffer = Base64.decode(getPem(path), 0);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private String getPem(String path) throws Exception {
        InputStream fin = getAssets().open(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fin));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            } else {
                sb.append(readLine);
                sb.append('\r');
            }
        }
        fin.close();
        return sb.toString();
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "connectComplete");
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost:" + cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        final String msg = new String(message.getPayload());
        Log.d(TAG, "messageArrived:" + msg);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete");
    }
}