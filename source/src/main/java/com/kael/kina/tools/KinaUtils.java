package com.kael.kina.tools;

import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO annotation
 */
public final class KinaUtils {


    /**
     * 返回给定 url 中的域名
     * @param url 请求的 API url. 如 http://yapi.39on.com/mock/75/go/cfg/v2/float_window
     * @return API url 的域名, 如 yapi.39on.com
     */
    public static String getHost(String url) {
        if(TextUtils.isEmpty(url)) return url;
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            Logger.error("Trying to get host from Illegal url %s", url, e);
            return "";
        }
    }

    /**
     * 检查给定 url 是否是IP形式的请求
     * @param url 给定 url
     * @return true -> IP 形式的请求 false -> 非 IP 形式的请求
     */
    public static boolean isIpv4Url(String url) {
        if(TextUtils.isEmpty(url)) return false;

        String ip = getHost(url);
        if(TextUtils.isEmpty(ip)) ip = url;

        return isIpv4(ip);
    }

    public static boolean isIpv4(String ip) {
        String[] parts = ip.split("\\.", -1);
        if(parts.length != 4) return false;
        try {
            for (String s : parts) {
                int i = Integer.parseInt(s);
                if(i < 0 || i > 255) return false;
                if(!s.equals(String.valueOf(i))) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private KinaUtils(){}
}
