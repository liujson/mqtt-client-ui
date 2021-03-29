package cn.liujson.client.ui.widget.popup;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;

import cn.liujson.client.R;
import cn.liujson.client.ui.util.DoubleClickUtils;
import cn.liujson.client.ui.widget.popup.interfaces.OnPopupClickListener;

/**
 * 确认弹框
 *
 * @author liujson
 * @date 2021/3/29.
 */
public class AffirmPopupView extends CenterPopupView implements View.OnClickListener {

    private Button btn_ok, btn_cancel;
    private TextView tv_title, tv_content;

    private String title, content;

    private OnPopupClickListener onAffirmBtnClickListener, onCancelBtnClickListener;

    public void setOnAffirmBtnClickListener(OnPopupClickListener onAffirmBtnClickListener) {
        this.onAffirmBtnClickListener = onAffirmBtnClickListener;
    }

    public void setOnCancelBtnClickListener(OnPopupClickListener onCancelBtnClickListener) {
        this.onCancelBtnClickListener = onCancelBtnClickListener;
    }

    public AffirmPopupView(@NonNull Context context, String title, String content) {
        super(context);
        this.title = title;
        this.content = content;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_affirm;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        tv_title = findViewById(R.id.tv_title);
        tv_content = findViewById(R.id.tv_content);

        tv_title.setText(title);
        tv_content.setText(content);

        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_ok) {
            if (onAffirmBtnClickListener != null) {
                onAffirmBtnClickListener.onClick(this, v);
            }
        } else if (id == R.id.btn_cancel) {
            if (onCancelBtnClickListener != null) {
                onCancelBtnClickListener.onClick(this, v);
            } else {
                dismiss();
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }


}
