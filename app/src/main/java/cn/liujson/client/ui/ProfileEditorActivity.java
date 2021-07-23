package cn.liujson.client.ui;

import androidx.databinding.DataBindingUtil;


import android.os.Bundle;



import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityProfileEditorBinding;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.fragments.ProfileEditorFragment;

import static cn.liujson.client.ui.fragments.ProfileEditorFragment.KEY_MODE;
import static cn.liujson.client.ui.fragments.ProfileEditorFragment.KEY_PROFILE_ID;


/**
 * 配置界面
 */
public class ProfileEditorActivity extends BaseActivity {

    ActivityProfileEditorBinding viewDataBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile_editor);

        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
        ProfileEditorFragment.Mode mode = ProfileEditorFragment.Mode.NEW;
        long id = 0;
        if (getIntent() != null) {
            mode = (ProfileEditorFragment.Mode) getIntent().getSerializableExtra(KEY_MODE);
            id = getIntent().getLongExtra(KEY_PROFILE_ID, 0);
        }
        loadRootFragment(R.id.rl_container, ProfileEditorFragment.newInstance(mode,id));
    }

}