package cn.liujson.client.ui.viewmodel;

import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;

import org.greenrobot.eventbus.EventBus;

import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.bean.event.ConnectChangeEvent;
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
public class TopicsViewModel extends BaseViewModel implements ConnectionServiceRepository.OnBindStatus {

    public final ObservableBoolean fieldAllEnable = new ObservableBoolean(false);
    public final ObservableField<CharSequence> fieldInputTopic = new ObservableField<>();

    private final ConnectionServiceRepository repository;

    private Disposable subscribeDisposable;

    private PublishViewModel.Navigator navigator;

    public TopicsViewModel(Lifecycle mLifecycle) {
        super(mLifecycle);

        repository = new ConnectionServiceRepository(this);
    }

    @Override
    public void onRelease() {
        if (subscribeDisposable != null) {
            subscribeDisposable.dispose();
        }
    }


    public ConnectionServiceRepository getRepository() {
        return repository;
    }

    /**
     * 订阅主题
     *
     * @param view
     */
    public void subscribe(View view) {
        // TODO: 2021/3/11  
        final CharSequence topic = fieldInputTopic.get();
        subscribeDisposable = repository.subscribe(topic.toString(), QoS.AT_MOST_ONCE)
                .doOnSubscribe(disposable -> {
                    view.setEnabled(false);
                })
                .subscribe(() -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "订阅成功");
                }, throwable -> {
                    view.setEnabled(true);
                    ToastHelper.showToast(CustomApplication.getApp(), "订阅失败");
                });
    }

    @Override
    public void onBindSuccess(ConnectionService.ConnectionServiceBinder serviceBinder) {
        if (serviceBinder.isSetup()) {
            if (serviceBinder.getWrapper().getClient().isConnected()) {
                EventBus.getDefault().post(new ConnectChangeEvent(true));
            } else {
                EventBus.getDefault().post(new ConnectChangeEvent(false));
            }
        }
    }

    @Override
    public void onBindFailure() {

    }

    public void setNavigator(PublishViewModel.Navigator navigator) {
        this.navigator = navigator;
    }

    public interface Navigator {
        /**
         * 检查发布参数
         *
         * @return
         */
        boolean checkParam();


        /**
         * 读取 qos
         */
        QoS readQos();
    }
}
