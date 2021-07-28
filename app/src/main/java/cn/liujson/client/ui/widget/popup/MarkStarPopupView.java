package cn.liujson.client.ui.widget.popup;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.R;
import cn.liujson.client.ui.util.DoubleClickUtils;
import cn.liujson.client.ui.widget.popup.interfaces.OnPopupClickListener;

/**
 * 标记星号并设置相关主题订阅
 *
 * @author liujson
 * @date 2021/3/26.
 */
public class MarkStarPopupView extends CenterPopupView implements View.OnClickListener {

    private Button btn_apply, btn_cancel;
    private EditText et_topic_1, et_topic_2, et_topic_3;
    private NiceSpinner spinner_1, spinner_2, spinner_3;

    private OnPopupClickListener onMarkBtnClickListener, onCancelBtnClickListener;

    public void setOnMarkBtnClickListener(OnPopupClickListener onMarkBtnClickListener) {
        this.onMarkBtnClickListener = onMarkBtnClickListener;
    }

    public void setOnCancelBtnClickListener(OnPopupClickListener onCancelBtnClickListener) {
        this.onCancelBtnClickListener = onCancelBtnClickListener;
    }

    public MarkStarPopupView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_marked_star;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        btn_apply = findViewById(R.id.btn_apply);
        btn_cancel = findViewById(R.id.btn_cancel);

        et_topic_1 = findViewById(R.id.et_topic_1);
        et_topic_2 = findViewById(R.id.et_topic_2);
        et_topic_3 = findViewById(R.id.et_topic_3);

        spinner_1 = findViewById(R.id.spinner_qos_1);
        spinner_2 = findViewById(R.id.spinner_qos_2);
        spinner_3 = findViewById(R.id.spinner_qos_3);

        btn_apply.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    public List<TopicWrapper> getTopics() {
        List<TopicWrapper> topics = new ArrayList<>();

        if (!TextUtils.isEmpty(et_topic_1.getText())) {
            topics.add(new TopicWrapper(et_topic_1.getText().toString(), spinner_1.getSelectedIndex()));
        }
        if (!TextUtils.isEmpty(et_topic_2.getText())) {
            topics.add(new TopicWrapper(et_topic_2.getText().toString(), spinner_2.getSelectedIndex()));
        }
        if (!TextUtils.isEmpty(et_topic_3.getText())) {
            topics.add(new TopicWrapper(et_topic_3.getText().toString(), spinner_3.getSelectedIndex()));
        }
        return topics;
    }

    @Override
    public void onClick(View v) {
        if (DoubleClickUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_apply) {
            if (onMarkBtnClickListener != null) {
                onMarkBtnClickListener.onClick(this, v);
            }
        } else if (id == R.id.btn_cancel) {
            if (onCancelBtnClickListener != null) {
                onCancelBtnClickListener.onClick(this, v);
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }


    public static class TopicWrapper {
        public String topic;
        public int qos;

        public TopicWrapper(String topic, int qos) {
            this.topic = topic;
            this.qos = qos;
        }

        public TopicWrapper() {
        }
    }
}
