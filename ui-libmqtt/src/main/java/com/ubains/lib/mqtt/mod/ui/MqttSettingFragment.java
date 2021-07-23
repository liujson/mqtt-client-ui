package com.ubains.lib.mqtt.mod.ui;

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
import com.ubains.android.ubutil.comm4j.text.RegexUtils;
import com.ubains.lib.mqtt.mod.R;
import com.ubains.lib.mqtt.mod.databinding.FragmentMqttSettingBinding;
import com.ubains.lib.mqtt.mod.ui.popup.ConfirmPopView;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingObservableEntity;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingViewModel;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MqttSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author liujson
 */
public class MqttSettingFragment extends SupportFragment implements MqttSettingViewModel.Navigator {

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

        binding.btnApply.setOnClickListener(v -> viewModel.applyClick(v));
    }

    @Override
    public boolean checkApplyParam() {
        final FragmentMqttSettingBinding viewDataBinding = binding;
        if (viewModel == null) {
            return false;
        }

        if (TextUtils.isEmpty(entity.fieldProfileName.get())) {
            viewDataBinding.etProfileName.setError(getString(R.string.profile_name_cannot_be_null));
            return false;
        }
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
    public void onSupportVisible() {
        super.onSupportVisible();
        if (viewModel != null) {
            viewModel.loadProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel = null;
        binding = null;
        entity = null;
    }
}