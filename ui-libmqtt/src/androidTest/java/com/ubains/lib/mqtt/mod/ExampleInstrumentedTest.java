package com.ubains.lib.mqtt.mod;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.ubains.lib.mqtt.mod.test", appContext.getPackageName());
    }

    @Test
    public void rxjavaDispose() {
        final AtomicBoolean running = new AtomicBoolean(false);
        final Observable<String> stringObservable = Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                        Log.d("AAAA", "--subscribe：哈哈或");
                        Thread.sleep(5000);
                        emitter.onNext("哈哈或");
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.newThread())
                .doOnSubscribe(disposable -> {
                    Log.d("AAAA", "--doOnSubscribe");
                    if (!running.compareAndSet(false, true)) {
                        disposable.dispose();
                    }
                })
                .doFinally(() -> {
                    Log.d("AAAA", "--doFinally");
                    running.set(false);
                });
        for (int i = 0; i < 50; i++) {
            new Thread() {
                @Override
                public void run() {
                    stringObservable.subscribe(it -> {
                    }, e -> {
                    });
                }
            }.start();
        }


    }
}