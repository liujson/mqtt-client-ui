package cn.liujson.client.ui.base;

import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;


/**
 * @author liujson
 * @date 2021/1/12.
 */
public abstract class BaseViewModel extends BaseObservable implements LifecycleObserver {

    private static final String TAG = "BaseViewModel";

    private Lifecycle mLifecycle;

    public BaseViewModel(Lifecycle mLifecycle) {
        this.mLifecycle = mLifecycle;
        mLifecycle.addObserver(this);
    }

    /**
     * 释放资源
     */
    public abstract void onRelease();

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void releaseResource() {
        onRelease();
        mLifecycle.removeObserver(this);
        mLifecycle = null;
        Log.d(TAG,"监听生命周期自动释放资源");
    }
}
