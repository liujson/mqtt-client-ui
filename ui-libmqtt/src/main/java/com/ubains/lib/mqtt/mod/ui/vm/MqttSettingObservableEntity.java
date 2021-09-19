package com.ubains.lib.mqtt.mod.ui.vm;

import android.view.View;
import android.widget.RadioGroup;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.ubains.lib.mqtt.mod.R;

import cn.liujson.lib.mqtt.util.MqttUtils;

/**
 * @author liujson
 * @date 2021/7/22.
 */
public class MqttSettingObservableEntity {

    public static final int SELF_SIGNED = 2;
    public static final int SERVER_SIGNED = 1;

    public final ObservableBoolean fieldProfileVisible = new ObservableBoolean(false);
    public final ObservableField<String> fieldProfileName = new ObservableField<>("defaultProfile");
    public final ObservableField<String> fieldBrokerAddress = new ObservableField<>();
    public final ObservableField<String> fieldBrokerPort = new ObservableField<>("1883");
    public final ObservableField<String> fieldClientID = new ObservableField<>(MqttUtils.generateClientId());
    public final ObservableField<String> fieldUsername = new ObservableField<>();
    public final ObservableField<String> fieldPassword = new ObservableField<>();
    public final ObservableField<String> fieldKeepAliveInterval = new ObservableField<>("60");
    public final ObservableField<String> fieldConnectionTimeout = new ObservableField<>("15");
    public final ObservableField<String> fieldMaxReconnectDelay = new ObservableField<>(String.valueOf(128000));

    public final ObservableBoolean fieldCleanSession = new ObservableBoolean(true);
    public final ObservableBoolean fieldAutoReconnect = new ObservableBoolean(true);
    //false/CA signed server ; true/Self signed
    public final ObservableBoolean fieldCertificateSelf = new ObservableBoolean(false);
    public final ObservableBoolean fieldSslSecure = new ObservableBoolean(true);


    public final ObservableField<String> fieldCaFilePath = new ObservableField<>();
    public final ObservableField<String> fieldClientCertFilePath = new ObservableField<>();
    public final ObservableField<String> fieldClientKeyFilePath = new ObservableField<>();

    public final ObservableField<String> fieldLwtTopic = new ObservableField<>();
    public final ObservableField<String> fieldLwtMessage = new ObservableField<>();
    public final ObservableBoolean fieldLwtRetained = new ObservableBoolean();


    /**
     * 生成随机 ClientID
     */
    public final void generate() {
        fieldClientID.set(MqttUtils.generateClientId());
    }


    /**
     * Certificate group 选择监听
     */
    public void onGroupCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_signed_server) {
            fieldCertificateSelf.set(false);
        } else if (checkedId == R.id.rb_signed_self) {
            fieldCertificateSelf.set(true);
        }
    }

    /**
     * 文件选择清除
     */
    public void onSelectFileClearClick(View view) {
        if (view.getId() == R.id.btn_clear_ca_file) {
            fieldCaFilePath.set("");
        } else if (view.getId() == R.id.btn_clear_client_cert_file) {
            fieldClientCertFilePath.set("");
        } else if (view.getId() == R.id.btn_clear_client_key_file) {
            fieldClientKeyFilePath.set("");
        }
    }
}
