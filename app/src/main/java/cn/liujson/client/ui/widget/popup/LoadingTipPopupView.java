package cn.liujson.client.ui.widget.popup;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.wang.avi.AVLoadingIndicatorView;

import cn.liujson.client.R;
import cn.liujson.client.ui.widget.popup.interfaces.OnPopupClickListener;

/**
 * @author liujson
 * @date 2021/4/6.
 */
public class LoadingTipPopupView extends CenterPopupView implements View.OnClickListener {

    AVLoadingIndicatorView avLoading;
    TextView tvContent;
    ImageButton btnClose;

    private String strContent;

    private OnPopupClickListener onCloseBtnClickListener;


    public void setOnCloseBtnClickListener(OnPopupClickListener onCloseBtnClickListener) {
        this.onCloseBtnClickListener = onCloseBtnClickListener;
    }

    public LoadingTipPopupView(@NonNull Context context) {
        super(context);
    }

    public LoadingTipPopupView(@NonNull Context context, String strContent) {
        super(context);
        this.strContent = strContent;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_loading_tip;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        avLoading = findViewById(R.id.av_loading);
        tvContent = findViewById(R.id.tv_content);
        btnClose = findViewById(R.id.btn_close);

        if (strContent != null) {
            tvContent.setText(strContent);
        }

        btnClose.setOnClickListener(this);
    }


    public void setTvContent(String content) {
        this.strContent = content;
        if (tvContent != null) {
            tvContent.setText(content);
        }
    }

    @Override
    public void onClick(View v) {
        if (onCloseBtnClickListener != null) {
            dismiss();
            onCloseBtnClickListener.onClick(this, v);
        } else {
            dismiss();
        }
    }
}
