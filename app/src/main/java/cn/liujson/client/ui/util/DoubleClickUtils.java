package cn.liujson.client.ui.util;

/**
 * 双击检测工具
 * @author liujson
 */
public class DoubleClickUtils {
    private static final long DEFAULT_DELAY = 500L;
    private static long lastClickTime = 0L;
    private static int lastResId = -1;

    public DoubleClickUtils() {
    }

    public static boolean isFastDoubleClick(int resId) {
        return isFastDoubleClick(resId, 500L);
    }

    public static boolean isFastDoubleClick(int resId, long delay) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (lastResId == resId && lastClickTime > 0L && timeD < delay) {
            return true;
        } else {
            lastClickTime = time;
            lastResId = resId;
            return false;
        }
    }
}