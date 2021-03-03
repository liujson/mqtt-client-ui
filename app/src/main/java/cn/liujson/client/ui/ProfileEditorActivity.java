package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityProfileEditorBinding;
import cn.liujson.client.ui.viewmodel.ProfileEditorViewModel;

/**
 * 配置界面
 */
public class ProfileEditorActivity extends AppCompatActivity implements ProfileEditorViewModel.Navigator {

    ActivityProfileEditorBinding viewDataBinding;
    ProfileEditorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile_editor);
        viewModel = new ProfileEditorViewModel(getLifecycle());
        viewModel.setNavigator(this);

        viewDataBinding.setVm(viewModel);
        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
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
        return true;
    }

    @Override
    public void applySuccess() {
        startActivity(new Intent(this, ConnectionProfilesActivity.class));
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
        viewDataBinding = null;
    }
}