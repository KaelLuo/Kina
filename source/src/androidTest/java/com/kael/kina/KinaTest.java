package com.kael.kina;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;


import com.kael.kina.tools.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class KinaTest {

    private Context context;

    @Before
    public void setup() {
        Logger.setLogEnable(true);
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }


    /**
     * Test cases for http://gitlab.hapeenor.com/jl-android/android-tools/issues/22
     */
    private volatile AtomicInteger nums;
    @Test public void orderTest() throws InterruptedException {
        // You can also set 192.168.0.112 client-test.hapeenor.com in host to make it work
        // This api will return whatever you passed
        String url = "http://192.168.0.112/v1/test/client_test";
        nums = new AtomicInteger(0);
        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Logger.info(msg.arg1 + "");
                Assert.assertEquals(nums.intValue(), msg.arg1);
                nums.set(nums.intValue() + 1);
            }
        };
        KinaCallback callback = new KinaCallback() {
            @Override
            public void onSuccess(int code, byte[] data) {
                Message message = new Message();
                message.arg1 = Integer.parseInt(new String(data));
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(int code, String message) {
                Assert.fail();
            }
        };
        int times = 500;
        for (int i = 0; i < times; i++) {
            Logger.info("for post " + i);
            Kina request = new Kina.Builder()
                    .setParams(String.valueOf(i))
                    .setCallback(callback)
                    .build();
            request.postAsync(url);
        }
        Thread.sleep(35 * times);
    }


    // To test if NetworkUtil can handle and remove repeat request
    private boolean isRepeat;
    @Test public void repeatTest() {
        String url = "https://super-test.hapeenor.com/v1/utils/time";
        isRepeat = false;
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Assert.assertFalse(isRepeat);
                isRepeat = true;
            }
        };
        final KinaCallback callback = new KinaCallback() {
            @Override
            public void onSuccess(int code, byte[] data) {
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onFailure(int code, String message) {
                handler.sendEmptyMessage(1);
            }
        };
        for(int i = 0; i < 10; i++) {
            Logger.info("for fetch time " + i);
            Kina request = new Kina.Builder()
                    .setParams(String.valueOf(i))
                    .setCallback(callback)
                    .build();
            request.postAsync(url);
        }
    }


}
