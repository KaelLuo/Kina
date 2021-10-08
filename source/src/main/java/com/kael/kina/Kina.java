package com.kael.kina;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kael.kina.annotation.ContentType;
import com.kael.kina.constant.NetworkCode;
import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.httpdns.DnsParser;
import com.kael.kina.proxy.HeaderTools;
import com.kael.kina.tools.KinaUtils;
import com.kael.kina.tools.Logger;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FIXME 已知问题: 目前设置无限重试后, 虽然客户端能保证按调度一直重试，直到成功，但因后端设置超时时间的关系，有可能请求依旧会因超时失败。若需解决此问题，需要后端统一时间字段
 * TODO annotation
 * <p>
 * Network util class, support http <b>POST</b>, <b>GET</b> request, and other network util methods.
 * </p>
 * <p>
 * All methods defined here will set to <b>static</b>.
 * </p>
 * <p>
 * Methods here are implement with {@link HttpURLConnection} and {@link ByteArrayOutputStream}
 * </p>
 * <p>
 * Typically offer <b>Async</b> request methods, and it usually means there could be called in <b>UI thread</b>
 * Moreover in <b>Async</b> method, network request will be send one by one in order
 * </p>
 * <p>
 * We set the class to <b>abstract</b> since we do not wanna someone accidentally create a reference.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class Kina {

    private Kina() {}

    // common Logger String
    private static final String HTTP_EMPTY_DOMAIN = "The passed param domain is a empty string";
    private static final String URL_IS = "Requested url: %s";
    private static final String URL_BUILD_EXCEPTION = "Exception happen when create url";
    private static final String RESPONSE_READ_EXCEPTION = "Exception happen when read response from http request";
    private static final String CLOSE_EXCEPTION_IS = "Exception happen when trying to close %s, this may cause a memory leak";
    private static final String STREAM_BUILD_EXCEPTION = "Http passed param in http is not null, but exception happen when build buffer output stream, this may cause passed request http param does not take effect";
    private static final String METHOD_SET_EXCEPTION = "Exception happen when set http request method";
    private static final String URL_CONNECTION_EXCEPTION = "Exception happen when calling url.connect()";

    private static final List<Net> requests = new ArrayList<>(); // This list exist just for filter repeat network request
    private static final ScheduledExecutorService singleThreadExecutor = Executors.newSingleThreadScheduledExecutor();

    @Nullable private Context context;
    private HeaderTools header;
    private boolean isCallbackInUiThread;
    private boolean enableHttpDns;
    private String accept;
    private String contentType;
    private int retryNum = 3;
    private int retryInterval;
    private String params;
    private boolean waitSuccess;
    private boolean retryAble;
    private KinaCallback callback;

    public static class Builder {

        private Context context;
        private HeaderTools header;
        private boolean enableHttpDns;
        private String accept = ContentType.JSON;
        private String contentType = ContentType.JSON;
        private int retryNum = 3;
        private int retryInterval = 500; // 0.5 sec
        private String params = "";
        private boolean waitSuccess;
        private boolean retryAble;
        private boolean isCallbackInUiThread = true;
        private KinaCallback callback = new KinaCallback() {
            @Override
            public void onSuccess(int code, byte[] data) {
                Logger.info("Default network callback, request success, code: %d", code);
            }

            @Override
            public void onFailure(int code, String message) {
                Logger.warning("Default network callback, request failure, code: %d, message: %s", code, message);
            }
        };

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setHeader(HeaderTools header) {
            this.header = header;
            return this;
        }

        public Builder setAccept(String accept) {
            this.accept = accept;
            return this;
        }

        public Builder setRetry(boolean retryAble, int nums) {
            this.retryAble = retryAble;
            this.retryNum = nums;
            return this;
        }

        public Builder setInterval(int interval) {
            this.retryInterval = interval;
            return this;
        }

        public Builder setParams(String params) {
            this.params = params;
            return this;
        }

        public Builder enableHttpDns(@NonNull Context context) {
            this.context = context;
            this.enableHttpDns = true;
            return this;
        }

        public Builder setHttpDns(@Nullable Context context, boolean isEnable) {
            this.context = context;
            this.enableHttpDns = isEnable;
            return this;
        }

        public Builder setWaitSuccess(boolean waitSuccess) {
            this.waitSuccess = waitSuccess;
            return this;
        }


        public Builder setCallback(@NonNull KinaCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setCallbackInUiThread(boolean isInUiThread) {
            this.isCallbackInUiThread = isInUiThread;
            return this;
        }

        public Kina build() {
            Kina utils = new Kina();
            utils.accept = this.accept;
            utils.retryNum = this.retryNum;
            utils.contentType = this.contentType;
            utils.retryInterval = this.retryInterval;
            utils.params = this.params;
            utils.waitSuccess = this.waitSuccess;
            utils.retryAble = this.retryAble;
            utils.callback = this.callback;
            utils.context = this.context;
            utils.enableHttpDns = this.enableHttpDns;
            utils.header = this.header;
            utils.isCallbackInUiThread = this.isCallbackInUiThread;
            return utils;
        }

    }

    /**
     * TODO annotation
     * @param domain request url
     */
    public void postAsync(@Nullable String domain) {
        Net net = new Net(domain, params, ToolsConsent.HTTP_POST, contentType, accept, callback);
        net.setHttpDns(context, enableHttpDns);
        async(net);
    }


    /**
     * TODO annotation
     * @param domain Get request url
     *  callback Get request result callback, require {@link NonNull}
     *  waitSuccess if {@code waitSuccess} value was set as {@code true}, the request will be added in {@link NetworkSchedule}
     *                    and a unique thread wll keep trying to send get request with same params until the request success.
     *                    <b>You should expected callback may never called if this param is set as {@code true}</b>
     *                    since user may do not have network connection until he killed the app
     *                    if this value was set as {code false},this param will be ignored
     *
     */
    public void getAsync(@Nullable String domain) {
        Net net = new Net(domain, params, ToolsConsent.HTTP_GET, contentType, accept, callback);
        net.setHttpDns(context, enableHttpDns);
        async(net);
    }

    /**
     * Use a {@link #singleThreadExecutor} to send a network request if request not in {@link #requests} list, so requests will be executor in order.
     * <br>If request in {@link #requests} list, it will be ignore
     * <br>If same request already exist {@link #requests}, do nothing
     * @param net network request info, include url, params and so on, check {@link Net}
     */
    private void async(final Net net) {
        if(net.callback == null) return;
        boolean isInclude = true;
        synchronized (requests) {
            if (!requests.contains(net)) {
                isInclude = false;
                requests.add(net);
            }
        }
        if(!isInclude) {
            singleThreadExecutor.schedule(() -> doRequest(net), 10, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * Simply make a network request by the passed argument <b>net</b>, every time send a request,
     * it will try {@link #retryNum} times if network connection failed. After all the retries,
     * it add the request into {@link NetworkSchedule} or do nothing based on another argument <b>waitSuccess</b>
     * <br>Note: this method is not Thread safe, it should not call in <b>UI Thread</b>
     * @param net network request info, include url, params and so on, check {@link Net}
     */
    private void doRequest(Net net) {
        if (TextUtils.isEmpty(net.domain)) {
            Logger.warning("Request called but domain is empty, return %s", net.toString());
            return;
        }
        int retry = retryAble ? retryNum : 1;
        boolean isSuccess = false;
        while (retry > 0) {
            retry--;
            if (ToolsConsent.HTTP_GET.equals(net.method)) {
                isSuccess = get(net.domain, retry > 0, waitSuccess).second;
                if (isSuccess) break;
            } else if (ToolsConsent.HTTP_POST.equals(net.method)) {
                isSuccess = post(net.domain, retry > 0, waitSuccess).second;
                if (isSuccess) break;
            }
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Logger.warning("get retry sleep exception", e);
            }
        }
        synchronized (requests) {
            requests.remove(net);
        }
        if (waitSuccess && !isSuccess) {
            NetworkSchedule.add(net);
        }
    }

    public boolean get(@Nullable String domain) {
        return get(domain, retryAble, waitSuccess).second;
    }

    public byte[] getSync(@NonNull String domain) {
        return get(domain, retryAble, waitSuccess).first;
    }

    public boolean post(@Nullable String domain) {
        return post(domain, false, false).second;
    }

    public byte[] postSync(@Nullable String domain) {
        return post(domain, false, false).first;
    }

    /**
     * TODO annotation
     * @param domain   Get request url, e.g: http://domain/get/request
     * param    network params with sq format, you could check sq API document for params format
     *  callback Get result callback, require {@link NonNull}
     *  waitSuccess when this param set to true, callback will not calling onFailure if the request failed.
     *                    If request success, this param will not take any effect.
     * retryable when this param set to true, callback will not calling onFailure if the request failed.
     *                  If request success, this param will not take any effect.
     *                  Please note, this param is not doing actually retry even it set as true, it's just a flag.
     * @return return {@code true} if the get request success, otherwise return {@code false}
     */
    Pair<byte[], Boolean> get(@Nullable String domain, boolean retryable, boolean isWait) {
        if(callback == null) {
            Logger.warning("Get invoke with null callback, callback is a NonNull argument, check arguments passed in method");
            return new Pair<>(new byte[0], false);
        }
        if (TextUtils.isEmpty(domain)) {
            failure(callback, NetworkCode.EMPTY_DOMAIN, HTTP_EMPTY_DOMAIN, isWait, retryable);
            return new Pair<>(new byte[0], false);
        }
        HttpURLConnection conn;
        String finalDomain = TextUtils.isEmpty(params) ? domain : domain + "?" + params;
        if(enableHttpDns && context != null) {
            assert finalDomain != null;
            DnsParser parser = new DnsParser.Builder(context).build(finalDomain);
            finalDomain = parser.getSafeUrl();
        }
        Logger.debug(URL_IS, finalDomain);
        String host = KinaUtils.getHost(domain);
        try {
            URL url = new URL(finalDomain);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(ToolsConsent.HTTP_GET);
            conn.setRequestProperty("Host", host);
        } catch (Exception e) {
            if (failure(callback, NetworkCode.URL_BUILD_EXCEPTION, URL_BUILD_EXCEPTION, isWait, retryable))
                Logger.error("Exceptions happens when create URL connection, url: %s", domain, e);
            return new Pair<>(new byte[0], false);
        }

        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Accept", accept);
        conn.setConnectTimeout(ToolsConsent.CONN_TIMEOUT);
        conn.setDoOutput(false);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Charset", StandardCharsets.UTF_8.name());

        HashMap<String, String> mHeader = header == null ? null : header.toHeader();
        if (mHeader != null) {
            Logger.info("Request Header: %s, Default Header Host: %s, Content-Type: %s, Accept: %s", header.toString(), host, contentType, accept);
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        boolean isErrorStream = false;
        InputStream input = null;
        int code;
        byte[] data = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                input = conn.getInputStream();
            } else {
                input = conn.getErrorStream();
                isErrorStream = true;
            }
            int size;
            while ((size = input.read(data)) > 0) {
                baos.write(data, 0, size);
            }
        } catch (IOException e) {
            if (failure(callback, NetworkCode.RESPONSE_READ_EXCEPTION, RESPONSE_READ_EXCEPTION, isWait, retryable))
                Logger.error(RESPONSE_READ_EXCEPTION + " url: %s", domain, e);
            return new Pair<>(new byte[0], false);
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException e) {
                Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "GET InputStream", domain, e);
            }
            try {
                baos.close();
            } catch (IOException e) {
                Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "GET ByteArrayOutputStream", domain, e);
            }
        }

        //send callback
        if (isErrorStream) {
            String message = "ErrorStream: " + new String(data, 0, data.length, StandardCharsets.UTF_8);
            failure(callback, code, message, isWait, retryable);
            return new Pair<>(new byte[0], false);
        } else {
            Logger.verbose("Get %s return data %s", domain, baos.toString());
            success(callback, NetworkCode.SUCCESS, baos.toByteArray());
            return new Pair<>(baos.toByteArray(), true);
        }
    }


    /**
     * @param domain    Post request url
     *  params    network params with sq format, you could check sq API document for params format
     *  callback  Post result callback, require {@link NonNull}
     *  waitSuccess when this param set to true, callback will not calling onFailure if the request failed.
     *                    If request success, this param will not take any effect.
     *  retryable when this param set to true, callback will not calling onFailure if the request failed.
     *                  If request success, this param will not take any effect.
     *                  Please note, this param is not doing actually retry even it set as true, it's just a flag.
     * @return return {@code true} if the post request success, otherwise return {@code false}
     */
    @SuppressWarnings("ConstantConditions")
    Pair<byte[], Boolean> post(@Nullable String domain, boolean retryable, boolean isWait) {
        if(callback == null) {
            Logger.warning("post invoke with null callback, callback is a NonNull argument, check arguments passed in method");
            return new Pair<>(new byte[0], false);
        }
        if (TextUtils.isEmpty(domain)) {
            failure(callback, NetworkCode.EMPTY_DOMAIN, HTTP_EMPTY_DOMAIN, isWait, retryable);
            return new Pair<>(new byte[0], false);
        }
        String host = KinaUtils.getHost(domain);
        if(enableHttpDns && context != null) {
            assert domain != null;
            DnsParser parser = new DnsParser.Builder(context).build(domain);
            domain = parser.getSafeUrl();
        }
        Logger.info(URL_IS, domain);
        HttpURLConnection conn = null;
        try {
            //build http url connection
            URL url = new URL(domain);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Host", host);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Accept", accept);
            conn.setRequestMethod(ToolsConsent.HTTP_POST);
            conn.setReadTimeout(ToolsConsent.READ_TIMEOUT);
            conn.setConnectTimeout(ToolsConsent.CONN_TIMEOUT);
            HashMap<String, String> mHeader = header == null ? null : header.toHeader();
            if (mHeader != null) {
                Logger.info("Request Header: %s, Default Header Host: %s, Content-Type: %s, Accept: %s", header.toString(), host, contentType, accept);
                for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //set post request params
            Logger.info("Requested param: %s", params);
            if (!TextUtils.isEmpty(params)) {
                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(conn.getOutputStream());
                } catch (IOException e) {
                    Logger.warning(STREAM_BUILD_EXCEPTION + " url:%s", domain, e);
                }
                if (out != null) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    try {
                        writer.write(params);
                        writer.flush();
                    } catch (IOException e) {
                        Logger.warning("Exception happen when read http param in streams, url: %s", domain, e);
                    }
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "POST BufferWriter", domain, e);
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "POST BufferedOutputStream", domain, e);
                    }
                }
            }

            //connect url connection
            conn.connect();

            //start read data
            boolean isErrorStream = false;
            byte[] data = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream input = null;
            int responseCode = conn.getResponseCode();
            try {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    input = conn.getInputStream();
                } else {
                    input = conn.getErrorStream();
                    isErrorStream = true;
                }
                int size;
                while ((size = input.read(data)) > 0) {
                    baos.write(data, 0, size);
                }
            } catch (IOException e) {
                if (failure(callback, NetworkCode.RESPONSE_READ_EXCEPTION, RESPONSE_READ_EXCEPTION, isWait, retryable))
                    Logger.error(RESPONSE_READ_EXCEPTION + " url: %s", domain, e);
                return new Pair<>(new byte[0], false);
            } finally {
                try {
                    if (input != null) input.close();
                } catch (IOException e) {
                    Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "Respond InputStream", domain, e);
                }
                try {
                    baos.close();
                } catch (IOException e) {
                    Logger.warning(CLOSE_EXCEPTION_IS + " url: %s", "Reader ByteArrayOutputStream", domain, e);
                }
            }

            // return result
            if (isErrorStream) {
                String message = "ErrorStream: " + new String(data, 0, data.length, StandardCharsets.UTF_8);
                failure(callback, responseCode, message, isWait, retryable);
                return new Pair<>(new byte[0], false);
            } else {
                Logger.debug("Post %s server pure return data: %s", domain, baos.toString());
                success(callback, NetworkCode.SUCCESS, baos.toByteArray());
                return new Pair<>(baos.toByteArray(), true);
            }
        } catch (MalformedURLException e) {
            if (failure(callback, NetworkCode.URL_BUILD_EXCEPTION, URL_BUILD_EXCEPTION, isWait, retryable))
                Logger.error(URL_BUILD_EXCEPTION + " url: %s", domain, e);
        } catch (ProtocolException e) {
            if (failure(callback, NetworkCode.HTTP_METHOD_SET_EXCEPTION, METHOD_SET_EXCEPTION, isWait, retryable))
                Logger.error(METHOD_SET_EXCEPTION + " url: %s", domain, e);
        } catch (IOException e) {
            if (failure(callback, NetworkCode.URL_CONNECTION_OPEN_EXCEPTION, URL_CONNECTION_EXCEPTION, isWait, retryable))
                Logger.error(URL_CONNECTION_EXCEPTION + " url: %s", domain, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return new Pair<>(new byte[0], false);
    }

    /**
     * A private common onFailure method. The boolean argument decide the failure callback will be processed or not
     * Other arguments is the result content of the result callback.
     *
     * @param callback Network result callback, require {@link NonNull}
     * @param code result code for request result
     * @param message result message for request result
     * @param waitSuccess the waitSuccess flag, if this flag is {@code true}, onFailure will never be processed
     * @param retryable the retryable flag,  if this flag is {@code true}, onFailure will never be processed
     * @return if callback onFailure was invoked or not
     */
    private boolean failure(KinaCallback callback, int code, String message, boolean waitSuccess, boolean retryable) {
        if(retryable || waitSuccess) {
            if(retryable) Logger.warning("Request failed but still have chance to retry, failure des: %s", message);
            if(waitSuccess) Logger.warning("Request failed, but wait success enable, will try request later, failure des: %s", message);
            return false; // Do not failure the callback if we have times to retry
        }
        if(!isCallbackInUiThread) {
            callback.onFailure(code, message);
            return true;
        }
        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(code, message));
        return true;
    }

    private void success(KinaCallback callback, int code, byte[] data) {
        if(!isCallbackInUiThread) {
            callback.onSuccess(code, data);
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(code, data));
    }

}
