package com.ubains.lib.mqtt.mod.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.ubains.android.ubutil.comm.LogUtil;
import com.ubains.android.ubutil.comm.ToastUtils;
import com.ubains.lib.mqtt.mod.provider.IConnectionProfileStore;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;
import com.ubains.lib.mqtt.mod.provider.event.MqttBindChangeEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttConnectCompleteEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttFirstConnectErrorEvent;
import com.ubains.lib.mqtt.mod.provider.event.MqttFirstConnectRetryEvent;
import com.ubains.lib.mqtt.mod.util.LibMqttUtils;
import com.ubains.lib.mqtt.mod.util.retry.NeedRetryException;
import com.ubains.lib.mqtt.mod.util.retry.OnRetrying;
import com.ubains.lib.mqtt.mod.util.retry.RxReconnectDelayObservable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.liujson.lib.mqtt.api.ConnectionParams;
import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.ubains.lib.mqtt.mod.util.retry.RxReconnectDelayObservable.MAX_RECONNECT_DELAY_DEFAULT;
import static com.ubains.lib.mqtt.mod.util.retry.RxReconnectDelayObservable.MIN_RECONNECT_DELAY_DEFAULT;

/**
 * MQTTServiceManager
 *
 * @author liujson
 * @date 2021/3/5.
 */
public class MqttMgr {

    private static final String TAG = "MqttMgr";

    private final RxReconnectDelayObservable rxRetry;

    /**
     * MQTT 连接数据
     */
    private ConnectionBinder mBinder;

    private MqttMgr.OnSubScribeTopic mSubScribeTopic;

    private Disposable firstConnectTask;

    private IConnectionProfileStore mProfileStore;

    private boolean mInitConnect = true;
    private boolean mInitConnectRetry = true;

    private MqttMgr() {
        rxRetry = new RxReconnectDelayObservable(MIN_RECONNECT_DELAY_DEFAULT, MAX_RECONNECT_DELAY_DEFAULT);
        rxRetry.setOnRetrying(mOnRetrying);
        EventBus.getDefault().register(this);
    }


    public static MqttMgr instance() {
        return Holder.INSTANCE;
    }


    public boolean isInitConnectRetry() {
        return mInitConnectRetry;
    }

    public void setInitConnectRetry(boolean mInitConnectRetry) {
        this.mInitConnectRetry = mInitConnectRetry;
    }

    public IConnectionProfileStore getProfileStore() {
        return mProfileStore;
    }

    public void setProfileStore(IConnectionProfileStore mProfileStore) {
        this.mProfileStore = mProfileStore;
    }

    private boolean isInitConnect() {
        return mInitConnect;
    }

    private void setInitConnect(boolean mInitConnect) {
        this.mInitConnect = mInitConnect;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttConnectCompleteEvent(MqttConnectCompleteEvent event) {
        if (mSubScribeTopic != null) {
            mSubScribeTopic.onSubScribeTopic();
        }
    }


    /**
     * 使其运行在前台
     */
    public void runOnForeground(@NonNull Context context) {
        final Intent serviceIntent = getServiceIntent(context);
        //后台启动服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            runOnBackground(context, serviceIntent);
        }
    }

    public static class Builder {
        /**
         * 如果能拿到连接参数，是初始化时候进行连接
         */
        boolean initConnect = true;
        /**
         * 是否初始化连接失败重试
         */
        boolean initConnectRetry = true;
        /**
         * 连接属性序列化对象
         */
        IConnectionProfileStore profileStore;

        public Builder initConnect(boolean initConnect) {
            this.initConnect = initConnect;
            return this;
        }

        public Builder initConnectRetry(boolean initConnectRetry) {
            this.initConnectRetry = initConnectRetry;
            return this;
        }

        public Builder profileStore(IConnectionProfileStore profileStore) {
            this.profileStore = profileStore;
            return this;
        }

