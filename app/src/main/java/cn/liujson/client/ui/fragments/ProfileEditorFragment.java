package cn.liujson.client.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ubains.lib.mqtt.mod.databinding.FragmentMqttSettingBinding;
import com.ubains.lib.mqtt.mod.ui.vm.MqttSettingObservableEntity;

import java.io.Serializable;

import cn.liujson.client.R;
import cn.liujson.client.ui.base.BaseFragment;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.ProfileEditorViewModel;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;


/**
 * 编辑连接
 *
 * @author liujson
 * @date 2021/7/23.
 */
public class ProfileEditorFragment extends BaseFragment implements ProfileEditorViewModel.Navigator {

    FragmentMqttSettingBinding binding;

    ProfileEditorViewModel viewModel;

    public static final String KEY_MODE = "key_mode";
    public static final String KEY_PROFILE_ID = "key_profile_id";

    MqttSettingObservableEntity entity;

    public enum Mode implements Serializable {
        EDIT,
        NEW,
    }


    private Mode mode = Mode.NEW;
    private long profileID = 0;

    public static ProfileEditorFragment newInstance(Mode mode, long profileId) {
        ProfileEditorFragment fragment = new ProfileEditorFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_MODE, mode);
        bundle.putLong(KEY_PROFILE_ID, profileId);
        fragment.setArguments(bundle);
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
        if (getArguments() != null) {
            mode = (Mode) getArguments().getSerializable(KEY_MODE);
            profileID = getArguments().getLong(KEY_PROFILE_ID, 0);
        }
        viewModel = new ProfileEditorViewModel(getLifecycle(), entity, mode, profileID);
        viewModel.setNavigator(this);
        entity.fieldProfileVisible.set(true);
        binding.setVm(entity);


        if (mode == Mode.EDIT && profileID > 0) {
            viewModel.queryProfileById(profileID);
        }


        binding.btnApply.setOnClickListener(v -> {
            viewModel.applyProfile();
        });
    }


    @Override
    public boolean checkApplyParam() {
        if (viewModel == null) {
            return false;
        }

        if (TextUtils.isEmpty(entity.fieldProfileName.get())) {
            binding.etProfileName.setError(getString(R.string.profile_name_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldBrokerAddress.get())) {
            binding.etBrokerAddress.setError(getString(R.string.broker_address_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldBrokerPort.get())) {
            binding.etBrokerPort.setError(getString(R.string.broker_port_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldClientID.get())) {
            binding.etClientId.setError(getString(R.string.client_id_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(entity.fieldConnectionTimeout.get())) {
            binding.etConnectionTimeout.setError(getString(R.string.connection_timeout_cannot_be_null));
            return false;
        }
        if (entity.fieldAutoReconnect.get()) {
            if (TextUtils.isEmpty(entity.fieldKeepAliveInterval.get())) {
                binding.etKeepAliveInterval.setError(getString(R.string.keep_alive_interval_cannot_be_null));
                return false;
            }
        }

        //如果遗嘱topic和遗嘱消息不为空
        if (!TextUtils.isEmpty(entity.fieldLwtTopic.get()) || !TextUtils.isEmpty(entity.fieldLwtMessage.get())) {
            if (TextUtils.isEmpty(entity.fieldLwtTopic.get())) {
                binding.etLwtTopic.setError(getString(R.string.lwt_topic_cannot_be_null));
                return false;
            }
            if (TextUtils.isEmpty(entity.fieldLwtMessage.get())) {
                binding.etLwtMessage.setError(getString(R.string.lwt_message_cannot_be_null));
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
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void onEditingQueryProfile(ConnectionProfile connectionProfile) {
        //回显到控件上
        if (viewModel != null) {
            entity.fieldProfileName.set(connectionProfile.profileName);
            entity.fieldBrokerAddress.set(connectionProfile.brokerAddress);
            entity.fieldBrokerPort.set(String.valueOf(connectionProfile.brokerPort));
            entity.fieldClientID.set(connectionProfile.clientID);
            entity.fieldCleanSession.set(connectionProfile.cleanSession);
            entity.fieldUsername.set(connectionProfile.username);
            entity.fieldPassword.set(connectionProfile.password);
            entity.fieldConnectionTimeout.set(String.valueOf(connectionProfile.connectionTimeout));
            entity.fieldKeepAliveInterval.set(String.valueOf(connectionProfile.keepAliveInterval));
            entity.fieldAutoReconnect.set(connectionProfile.autoReconnect);
            entity.fieldMaxReconnectDelay.set(String.valueOf(connectionProfile.maxReconnectDelay));

            entity.fieldLwtTopic.set(connectionProfile.willTopic);
            entity.fieldLwtMessage.set(connectionProfile.willMessage);
            entity.fieldLwtRetained.set(connectionProfile.willRetained);

            binding.tvLwtQos.setSelectedIndex(MqttUtils.qoS2Int(connectionProfile.willQoS));
        }
    }

    @Override
    public void onEditingQueryProfileFail(Throwable throwable) {
        ToastHelper.showToast(getContext(), "Sorry,operation failure.");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel = null;
        binding = null;
    }

}
