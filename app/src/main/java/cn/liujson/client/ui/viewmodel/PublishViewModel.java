package cn.liujson.client.ui.viewmodel;


import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.service.ConnectionBinder;
import cn.liujson.client.ui.service.ConnectionService;
import cn.liujson.client.ui.util.ToastHelper;
import cn.liujson.client.ui.viewmodel.repository.ConnectionServiceRepository;
import cn.liujson.lib.mqtt.api.QoS;

import cn.liujson.logger.LogUtils;
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
        final CharSequence content;
        if (fieldInputContent.get() == null) {
            content = "";
        } else {
            content = fieldInputContent.get();
        }
        assert topic != null;
        assert content != null;
        publishDisposable = repository.publish(topic.toString(),
                content.toString(),
                navigator.readQos(),
                navigator.isRetained())
                .doOnSubscribe(disposable -> {
                    view.setEnabled(false);
                })
                .subscribe(() -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "发送成功");
                    LogUtils.d("MQTT 发送成功,topic:" + topic + ",content:" + content);
                }, throwable -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "发送异常");
                    LogUtils.d("MQTT 发送失败,topic:" + topic);
                });
    }

    @Override
    public void onRelease() {
        if (publishDisposable != null) {
            publishDisposable.dispose();
        }
    }

    @Override
    public void onBindSuccess(ConnectionBinder serviceBinder) {

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


        /**
         * 读取 qos
         */
        QoS readQos();

        /**
         * 是否勾选 retained
         */
        boolean isRetained();
    }
}
