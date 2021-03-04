package cn.liujson.client.ui.viewmodel;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import cn.liujson.client.R;
import cn.liujson.client.ui.adapter.ConnectionProfilesAdapter;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.entities.ConnectionProfile;
import cn.liujson.client.ui.util.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ConnectionProfilesViewModel extends BaseViewModel {

    public final ObservableList<ConnectionProfilesAdapter.ItemProfile> dataList = new ObservableArrayList<>();
    public final ConnectionProfilesAdapter adapter = new ConnectionProfilesAdapter(dataList);
    public final LinearLayoutManager layoutManager = new LinearLayoutManager(CustomApplication.getApp());


    Disposable loadProfilesDisposable;

    Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public ConnectionProfilesViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (adapter instanceof ConnectionProfilesAdapter) {
                ConnectionProfilesAdapter profilesAdapter = (ConnectionProfilesAdapter) adapter;
                if (view.getId() == R.id.tv_edit) {
                    if (navigator != null) {
                        navigator.editProfile(profilesAdapter.getData().get(position).id);
                    }
                } else if (view.getId() == R.id.tv_del) {
                    if (navigator != null) {
                        navigator.delProfile(profilesAdapter.getData().get(position).id);
                    }
                }
            }
        });
        adapter.addChildClickViewIds(R.id.tv_edit, R.id.tv_del);
    }

    /**
     * 加载配置
     */
    public void loadConnectionProfiles() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
        }
        loadProfilesDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .loadProfiles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    loadProfilesDisposable = null;
                    dataList.clear();
                    for (ConnectionProfile profile : data) {
                        dataList.add(ConnectionProfilesAdapter.ItemProfile.covert(profile));
                    }
                    adapter.notifyDataSetChanged();
                }, throwable -> {
                    loadProfilesDisposable = null;
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }

    /**
     * 删除配置
     *
     * @param id
     */
    public void delProfile(int id) {
        Disposable delProfilesDisposable = DatabaseHelper
                .getInstance()
                .connectionProfileDao()
                .deleteProfile(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    ToastHelper.showToast(CustomApplication.getApp(), "successfully!");
                    adapter.notifyDataSetChanged();
                }, throwable -> {
                    ToastHelper.showToast(CustomApplication.getApp(), "Sorry,delete failure.");
                });
    }

    @Override
    public void onRelease() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
            loadProfilesDisposable = null;
        }
    }

    public interface Navigator {
        /**
         * 编辑配置
         */
        void editProfile(long id);

        /**
         * 删除配置
         */
        void delProfile(long id);
    }
}
