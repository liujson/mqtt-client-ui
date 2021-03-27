package cn.liujson.client.ui.base;

import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

/**
 * @author liujson
 * @date 2021/3/10.
 */
public abstract class BaseFragment extends Fragment {

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
