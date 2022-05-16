package com.ubains.lib.mqtt.mod.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;
import com.ubains.android.ubutil.comm.UriUtils;
import com.ubains.android.ubutil.comm4j.text.RegexUtils;
import com.ubains.lib.mqtt.mod.R;
import com.ubains.lib.mqtt.mod.databinding.FragmentMqttSettingBinding;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;
import com.ubains.lib.mqtt.mod.ui.popup.ConfirmPopView;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingObservableEntity;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingViewModel;

import java.io.File;
import java.util.Objects;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MqttSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author liujson
 */
public class MqttSettingFragment extends Fragment implements MqttSettingViewModel.Navigator, View.OnClickListener {

    MqttSettingViewModel viewModel;
    FragmentMqttSettingBinding binding;

    LoadingPopupView loadingPopupView;

    MqttSettingObservableEntity entity;

    public MqttSettingFragment() {
        // Required empty public constructor
    }


    public static MqttSettingFragment newInstance() {
        MqttSettingFragment fragment = new MqttSettingFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentMqttSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        entity = new MqttSettingObservableEntity();
        viewModel = new MqttSettingViewModel(entity);
        viewModel.setNavigator(this);

        binding.setVm(entity);
        binding.setFileSelectClick(this);
        binding.btnApply.setOnClickListener(v -> viewModel.applyClick(v));

        viewModel.loadProfile();
    }

    @Override
    public boolean checkApplyParam() {
        final FragmentMqttSettingBinding viewDataBinding = binding;
        if (viewModel == null) {
            return false;
        }

//        if (TextUtils.isEmpty(entity.fieldProfileName.get())) {
//            viewDataBinding.etProfileName.setError(getString(R.string.profile_name_cannot_be_null));
//            return false;
//        }
        if (TextUtils.isEmpty(entity.fieldBrokerAddress.get())) {
            viewDataBinding.etBrokerAddress.setError(getString(R.string.broker_address_cannot_be_null));
            return false;
        }
        if (!RegexUtils.isIP(entity.fieldBrokerAddress.get())) {
            viewDataBinding.etBrokerAddress.setError("解析IP地址错误");
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldBrokerPort.get())) {
            viewDataBinding.etBrokerPort.setError(getString(R.string.broker_port_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldClientID.get())) {
            viewDataBinding.etClientId.setError(getString(R.string.client_id_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldConnectionTimeout.get())) {
            viewDataBinding.etConnectionTimeout.setError(getString(R.string.connection_timeout_cannot_be_null));
            return false;
        }
        if (entity.fieldAutoReconnect.get()) {
            if (TextUtils.isEmpty(entity.fieldKeepAliveInterval.get())) {
                viewDataBinding.etKeepAliveInterval.setError(getString(R.string.keep_alive_interval_cannot_be_null));
                return false;
            }
        }

        //如果遗嘱topic和遗嘱消息不为空
        if (!TextUtils.isEmpty(entity.fieldLwtTopic.get()) || !TextUtils.isEmpty(entity.fieldLwtMessage.get())) {
            if (TextUtils.isEmpty(entity.fieldLwtTopic.get())) {
                viewDataBinding.etLwtTopic.setError(getString(R.string.lwt_topic_cannot_be_null));
                return false;
            }
            if (TextUtils.isEmpty(entity.fieldLwtMessage.get())) {
                viewDataBinding.etLwtMessage.setError(getString(R.string.lwt_message_cannot_be_null));
                return false;
            }
        }
        return true;
    }

    @Override
    public QoS readWillQos() {
        final int selectedIndex = binding.tvLwtQos.getSelectedIndex();
        return MqttUtils.int2QoS(selectedIndex);
    }

    @Override
    public String readSchema() {
        final int selectedIndex = binding.spinnerSchema.getSelectedIndex();
        final String[] stringArray = getResources().getStringArray(R.array.schema);
        return stringArray[selectedIndex];
    }

    @Override
    public boolean isWillRetained() {
        return binding.cbLwtRetained.isChecked();
    }

    @Override
    public void applySuccess() {
        new AestheticDialog
                .Builder(getActivity(), DialogStyle.TOASTER, DialogType.SUCCESS)
                .setTitle("温馨提示")
                .setMessage("配置保存成功")
                .show();
    }

    @Override
    public void applyFailure(Throwable throwable) {
        String errMsg;
        if (throwable instanceof RuntimeException) {
            errMsg = throwable.getMessage();
        } else {
            errMsg = throwable.toString();
        }
        new AestheticDialog.Builder(getActivity(), DialogStyle.TOASTER, DialogType.ERROR)
                .setTitle("温馨提示")
                .setMessage("保存配置失败:" + errMsg)
                .show();
    }

    @Override
    public void applyProcess(String processMsg) {
        if (loadingPopupView != null) {
            loadingPopupView.setTitle(processMsg);
        }
    }

    @Override
    public void showLoading() {
        hideLoading();
        loadingPopupView = new XPopup.Builder(getContext())
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .asLoading();
        loadingPopupView.show();
    }

    @Override
    public void hideLoading() {
        if (loadingPopupView != null) {
            loadingPopupView.dismiss();
        }
    }

    @Override
    public void showApplyConfirm(View view) {

        ConfirmPopView confirmPopView = (ConfirmPopView) new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true)
                .asCustom(new ConfirmPopView(getContext(), "温馨提示", "是否保存连接配置信息"))
                .show();

        confirmPopView.setConfirmPopupClickListener((popupView, v) -> {
            popupView.dismiss();
            if (viewModel != null) {
                viewModel.applyProfile();
            }
        });
    }

