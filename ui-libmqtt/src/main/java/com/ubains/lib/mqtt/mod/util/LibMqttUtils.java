package com.ubains.lib.mqtt.mod.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import androidx.annotation.NonNull;

import com.ubains.android.ubutil.comm.AppUtils;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.util.random.RandomStringUtils;


/**
 * @author liujson
 * @date 2021/4/22.
 */
public class LibMqttUtils {

    public static ConnectionParams profile2Params(ConnectionProfile profile) throws Exception {
        // TODO: 2021/3/19  配置连接参数
        ConnectionParams.Builder builder = ConnectionParams.newBuilder()
                .serverURI(profile.brokerAddress + ":" + profile.brokerPort)
                .cleanSession(profile.cleanSession)
                .automaticReconnect(profile.autoReconnect)
                .maxReconnectDelay(profile.maxReconnectDelay)
                .keepAlive(profile.keepAliveInterval)
                .connectionTimeout(profile.connectionTimeout)
                .clientId(profile.clientID)
                .username(profile.username)
                .password(profile.password);
        //自身验证
        if (profile.certificateSigned == 2) {
            builder.socketFactory(createSocketFactory(profile.caFilePath, profile.clientCertificateFilePath, profile.clientKeyFilePath, RandomStringUtils.randomNumeric(4), profile.sslSecure));
            builder.sslHostnameVerifier((hostname, session) -> true);
        }
        if (!TextUtils.isEmpty(profile.willTopic) && !TextUtils.isEmpty(profile.willMessage)) {
            builder.setWill(profile.willTopic, profile.willMessage.getBytes(), profile.willQoS, profile.willRetained);
        }
        return builder.build();
    }


    private static SSLSocketFactory createSocketFactory(@NonNull String caPath, String crtPath, String keyPath, String password, boolean sslSecure) throws Exception {
        Objects.requireNonNull(caPath);
        // CA certificate is used to authenticate server
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
        InputStream caIn;
        //判断文件路径是否是应用内部assets路径
        if (isAssetsPath(caPath)) {
            caIn = openAssetsPath(caPath);
        } else {
            caIn = new FileInputStream(caPath);
        }
        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", ca);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        caIn.close();


        KeyManager[] keyManagers = null;

        if (!TextUtils.isEmpty(crtPath) && !TextUtils.isEmpty(keyPath)) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream crtIn;
            if (isAssetsPath(crtPath)) {
                crtIn = openAssetsPath(crtPath);
            } else {
                crtIn = new FileInputStream(crtPath);
            }
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(crtIn);

            crtIn.close();
            // client key and certificates are sent to server so it can authenticate
            // us
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            //ks.load(caIn,password.toCharArray());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", caCert);
            ks.setKeyEntry("private-key", getPrivateKey(keyPath), password.toCharArray(),
                    new java.security.cert.Certificate[]{caCert});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            //keyIn.close();

            keyManagers = kmf.getKeyManagers();
        }

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");
        if (sslSecure) {
            //默认验证
            context.init(keyManagers, tmf.getTrustManagers(), new SecureRandom());
        } else {
            //信任所有
            context.init(keyManagers, getTrustManager(), new SecureRandom());
        }

        return context.getSocketFactory();
    }


    private static PrivateKey getPrivateKey(String keyPemPath) throws Exception {
        byte[] buffer = Base64.decode(getPem(keyPemPath), 0);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private static String getPem(String keyPem) throws Exception {
        InputStream fin;
        if (isAssetsPath(keyPem)) {
            fin = openAssetsPath(keyPem);
        } else {
            fin = new FileInputStream(keyPem);
        }
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


    private static boolean isAssetsPath(@NonNull String path) {
        return path.startsWith("file:///android_asset/");
    }

    private static InputStream openAssetsPath(@NonNull String path) {
        try {
            final String pathReplace = path.replace("file:///android_asset/", "");
            return AppUtils.getApp().getAssets().open(pathReplace);
        } catch (Exception e) {
            throw new IllegalArgumentException("MQTT 读取 Assets 错误或文件不存在:" + path);
        }
    }
}
