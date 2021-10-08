package com.kael.kina;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kael.kina.annotation.ContentType;


/**
 * A proxy for a network request, it contains some basic values
 */
class Net {
    boolean enableHttpDns;
    Context context;
    String method;
    String domain;
    String params;
    @ContentType.Type String contentType;
    String accept;
    KinaCallback callback;


    Net(@Nullable String domain, @Nullable String params, @NonNull String method, @ContentType.Type String contentType, String accept, KinaCallback callback) {
        this.method = method;
        this.domain = domain;
        this.callback = callback;
        this.params = params;
        this.accept = accept;
        this.contentType = contentType;
    }

    protected void setHttpDns(Context context, boolean isEnable) {
        this.context = context;
        this.enableHttpDns = isEnable;
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
                    && TextUtils.equals(contentType, n.contentType)
                    && TextUtils.equals(accept, n.accept)
                    && TextUtils.equals(params, n.params)
                    && (callback == null || callback.equals(n.callback));
        }
        else return false;
    }


    public String toParams() {
        // TODO 我们希望通过某些方法，在请求时，设置并修改参数中的时间字段，然后重新进行签名，以保证每次重试均有效，
        return params;
    }
}