    @Override
    public void onLoadProfileEcho(ConnectionProfile connectionProfile) {
        entity.fieldProfileName.set(connectionProfile.profileName);
        final Uri parse = Uri.parse(connectionProfile.brokerAddress);
        final String scheme = parse.getScheme() + "://";
        final String[] stringArray = getResources().getStringArray(R.array.schema);
        int objIndex = 0;
        for (int i = 0; i < stringArray.length; i++) {
            if (Objects.equals(scheme, stringArray[i])) {
                objIndex = i;
            }
        }
        binding.spinnerSchema.setSelectedIndex(objIndex);
        final String host = parse.getHost();
        entity.fieldBrokerAddress.set(host);
        entity.fieldBrokerPort.set(String.valueOf(connectionProfile.brokerPort));
        entity.fieldClientID.set(connectionProfile.clientID);
        entity.fieldUsername.set(connectionProfile.username);
        entity.fieldPassword.set(connectionProfile.password);
        entity.fieldKeepAliveInterval.set(String.valueOf(connectionProfile.keepAliveInterval));
        entity.fieldConnectionTimeout.set(String.valueOf(connectionProfile.connectionTimeout));
        entity.fieldMaxReconnectDelay.set(String.valueOf(connectionProfile.maxReconnectDelay));
        entity.fieldCleanSession.set(connectionProfile.cleanSession);
        entity.fieldAutoReconnect.set(connectionProfile.autoReconnect);
        entity.fieldLwtTopic.set(connectionProfile.willTopic);
        entity.fieldLwtMessage.set(connectionProfile.willMessage);
        entity.fieldLwtRetained.set(connectionProfile.willRetained);

        //ssl
        if (connectionProfile.certificateSigned == 2) {
            entity.fieldCertificateSelf.set(true);
        } else {
            entity.fieldCertificateSelf.set(false);
        }
        entity.fieldCaFilePath.set(connectionProfile.caFilePath);
        entity.fieldClientCertFilePath.set(connectionProfile.clientCertificateFilePath);
        entity.fieldClientKeyFilePath.set(connectionProfile.clientKeyFilePath);

        entity.fieldSslSecure.set(connectionProfile.sslSecure);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel = null;
        binding = null;
        entity = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ca_file) {
            startActivityForResult(selectFileIntent(), 0x16);
        } else if (v.getId() == R.id.btn_client_cert_file) {
            startActivityForResult(selectFileIntent(), 0x17);
        } else if (v.getId() == R.id.btn_client_key_file) {
            startActivityForResult(selectFileIntent(), 0x18);
        }
    }


    public Intent selectFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return Intent.createChooser(intent, "File");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Uri fileUri = data.getData();
            if (fileUri == null) {
                return;
            }
            final File file = UriUtils.uri2File(getContext(), fileUri);
            if (file == null) {
                return;
            }
            switch (requestCode) {
                case 0x16:
                    entity.fieldCaFilePath.set(file.getAbsolutePath());
                    break;
                case 0x17:
                    entity.fieldClientCertFilePath.set(file.getAbsolutePath());
                    break;
                case 0x18:
                    entity.fieldClientKeyFilePath.set(file.getAbsolutePath());
                    break;
            }

        }

    }
}