package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import cn.liujson.client.R;
import cn.liujson.client.databinding.ActivityConfigBinding;
import cn.liujson.client.databinding.ActivityConfigListBinding;

public class ConfigListActivity extends AppCompatActivity {

    ActivityConfigListBinding viewDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_config_list);

        viewDataBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }
}