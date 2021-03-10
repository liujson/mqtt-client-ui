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
    public void publish() {
        final CharSequence topic = fieldInputTopic.get();
        final CharSequence content = fieldInputContent.get();
        repository.publish(topic.toString(), content.toString(), QoS.AT_MOST_ONCE, false)
                .subscribe(() -> {
                    ToastHelper.showToast(CustomApplication.getApp(), "发送成功");
                });

    }

    @Override
    public void onRelease() {

    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {

    }

    @Override
    public void onBindFailure() {

    }
}
