package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;

import java.io.Serializable;

import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityProfileEditorBinding;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.util.InputMethodUtils;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.ProfileEditorViewModel;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * 配置界面
 */
public class ProfileEditorActivity extends BaseActivity implements ProfileEditorViewModel.Navigator {

    ActivityProfileEditorBinding viewDataBinding;
    ProfileEditorViewModel viewModel;

    public static final String KEY_MODE = "key_mode";
    public static final String KEY_PROFILE_ID = "key_profile_id";

    private Mode mode = Mode.NEW;
    private long profileID = 0;

    public enum Mode implements Serializable {
        EDIT,
        NEW,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile_editor);

        final Intent intent = getIntent();
        if (intent != null) {
            mode = (Mode) intent.getSerializableExtra(KEY_MODE);
            profileID = intent.getLongExtra(KEY_PROFILE_ID, 0);
        }

        viewModel = new ProfileEditorViewModel(getLifecycle(), mode, profileID);
        viewModel.setNavigator(this);

        viewDataBinding.setVm(viewModel);
        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        if (mode == Mode.EDIT && profileID > 0) {
            viewModel.queryProfileById(profileID);
        }

        //过渡滑动效果
        OverScrollDecoratorHelper.setUpOverScroll(viewDataBinding.scrollView);
    }


    @Override
    public boolean checkApplyParam() {
        if (viewModel == null) {
            return false;
        }

        if (TextUtils.isEmpty(viewModel.fieldProfileName.get())) {
            viewDataBinding.etProfileName.setError(getString(R.string.profile_name_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(viewModel.fieldBrokerAddress.get())) {
            viewDataBinding.etBrokerAddress.setError(getString(R.string.broker_address_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(viewModel.fieldBrokerPort.get())) {
            viewDataBinding.etBrokerPort.setError(getString(R.string.broker_port_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(viewModel.fieldClientID.get())) {
            viewDataBinding.etClientId.setError(getString(R.string.client_id_cannot_be_null));
            return false;
        }
        if (TextUtils.isEmpty(viewModel.fieldConnectionTimeout.get())) {
            viewDataBinding.etConnectionTimeout.setError(getString(R.string.connection_timeout_cannot_be_null));
            return false;
        }
        if (viewModel.fieldAutoReconnect.get()) {
            if (TextUtils.isEmpty(viewModel.fieldKeepAliveInterval.get())) {
                viewDataBinding.etKeepAliveInterval.setError(getString(R.string.keep_alive_interval_cannot_be_null));
                return false;
            }
        }
        return true;
    }

    @Override
    public void applySuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onEditingQueryProfile(ConnectionProfile connectionProfile) {
        //回显到控件上
        if (viewModel != null) {
            viewModel.fieldProfileName.set(connectionProfile.profileName);
            viewModel.fieldBrokerAddress.set(connectionProfile.brokerAddress);
            viewModel.fieldBrokerPort.set(String.valueOf(connectionProfile.brokerPort));
            viewModel.fieldClientID.set(connectionProfile.clientID);
            viewModel.fieldCleanSession.set(connectionProfile.cleanSession);
            viewModel.fieldUsername.set(connectionProfile.username);
            viewModel.fieldPassword.set(connectionProfile.password);
            viewModel.fieldConnectionTimeout.set(String.valueOf(connectionProfile.connectionTimeout));
            viewModel.fieldKeepAliveInterval.set(String.valueOf(connectionProfile.keepAliveInterval));
            viewModel.fieldAutoReconnect.set(connectionProfile.autoReconnect);
            viewModel.fieldMaxReconnectDelay.set(String.valueOf(connectionProfile.maxReconnectDelay));
        }
    }

    @Override
    public void onEditingQueryProfileFail(Throwable throwable) {
        ToastHelper.showToast(this, "Sorry,operation failure.");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
        viewDataBinding = null;
    }


}