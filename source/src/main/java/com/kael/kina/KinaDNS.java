package com.kael.kina;

import android.content.Context;

import androidx.annotation.NonNull;

import com.kael.kina.annotation.Api;
import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.httpdns.DNSCallback;

/**
 *
 */
@Api(version = "1.0")
public class KinaDNS {

    public Context context;
    public String id;
    public String key;

    public int timeout; // unit, ms
    public boolean isEnable;
    public DNSCallback callback;

    public KinaDNS(@NonNull Context context, @NonNull String id, @NonNull String key) {
        this.context = context;
        this.id = id;
        this.key = key;
        isEnable = true;
        timeout = ToolsConsent.DNS_TIMEOUT;
    }

}
