package cn.liujson.client.ui.widget.retry;

/**
 * 需要进行 Retry
 */
public class NeedRetryException extends RuntimeException {

    public NeedRetryException() {
    }

    public NeedRetryException(String message) {
        super(message);
    }

    public NeedRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeedRetryException(Throwable cause) {
        super(cause);
    }
}