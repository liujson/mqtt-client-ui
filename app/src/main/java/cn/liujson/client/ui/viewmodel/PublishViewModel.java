package cn.liujson.client.ui.viewmodel;


import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.lib.mqtt.api.QoS;

import io.reactivex.disposables.Disposable;

/**
 * 发布
 *
 * @author liujson
 * @date 2021/3/10.
 */
public class PublishViewModel extends BaseViewModel implements ConnectionServiceRepository.OnBindStatus {

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);
    public final ObservableField<CharSequence> fieldInputContent = new ObservableField<>();
    public final ObservableField<CharSequence> fieldInputTopic = new ObservableField<>();

    private final ConnectionServiceRepository repository;

    private Disposable publishDisposable;

    private Navigator navigator;

    public PublishViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);
        repository = new ConnectionServiceRepository(this);
    }

    public ConnectionServiceRepository getRepository() {
        return repository;
    }

    /**
     * 发布消息
     */
    public void publish(View view) {
        if (this.navigator == null || !this.navigator.checkPublishParam()) {
            return;
        }

        final CharSequence topic = fieldInputTopic.get();
        CharSequence content = fieldInputContent.get();
        if (content == null) {
            content = "";
        }
        assert topic != null;
        publishDisposable = repository.publish(topic.toString(), content.toString(), QoS.AT_MOST_ONCE, false)
                .doOnSubscribe(disposable -> {
                    view.setEnabled(false);
                })
                .subscribe(() -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "发送成功");
                }, throwable -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "发送异常");
                });
    }

    @Override
    public void onRelease() {
        if (publishDisposable != null) {
            publishDisposable.dispose();
        }
    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {

    }

    @Override
    public void onBindFailure() {

    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public interface Navigator {
        /**
         * 检查发布参数
         *
         * @return
         */
        boolean checkPublishParam();
    }
}
