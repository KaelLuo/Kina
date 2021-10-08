package com.kael.kina.httpdns;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;


import androidx.test.platform.app.InstrumentationRegistry;

import com.kael.kina.tools.KinaUtils;
import com.kael.kina.tools.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class DnsParserTest {

    private Context context;

    @Before
    public void setup() {
        Logger.setLogEnable(true);
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test public void dnsLocalTest() {
        for (String url : urls) {
            DnsParser parser = new DnsParser.Builder(context).build(url);
            String ip = parser.getLocalIp();
            Logger.info("DNS local test url %s, ip: %s", url, ip);
            Assert.assertFalse(TextUtils.isEmpty(ip));
            Assert.assertTrue(KinaUtils.isIpv4Url(ip));
        }
    }

    @Test public void dnsApiTest() {
        for (String url : urls) {
            DnsParser parser = new DnsParser.Builder(context)
                    .setId("7128")
                    .setKey("fHS5JADA")
                    .build(url);
            String result = parser.getIpFromApi();
            String savedIp = parser.getSavedIp();
            Logger.info("DNS api test url %s, ip: %s, saved ip: %s", url, result, savedIp);
            Assert.assertFalse(TextUtils.isEmpty(result));
            Assert.assertTrue(KinaUtils.isIpv4Url(result));
            // After request dns api, ip should be saved
            Assert.assertTrue(savedIp.contains(result));
        }
    }

    // Since it is hard to monitor DNS attack by code, so we are not testing that case
    @Test public void dnsSafeHostTest() {
        List<Pair<String, String>> cases = new ArrayList<Pair<String, String>>() {
            {
                add(new Pair<>("http://8.7.6.1/sdk/getAdCodeID", "8.7.6.1"));
                add(new Pair<>("https://2.3.5.6/track/event/report", "2.3.5.6"));
                // No dns attack happen, it should return the domain normally
                add(new Pair<>("http://ad-api.37.com.cn/sdk/getAdCodeID", "ad-api.37.com.cn"));
            }
        };

        for(Pair<String, String> c : cases) {
            DnsParser parser = new DnsParser.Builder(context).build(c.first);
            Assert.assertEquals(c.second, parser.getSafeHost());
        }
    }


    //TODO need modify
    private final String[] urls = {
            "http://ad-api.37.com.cn/sdk/getAdCodeID",
            "https://track-new.39ej7e.com/track/event/report",
            "http://m-api.37.com.cn/go/cfg/user_protocol",
            "http://m-api.37.com.cn/go/cfg/sdk_permission",
            "http://s-api.37.com.cn/sdk/login/",
            "http://s-api.37.com.cn/sdk/reg/",
            "http://s-api.37.com.cn/mobile/reg_res/",
            "http://s-api.37.com.cn/sdk/autoassign",
            "http://s-api.37.com.cn/mobile/scode/",
            "http://s-api.37.com.cn/mobile/reg/",
            "http://39ej7e.com/useragreement/shell/31024.html?gid=1000000&gwversion=4.0.5&pid=1&scut=1",
            "http://s-api.37.com.cn/go/sdk/reportDevDuration",
            "http://s-api.37.com.cn/go/sdk/reportUserDuration",
            "http://m-api.37.com.cn/antiindulge/pcheck/",
            "https://39ej7e.com/mt/user/account/forgetpassword/index?gid=1000000&pid=1&dev=770f7b6e82fa53507f59e30a18d8a4c0&sversion=3.7.7.5&refer=sy_00001&scut=1&locale=zh-cn",
            "http://37.com.cn/service-system/accountappeal/phoneRetrieval",
            "http://s-api.37.com.cn/oauth/login/",
            "http://m-api.37.com.cn/go/cfg/user_protocol/",
            "https://s-api.37.com.cn/go/cfg/float_window",
            "https://s-api.37.com.cn/go/cfg/show_red_float_window",
            "http://us-api.37.com.cn/oauth/gw/fetchUinfo",
            "http://s-api.37.com.cn/go/cfg/age_appropriate_conf",
            "http://s-api.37.com.cn/go/sdk/reportOnline",
            "https://s-api.37.com.cn/go/sdk/mobile/shan_yan_login",
            "https://s-api.37.com.cn/go/sdk/mobile/login_scode",
            "https://s-api.37.com.cn/go/sdk/mobile/send_scode",
            "https://s-api.37.com.cn/go/sdk/mobile/login_pwd",
            "https://s-api.37.com.cn/go/sdk/quick_login",
            "http://s-api.37.com.cn/go/cfg/entrance",
            "http://m-api.37.com.cn/go/sdk/report/login_fail",
            "http://m-api.37.com.cn/go/sdk/popups/login",
            "http://m-api.37.com.cn/go/sdk/report/risk"
    };

}
