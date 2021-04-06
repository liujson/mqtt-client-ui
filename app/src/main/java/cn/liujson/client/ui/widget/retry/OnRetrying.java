package cn.liujson.client.ui.widget.retry;

/**
 * @author liujson
 * @date 2021/4/6.
 */
public interface OnRetrying {
    /**
     * 当进行retry
     *
     * @param retryCount 尝试次数
     * @param nextDelay  下一次的延时时间(ms)
     */
    void onRetry(int retryCount, long nextDelay);
}
