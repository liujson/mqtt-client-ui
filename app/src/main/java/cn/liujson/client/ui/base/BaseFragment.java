package cn.liujson.client.ui.base;

import org.greenrobot.eventbus.EventBus;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author liujson
 * @date 2021/3/10.
 */
public abstract class BaseFragment extends SupportFragment {

    @Override
    public void onStart() {
        super.onStart();
        if (useEventBus() && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (useEventBus() && EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public boolean useEventBus() {
        return false;
    }

}
