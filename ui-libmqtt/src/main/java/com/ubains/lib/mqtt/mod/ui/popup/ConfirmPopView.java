package com.ubains.lib.mqtt.mod.ui.popup;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.ubains.android.ubutil.touch.DoubleClickUtils;
import com.ubains.lib.mqtt.mod.R;
import com.ubains.lib.mqtt.mod.ui.popup.base.OnPopupClickListener;


/**
 * 确认弹窗
 *
 * @author liujson
 * @date 2021/4/8.
 */
public class ConfirmPopView extends CenterPopupView implements View.OnClickListener {

    private TextView tv_title;
    private ImageButton btn_close;
    private TextView tv_content;
    private Button btn_confirm;
    private Button btn_cancel;

    OnPopupClickListener confirmPopupClickListener, cancelPopupClickListener;

    private String title;
    private String content;

    public void setConfirmPopupClickListener(OnPopupClickListener confirmPopupClickListener) {
        this.confirmPopupClickListener = confirmPopupClickListener;
    }

    public void setCancelPopupClickListener(OnPopupClickListener cancelPopupClickListener) {
        this.cancelPopupClickListener = cancelPopupClickListener;
    }

    public ConfirmPopView(@NonNull Context context) {
        super(context);
    }

    public ConfirmPopView(@NonNull Context context, String title, String content) {
        super(context);
        this.title = title;
        this.content = content;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.comm_popup_confirm;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        tv_title = findViewById(R.id.tv_title);
        btn_close = findViewById(R.id.btn_close);
        tv_content = findViewById(R.id.tv_content);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);


        btn_close.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
        }
        if (!TextUtils.isEmpty(content)) {
            tv_content.setText(content);
        }
    }

    public void setTitle(String title) {
        this.title = title;
        if (tv_title != null) {
            tv_title.setText(title);
        }
    }

    public void setContent(String content) {
        this.content = content;
        if (tv_content != null) {
            tv_content.setText(title);
        }
    }

    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_close) {
            dismiss();
        } else if (id == R.id.btn_confirm) {
            if (confirmPopupClickListener != null) {
                confirmPopupClickListener.onClick(this, v);
            } else {
                dismiss();
            }
        } else if (id == R.id.btn_cancel) {
            if (cancelPopupClickListener != null) {
                cancelPopupClickListener.onClick(this, v);
            } else {
                dismiss();
            }
        }
    }
}
