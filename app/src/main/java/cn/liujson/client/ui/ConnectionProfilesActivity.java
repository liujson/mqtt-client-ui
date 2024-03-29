package cn.liujson.client.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityConnectionProfilesBinding;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.fragments.ProfileEditorFragment;
import cn.liujson.client.ui.viewmodel.ConnectionProfilesViewModel;
import cn.liujson.client.ui.widget.popup.AffirmPopupView;
import cn.liujson.client.ui.widget.popup.interfaces.OnPopupClickListener;

public class ConnectionProfilesActivity extends BaseActivity implements OnClickListener
        , ConnectionProfilesViewModel.Navigator {

    ActivityConnectionProfilesBinding viewDataBinding;
    ConnectionProfilesViewModel viewModel;

    public final int EDIT_REQUEST_CODE = 0x12;
    public final int NEW_REQUEST_CODE = 0x13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_connection_profiles);
        viewDataBinding.setClickListener(this);
        viewModel = new ConnectionProfilesViewModel(getLifecycle());
        viewModel.setNavigator(this);
        viewDataBinding.setVm(viewModel);
        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        viewModel.loadConnectionProfiles();
    }


    /**
     * 添加配置
     *
     * @param view
     */
    public void addProfile(View view) {
        Intent intent = new Intent(this, ProfileEditorActivity.class);
        intent.putExtra(ProfileEditorFragment.KEY_MODE, ProfileEditorFragment.Mode.NEW);
        startActivityForResult(intent, NEW_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add_profile) {
            addProfile(v);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
    }

    @Override
    public void editProfile(long id) {
        Intent intent = new Intent(this, ProfileEditorActivity.class);
        intent.putExtra(ProfileEditorFragment.KEY_MODE, ProfileEditorFragment.Mode.EDIT);
        intent.putExtra(ProfileEditorFragment.KEY_PROFILE_ID, id);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    public void delProfile(long id) {
        AffirmPopupView affirmPopupView = new AffirmPopupView(this, "温馨提示", "是否删除该连接参数?");
        new XPopup.Builder(this)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .asCustom(affirmPopupView)
                .show();
        affirmPopupView.setOnAffirmBtnClickListener((popupView, v) -> {
            if (viewModel != null) {
                viewModel.delProfile((int) id);
            }
            popupView.dismiss();
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case NEW_REQUEST_CODE:
                case EDIT_REQUEST_CODE:
                    if (viewModel != null) {
                        viewModel.loadConnectionProfiles();
                    }
                    break;
                default:
                    throw new RuntimeException("状态码错误，必须是：NEW_REQUEST_CODE or EDIT_REQUEST_CODE");
            }
        }
    }
}