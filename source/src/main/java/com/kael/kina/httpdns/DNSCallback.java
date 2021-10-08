package com.kael.kina.httpdns;

import com.kael.kina.annotation.Api;

@Api(version = "1.0")
public interface DNSCallback {

    void onAttack(String url, String attackIp, String executeIp);
}
