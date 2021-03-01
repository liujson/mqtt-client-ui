package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityConfigBinding;

/**
 * 配置界面
 */
public class ConfigActivity extends AppCompatActivity {

    ActivityConfigBinding viewDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_config);

        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }
}