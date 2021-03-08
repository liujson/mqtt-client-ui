package cn.liujson.client.ui.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.Lifecycle;

import java.util.List;

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
 * @date 2021/3/4.
 */
public class PreviewMainViewModel extends BaseViewModel {

    public final ObservableBoolean fieldConnectEnable = new ObservableBoolean(false);
    public final ObservableBoolean fieldDisconnectEnable = new ObservableBoolean(false);

    private Disposable loadProfilesDisposable;

    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public PreviewMainViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
    }


    @Override
    public void onRelease() {
        if (loadProfilesDisposable != null) {
            loadProfilesDisposable.dispose();
            loadProfilesDisposable = null;
        }
    }


    /**
     * 加载连接属性列表
     */
    public void loadProfiles() {
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
                    if (navigator != null) {
                        navigator.notifyChangeSpinner(data);
                    }
                }, throwable -> {
                    loadProfilesDisposable = null;
                    ToastHelper.showToast(CustomApplication.getApp(), "load connection profiles failure.");
                });
    }


    public interface Navigator {
        /**
         * 通知更新
         */
        void notifyChangeSpinner(List<ConnectionProfile> data);
    }
}
