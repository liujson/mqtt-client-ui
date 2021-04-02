package cn.liujson.client.ui.bean.event;

import androidx.annotation.NonNull;

/**
 * 打印一条日志
 *
 * @author liujson
 * @date 2021/3/9.
 */
public class PrintOneLogEvent {

    public int priority;
    @NonNull
    public String tag;
    @NonNull
    public String message;

    public PrintOneLogEvent(int priority, @NonNull String tag, @NonNull String message) {
        this.priority = priority;
        this.tag = tag;
        this.message = message;
    }
}
