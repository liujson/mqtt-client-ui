package cn.liujson.client.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.liujson.client.R;
import cn.liujson.client.ui.base.BaseActivity;
import cn.liujson.client.ui.util.DoubleClickUtils;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;


public class DemoActivity extends BaseActivity implements View.OnClickListener {

    RxPahoClient client1;
    RxPahoClient client2;

    private EditText edClient1, edClient2;
    private Button btnClient1, btnClient2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        edClient1 = findViewById(R.id.ed_address_client_1);
        edClient2 = findViewById(R.id.ed_address_client_2);
        btnClient1 = findViewById(R.id.btn_connect_client_1);
        btnClient2 = findViewById(R.id.btn_connect_client_2);

        btnClient1.setOnClickListener(this);
        btnClient2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_connect_client_1) {

        } else if (id == R.id.btn_connect_client_2) {

        } else {
            throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client1!=null){
            //强制关闭连接
            client1.disconnect().doFinally(()->client1.close().subscribe());
        }
        if(client2!=null){
            //关闭连接
            client2.disconnect().doFinally(()->client1.close().subscribe());
        }
    }
}