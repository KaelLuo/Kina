package com.kael.kina.httpdns;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import com.kael.kina.Kina;
import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.tools.DES;
import com.kael.kina.tools.KinaUtils;
import com.kael.kina.tools.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * TODO annotation
 */
public class DnsParser {

    private final String SP_REPO_DNS = "http_dns_parser_sp";
    private final String SP_KEY_DNS_SUFFIX = "http_dns_parser_key";

    private String id;
    private String key;
    private Context context;
    private DNSCallback callback;
    private String timeout;

    private String host; // 域名,通过url处理所得: 如 yapi.39on.com
    private String url; // 具体请求 url, 如 http://yapi.39on.com/mock/75/go/cfg/v2/float_window


    public static class Builder {

        private final Context context;
        private String key;
        private String id;
        private int timeout = ToolsConsent.DNS_TIMEOUT;
        private DNSCallback callback = (url, attackIp, executeIp) ->
                Logger.warning("DNS attack happen, Kina defence it , url: %s, attackIp: %s, executeIp: %s", url, attackIp, executeIp);


        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public DnsParser.Builder setTimeOut(int timeOut) {
            if(timeOut <= 0) return this;
            this.timeout = timeOut;
            return this;
        }

        public DnsParser.Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public DnsParser.Builder setId(String id) {
            this.id = id;
            return this;
        }

        public DnsParser.Builder setCallback(DNSCallback callback) {
            if(callback == null) return this;
            this.callback = callback;
            return this;
        }

        public DnsParser build(@NonNull String url) {
            DnsParser dnsParser = new DnsParser();
            dnsParser.context = context;
            dnsParser.url = url;
            String host = KinaUtils.getHost(url);
            dnsParser.host = TextUtils.isEmpty(host) ? url : host;
            dnsParser.id = id;
            dnsParser.key = key;
            dnsParser.callback = callback;
            dnsParser.timeout = String.valueOf(timeout);
            return dnsParser;
        }
    }

    public String getSafeUrl() {
        return TextUtils.isEmpty(url) ? "" : url.replace(host, getSafeHost());
    }

    /**
     * Get given url ip from rather local dns or http dns, depends on specific logic
     * @return Safe url ip from request, if no dns change happen, return origin host.
     *         if Given host is a ip, renturn it without change
     */
    public String getSafeHost() {
        if(KinaUtils.isIpv4Url(url)) return host;
        String savedIp = getSavedIp();
        String localIp = getLocalIp();
        if(!TextUtils.isEmpty(localIp) && savedIp.contains(localIp)) return host;

        String apiIp = getIpFromApi();
        savedIp = getSavedIp(); // Saved IP will refresh after call getIpFromApi()
        if(TextUtils.isEmpty(apiIp) || (!TextUtils.isEmpty(localIp) && savedIp.contains(localIp))) return host;

        callback.onAttack(url, localIp, apiIp);
        return apiIp;
    }

    /**
     *
     * @return Local Dns ip for given url otherwise {@code ""} if exception or other corner case
     */
    @NonNull public String getLocalIp() {
        if(TextUtils.isEmpty(host)) return "";
        try {
            String ip = InetAddress.getByName(host).getHostAddress();
            Logger.info("Local IP: %s from host: %s", ip, host);
            return TextUtils.isEmpty(ip) ? "" : ip;
        } catch (UnknownHostException e) {
            Logger.error("Exception when trying to get local IP, url: %s", host, e);
            return "";
        }
    }

    /**
     * 通过请求腾讯 HTTP_DNS API获取IP, 详细文档如下:
     * https://cloud.tencent.com/document/product/379/54976#.E8.BF.94.E5.9B.9E-a-.E4.B8.8E-aaaa-.E7.9A.84.E8.AE.B0.E5.BD.95
     * @return 腾讯 HTTP DNS 返回的 IP, 若返回多个IP，取第一个
     */
    public String getIpFromApi() {
        if(TextUtils.isEmpty(host)) return "";

        String api = "http://119.29.29.98/d";
        DnsParam param = new DnsParam(host, id, key);
        param.timeout = timeout;
        Kina request = new Kina.Builder()
                .setContentType(param.contentType)
                .setParams(param.toGetParam(context))
                .build();
        String result = new String(request.getSync(api), StandardCharsets.UTF_8);
        if (TextUtils.isEmpty(result)) return "";

        String data = new String(DES.decrypt(KinaUtils.hexToBytes(result), key), StandardCharsets.UTF_8);
        if(TextUtils.isEmpty(data)) return "";
        Logger.info("Decrypt http dns data: %s", data);

        saveIp(data);

        if(!data.contains(";")) return data;
        return data.split(";")[0];
    }

    /**
     * 通过 host 获取其存储的给定 IP
     * @return host 对应的 IP 值或者以 ";" 隔开的多个 IP 值
     */
    public String getSavedIp() {
        if(context == null) return "";
        SharedPreferences preferences = context.getSharedPreferences(SP_REPO_DNS, Context.MODE_PRIVATE);
        return preferences.getString(SP_KEY_DNS_SUFFIX + host, "");
    }

    /**
     * 存储给定 IP 值在其对应的 前缀 + host 作为 key 的{@link SharedPreferences} 中.
     * <br>存储时会对存储IP进行格式校验，不符合IPV4格式的字符不会被存储
     * @param ip IP 值或者以 ";" 隔开的多个 IP
     */
    protected void saveIp(String ip) {
        if(context == null) return;
        if(TextUtils.isEmpty(ip)) return;
        if(!KinaUtils.isIpv4Url(ip) && !ip.contains(";")) return;

        if(ip.contains(";")) {
            String[] ips = ip.split(";");
            for (String i : ips) {
                if(!KinaUtils.isIpv4Url(i)) return;
            }
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(SP_REPO_DNS, Context.MODE_PRIVATE).edit();
        editor.putString(SP_KEY_DNS_SUFFIX + host, ip);
        editor.apply();
    }


}