        public void init(@NonNull Context context) {
            Objects.requireNonNull(context);
            instance().setInitConnect(this.initConnect);
            instance().setInitConnectRetry(this.initConnectRetry);
            instance().setProfileStore(profileStore);
            instance().bindToApplication(context.getApplicationContext());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 初始化 MQTT 服务
     *
     * @param context
     */
    public void init(@NonNull Context context) {
        builder().initConnect(true)
                .initConnectRetry(true)
                .init(context);
    }

    /**
     * 后台运行服务
     */
    public void runOnBackground(@NonNull Context context, @NonNull Intent serviceIntent) {
        context.startService(serviceIntent);
    }

    /**
     * 绑定服务到 Application
     */
    public boolean bindToApplication(@NonNull Context application) {
        return bindService(application.getApplicationContext(), mServiceConnection);
    }

    public void unbindToApplication(@NonNull Context context) {
        unbindService(context.getApplicationContext(), mServiceConnection);
    }

    /**
     * 与 Activity 绑定（所有绑定的Activity生命周期结束，服务结束）
     */
    public void bindToActivity(@NonNull Activity activity, @NonNull ServiceConnection serviceConnection) {
        bindService(activity, serviceConnection);
    }


    public boolean bindService(@NonNull Context context, @NonNull ServiceConnection serviceConnection) {
        return context.bindService(getServiceIntent(context), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(@NonNull Context context, @NonNull ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }


    private Intent getServiceIntent(@NonNull Context context) {
        return new Intent(context, ConnectionService.class);
    }

    /**
     * 获取Binder(只有通过绑定Application启动才有这个值)
     *
     * @return
     */
    @Nullable
    public ConnectionBinder binder() {
        return mBinder;
    }

    public boolean isBind() {
        return mBinder != null;
    }


    public boolean isInstalled() {
        return mBinder != null && mBinder.isInstalled();
    }

    public boolean isSame(Object object) {
        checkBindAndInstall();
        return mBinder.isSame(object);
    }


    public boolean isConnected() {
        checkBindAndInstall();
        return mBinder.getClient().isConnected();
    }


    public boolean isClosed() {
        checkBindAndInstall();
        return mBinder.getClient().isClosed();
    }


    public RxPahoClient getClient() {
        checkBindAndInstall();
        return mBinder.getClient();
    }

    public void install(RxPahoClient client) {
        if (!isBind()) {
            throw new RuntimeException("MQTT 后台服务未启动");
        }
        mBinder.install(client);
    }


    public void uninstall() {
        checkBindAndInstall();
        mBinder.uninstall();
    }


    public List<Pair<String, QoS>> getSubList() {
        return binder().getClient().getActiveSubs();
    }

    private void checkBindAndInstall() {
        if (!isBind()) {
            throw new RuntimeException("MQTT 后台服务未启动");
        }
        if (!isInstalled()) {
            throw new RuntimeException("MQTT 后台服务未安装");
        }
    }


    private final OnRetrying mOnRetrying = (retryCount, nextDelay) -> {
        final String message = "首次连接失败，正在进行第" + retryCount + "次重连,下次重试延时：" + nextDelay + "ms";
        LogUtil.d(TAG, message);
        EventBus.getDefault().post(new MqttFirstConnectRetryEvent(message));
    };


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectionBinder) service;
            EventBus.getDefault().postSticky(new MqttBindChangeEvent(true));
            if (mInitConnect && getProfileStore() != null) {
                final ConnectionProfile connectionProfile = getProfileStore().load();
                if (connectionProfile != null) {
                    //第一次连接可能会失败，创建一个任务用来重连
                    startFirstConnectTask(connectionProfile);
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            ToastUtils.showToast("MQTT后台服务绑定失败");
            EventBus.getDefault().post(new MqttBindChangeEvent(false));
        }
    };

    private static final class Holder {
        private static final MqttMgr INSTANCE = new MqttMgr();
    }


    final AtomicBoolean isConnecting = new AtomicBoolean(false);

    /**
     * 连接到
     *
     * @param connectionProfile
     */
    public Observable<String> connectTo(@NonNull final ConnectionProfile connectionProfile,
                                        final int sleepTime) {
//        if (isConnecting.get()) {
//            return Observable.error(new RuntimeException("正在配置中,不用重复操作..."));
//        }
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            emitter.onNext("请不要关闭...");
            conditionSleep(sleepTime);
            final ConnectionParams connectionParams = LibMqttUtils.profile2Params(connectionProfile);
            //1. 检查服务是否绑定启动完成
            if (!isBind()) {
                emitter.onError(new RuntimeException("请先启动后台服务后再次尝试"));
                return;
            }
            //2. 检查是否此时存在相同参数的client正在运行
            if (isInstalled()) {
                if (isSame(connectionParams)) {
                    emitter.onError(new RuntimeException("相同的参数配置不需要重复连接"));
                    return;
                }
                final RxPahoClient client = getClient();
                emitter.onNext("请不要关闭，正在强制断开MQTT连接...");
                client.disconnectForcibly(10000, 10000);
                conditionSleep(sleepTime);
                emitter.onNext("请不要关闭，正在卸载客户端...");
                uninstall();
            }
            //3. 创建RxPahoClient 进行连接
            final RxPahoClient rxPahoClient = new RxPahoClient(connectionParams);
            try {
                emitter.onNext("请不要关闭，尝试连接到服务器...");
                conditionSleep(sleepTime);
                rxPahoClient.setCallback(binder());
                rxPahoClient.connectWithResult().waitForCompletion(10000);
                emitter.onNext("请不要关闭，正在安装为MQTT服务...");
                conditionSleep(sleepTime);
                install(rxPahoClient);
            } catch (Exception e) {
                //安装过程出现异常强制关闭掉
                emitter.onNext("请不要关闭，正在释放资源...");
                try {
                    rxPahoClient.close(true);
                } catch (Exception ee) {
                    rxPahoClient.closeForcibly(500, 500);
                }
                emitter.onError(e);
                return;
            }
            emitter.onComplete();
        }).doOnSubscribe(disposable -> {
            final boolean b = isConnecting.getAndSet(true);
            if (b) {
                Log.d(TAG, "connectTo:connecting dispose....");
                disposable.dispose();
            }
        }).doFinally(() -> {
            isConnecting.getAndSet(false);
            Log.d(TAG, "connectTo:doFinally!");
        });
    }

