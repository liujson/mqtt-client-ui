package com.ubains.lib.mqtt.mod.util.retry;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


/**
 * @author liujson
 * @date 2021/4/6.
 */
public class RxReconnectDelayObservable implements Function<Observable<? extends Throwable>, Observable<?>> {
    /**
     * 最小延时单位（ms）
     */
    public final static int MIN_RECONNECT_DELAY_DEFAULT = 1000;
    /**
     * 最大重连延时
     */
    public final static int MAX_RECONNECT_DELAY_DEFAULT = 128000;
    /**
     * 默认最大重连次数
     */
    private final static int MAX_RECONNECT_TIMES_DEFAULT = -1;

    private final int maxReconnectDelay;
    private final int minReconnectDelay;
    /**
     * 当超过这个连接次数的时候不再重连，-1表示无穷大
     */
    private final int maxReconnectTimes;
    /**
     * 重试次数
     */
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private int reconnectDelay = 0;

    private OnRetrying onRetrying;

    /**
     * 通过延时重连
     *
     * @param minReconnectDelay 最小延时
     * @param maxReconnectDelay 最大延时
     */
    public RxReconnectDelayObservable(int minReconnectDelay, int maxReconnectDelay) {
        if (minReconnectDelay <= 0) {
            minReconnectDelay = MIN_RECONNECT_DELAY_DEFAULT;
        }
        this.minReconnectDelay = minReconnectDelay;
        this.maxReconnectDelay = maxReconnectDelay;
        this.maxReconnectTimes = MAX_RECONNECT_TIMES_DEFAULT;
    }

    /**
     * 只设置最大重连次数，延时使用默认
     *
     * @param maxReconnectTimes
     */
    public RxReconnectDelayObservable(int maxReconnectTimes) {
        this.maxReconnectTimes = maxReconnectTimes;
        this.minReconnectDelay = MIN_RECONNECT_DELAY_DEFAULT;
        this.maxReconnectDelay = MAX_RECONNECT_DELAY_DEFAULT;
    }


    /**
     * 重置连接延时
     */
    public void resetReconnectDelay() {
        reconnectDelay = 0;
    }

    public void setOnRetrying(OnRetrying onRetrying) {
        this.onRetrying = onRetrying;
    }

    /**
     * 检查是否需要延时
     *
     * @return
     */
    private boolean checkRetry(Throwable throwable) {
        if (throwable instanceof NeedRetryException) {
            if (maxReconnectTimes < 0) {
                //没有最大重连次数限制
                return true;
            } else return retryCount.get() < maxReconnectTimes;
        }
        return false;
    }


    /**
     * 返回下一个延时时间
     * (指数补偿算法)
     * int count = 0;
     * int MAXSLEEP = 128000;
     * for (int numsec = 1; numsec < MAXSLEEP; numsec <<= 1)
     * if (numsec <= MAXSLEEP / 2)
     * System.out.println("numsec:" + numsec + ",count:" + (++count));
     *
     * @return
     */
    private long nextDelay() {
        final int nextDelay = reconnectDelay << 1;
        if (reconnectDelay != 0 && nextDelay < maxReconnectDelay) {
            reconnectDelay = nextDelay;
        } else {
            reconnectDelay = minReconnectDelay;
        }
        return reconnectDelay;
    }

    @Override
    public Observable<?> apply(@NonNull Observable<? extends Throwable> observable) throws Exception {
        return observable.flatMap((Function<Throwable, Observable<?>>) throwable -> {
            if (checkRetry(throwable)) {
                int incrementAndGet = retryCount.incrementAndGet();
                long nextDelay = nextDelay();
                System.out.println("第" + incrementAndGet + "次重试," +
                        "nextDelay:" + nextDelay);
                if (onRetrying != null) {
                    onRetrying.onRetry(incrementAndGet, nextDelay);
                }
                //指数补偿计算下次延时
                return Observable.timer(nextDelay, TimeUnit.MILLISECONDS);
            }
            //直接返回错误不重试
            return Observable.error(throwable);
        }).onErrorResumeNext((Function<Throwable, Observable<?>>) Observable::error);
    }
}
