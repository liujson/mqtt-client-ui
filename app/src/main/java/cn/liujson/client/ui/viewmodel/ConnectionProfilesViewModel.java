package cn.liujson.client.ui.viewmodel;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    public ConnectionProfilesViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
    }


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
                        final ConnectionProfilesAdapter.ItemProfile itemProfile = new ConnectionProfilesAdapter.ItemProfile();
                        itemProfile.profileName = profile.profileName;
                        itemProfile.brokerPort = profile.brokerPort;
                        itemProfile.brokerAddress = profile.brokerAddress;
                        dataList.add(itemProfile);
                    }
                    adapter.notifyDataSetChanged();
                }, throwable -> {
                    loadProfilesDisposable = null;
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }

    @Override
    public void onRelease() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
            loadProfilesDisposable = null;
        }
    }
}
