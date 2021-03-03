package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import cn.liujson.client.R;

import cn.liujson.client.databinding.ActivityConnectionProfilesBinding;
import cn.liujson.client.ui.viewmodel.ConnectionProfilesViewModel;

public class ConnectionProfilesActivity extends AppCompatActivity implements OnClickListener {

    ActivityConnectionProfilesBinding viewDataBinding;
    ConnectionProfilesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_connection_profiles);
        viewDataBinding.setClickListener(this);
        viewModel = new ConnectionProfilesViewModel(getLifecycle());
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
        startActivity(intent);
    }

    /**
     * 删除配置
     *
     * @param view
     */
    public void removeProfile(View view) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add_profile) {
            addProfile(v);
        }
//        else if (id == R.id.btn_remove_profile) {
//            removeProfile(v);
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel = null;
    }
}