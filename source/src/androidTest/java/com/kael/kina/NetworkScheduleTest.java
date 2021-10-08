package com.kael.kina;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;


import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.tools.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class NetworkScheduleTest {

    private Context context;

    @Before
    public void setup() {
        Logger.setLogEnable(true);
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test public void networkNullCheck() {
//        for(int i = 0; i < 32; i++) {
//            String domain = i % 2 == 0 ? null : "https://super-test.hapeenor.com/v1/utils/time";
//            String param = (i >> 1) % 2 == 0 ? null : "param";
//            String method = (i >> 2) % 2 == 0 ? null : ToolsConsent.HTTP_GET;
//            String key = (i >> 3) % 2 == 0 ? null : EncryptApi.signKey(context);
//            NetworkCallback callback = (i >> 4) % 2 == 0 ? null : new NetworkCallback() {
//                @Override
//                public void onSuccess(int code, byte[] data) {
//                    Logger.info("NetworkScheduleTest#networkNullCheck request success");
//                }
//
//                @Override
//                public void onFailure(int code, String message) {
//                    Logger.info("NetworkScheduleTest#networkNullCheck request fail");
//                }
//            };
//
//            Net net = new Net(domain, param, method, callback, key);
//            boolean isRequestSuccess = NetworkUtils.post(net.domain, net.toParams(), net.callback, true, false);
//            Assert.assertEquals(domain != null && callback != null, isRequestSuccess);
//            isRequestSuccess = NetworkUtils.get(net.domain, net.toParams(), net.callback, true, false);
//            Assert.assertEquals(domain != null && callback != null, isRequestSuccess);
//            NetworkSchedule.add(net);
//        }
//        NetworkSchedule.add(null);
    }

    /**
     * Test case for: http://gitlab.hapeenor.com/jl-android/android-tools/issues/30
     * Test pass if no crash occur
     */
    @Test public void stackOverFlowTest() {
//        NetworkSchedule.setQueryInterval(1);
//        String url = "https://super-test.hapeenor.com/v1/utils/time";
//        NetworkSchedule.add(new Net(url, "", ToolsConsent.HTTP_GET, null, ""));
//        NetworkSchedule.add(new Net("a", "", ToolsConsent.HTTP_GET, new NetworkCallback() {
//            @Override
//            public void onSuccess(int code, byte[] data) {}
//
//            @Override
//            public void onFailure(int code, String message) {}
//        }, ""));
//        try{
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            Logger.info( "InterruptedException happen ", e);
//        }
    }


    /**
     * This test case exist because of issue: http://gitlab.hapeenor.com/jl-android/android-tools/issues/20
     * This test probably cost a lot of time, for now, we don't care.
     * When doing CI/CD, you may need opt this part
     *
     */
    @Test public void addAsyncTests() {
        NetworkSchedule.setQueryInterval(1);
        for(int i = 0; i < 10; i++) {
            addAsyncTest(i);
        }
    }

    private void addAsyncTest(int multi) {
//        final String url = "https://super-test.hapeenor.com/v1/utils/time";
//        for(int i = 0; i < 100 * multi; i++) {
//            RequestParams params = new RequestParams(UUID.randomUUID().toString(), (int) (Math.random() * 10000), (float) Math.random() * 10000);
//            NetworkSchedule.add(url, params.toPostParam(context), ToolsConsent.HTTP_GET, new NetworkCallback() {
//                @Override
//                public void onSuccess(int code, byte[] data) {
//                    Logger.info("onSuccess time: " +  Long.parseLong(new String(data)) + " code: " + code);
//                }
//
//                @Override
//                public void onFailure(int code, String message) {
//                    Logger.info("onFailure, code" + code + " message: " + message);
//                }
//            }, EncryptApi.signKey(context));
//        }
//        try{
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Logger.info(Logger.GLOBAL_TAG, "InterruptedException happen ", e);
//        }
//        Assert.assertEquals(1, 1); // if this code execute without exception, test pass
    }
}
