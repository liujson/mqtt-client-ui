package cn.liujson.client.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * @author liujson
 * @date 2021/3/1.
 */
public class PageFragmentStateAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> fragmentList;

    public PageFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,ArrayList<Fragment> fragmentList) {
        super(fragmentManager, lifecycle);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
