package cn.liujson.client.ui.widget;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;



import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 显示日志的自定义 View
 *
 * @author liujson
 * @date 2021/3/17.
 */
public class LogView extends AppCompatTextView {

    public LogView(Context context) {
        super(context);

        init();
    }

    public LogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}
