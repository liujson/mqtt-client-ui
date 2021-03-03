package cn.liujson.client.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    private static Toast toast;

    private ToastHelper() {
        throw new AssertionError();
    }

    public static void showToast(Context context, String msg) {
        showToast(context, msg, 0);
    }

    @SuppressLint({"ShowToast"})
    public static void showToast(Context context, String msg, int duration) {
        ensureToast(context);
        toast.setText(msg);
        toast.setDuration(duration);
        toast.show();
    }

    @SuppressLint({"ShowToast"})
    public static void showToast(Context context, int resId, int duration) {
        ensureToast(context);
        toast.setText(resId);
        toast.setDuration(duration);
        toast.show();
    }

    @SuppressLint({"ShowToast"})
    private static void ensureToast(Context context) {
        if (toast == null) {
            Class var1 = ToastHelper.class;
            synchronized(ToastHelper.class) {
                if (toast == null) {
                    toast = Toast.makeText(context.getApplicationContext(), " ", 0);
                }
            }
        }
    }
}