    @WorkerThread
    private void conditionSleep(int sleepTime) {
        if (sleepTime > 0) {
            SystemClock.sleep(sleepTime);
        }
    }


    /**
     * 开始进行首次连接及连接失败后重试
     */
    private void startFirstConnectTask(@NonNull ConnectionProfile connectionProfile) {
        if (firstConnectTask != null) {
            firstConnectTask.dispose();
        }
        Observable<String> startConnectObservable = connectTo(connectionProfile, 0)
                //拦截所有错误,转换为需要重试异常
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof Exception) {
                        return Observable.error(new NeedRetryException(throwable.getMessage()));
                    } else {
                        return Observable.error(new NeedRetryException(throwable));
                    }
                });
        if (mInitConnectRetry) {
            startConnectObservable = startConnectObservable.retryWhen(rxRetry);
        }
        firstConnectTask = startConnectObservable
                .doFinally(() -> {
                    firstConnectTask = null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(info -> {
                    LogUtil.d(TAG, info);
                }, throwable -> {
                    LogUtil.e(TAG, "MQTT 初始连接失败:" + throwable.toString());
                    EventBus.getDefault().post(new MqttFirstConnectErrorEvent(throwable));
                }, () -> {
                    LogUtil.d(TAG, "MQTT 初始连接完成");
                });
    }

    /**
     * 触发连接成功订阅主题
     */
    public interface OnSubScribeTopic {

        void onSubScribeTopic();
    }
}
