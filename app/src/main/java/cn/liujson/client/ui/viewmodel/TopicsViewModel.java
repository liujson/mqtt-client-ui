package cn.liujson.client.ui.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;

import cn.liujson.client.ui.base.BaseViewModel;

/**
 * 发布
 * @author liujson
 * @date 2021/3/10.
 */
public class TopicsViewModel extends BaseViewModel {

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);

    public TopicsViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
    }

    @Override
    public void onRelease() {

    }
}
