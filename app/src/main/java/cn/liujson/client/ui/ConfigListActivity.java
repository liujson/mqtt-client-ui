package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityConfigBinding;
import cn.liujson.client.databinding.ActivityConfigListBinding;

public class ConfigListActivity extends AppCompatActivity implements OnClickListener {

    ActivityConfigListBinding viewDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_config_list);
        viewDataBinding.setClickListener(this);

        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }


    /**
     * 添加配置
     *
     * @param view
     */
    public void addProfile(View view) {
        Intent intent = new Intent(this, ConfigActivity.class);
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
        } else if (id == R.id.btn_remove_profile) {
            removeProfile(v);
        }
    }
}