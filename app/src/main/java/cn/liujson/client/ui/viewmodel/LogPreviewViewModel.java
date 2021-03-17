package cn.liujson.client.ui.viewmodel;

import androidx.lifecycle.Lifecycle;

import cn.liujson.client.ui.base.BaseViewModel;

/**
 * 日志查看 ViewModel
 * @author liujson
 * @date 2021/3/17.
 */
public class LogPreviewViewModel extends BaseViewModel {

    public LogPreviewViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
    }

    @Override
    public void onRelease() {

    }
}
