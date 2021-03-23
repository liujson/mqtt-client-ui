package cn.liujson.logger;

import java.io.Serializable;

/**
 * @author liujson
 * @date 2021/3/23.
 */
public class LogRecord implements Serializable {
    private int priority;
    private String formatMessage;

    public LogRecord(int priority, String formatMessage) {
        this.priority = priority;
        this.formatMessage = formatMessage;

    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFormatMessage() {
        return formatMessage;
    }

    public void setFormatMessage(String formatMessage) {
        this.formatMessage = formatMessage;
    }
}
