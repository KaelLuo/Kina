package com.kael.kina;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kael.kina.proxy.RequestTools;


/**
 * A proxy for a network request, it contains some basic values
 */
class Net {
    String method;
    String domain;
    RequestTools params;
    KinaCallback callback;
    KinaDNS dns;


    Net(@Nullable String domain, @Nullable RequestTools params, @NonNull String method, KinaCallback callback) {
        this.method = method;
        this.domain = domain;
        this.callback = callback;
        this.params = params;
    }

    protected void setHttpDns(KinaDNS dns) {
        this.dns = dns;
    }

    /**
     * Since we do not know non-typical request params looks like, so we simply check if params equals or not
     * @param obj the obj to be compare
     * @return obj equals this or not
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Net) {
            Net n = (Net)obj;
            return TextUtils.equals(method, n.method)
                    && TextUtils.equals(domain, n.domain)
                    && (params == n.params || params.equals(n.params))
                    && (callback == null || callback.equals(n.callback));
        }
        else return false;
    }

}
