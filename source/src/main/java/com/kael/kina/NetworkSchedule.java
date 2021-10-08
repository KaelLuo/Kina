package com.kael.kina;


import android.text.TextUtils;


import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.tools.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class maintains a {@link Thread} that will keeping doing network request time by time
 * once there are requests storage in {@link NetworkSchedule#nets}.
 */
class NetworkSchedule {

    private static int QUERY_INTERVAL = 5000;
    private final static List<Net> nets = new ArrayList<>();
    private static final ScheduledExecutorService singleThreadExecutor = Executors.newSingleThreadScheduledExecutor();
    private static volatile boolean isInit;

    private static void init() {
        if(isInit) return;
        singleThreadExecutor.scheduleAtFixedRate(() -> {
            if(nets.size() <= 0) return;
            for(int i = 0; i < nets.size(); i++) {
                Net net = nets.get(i);
                // default request success, so if the method is something we don't support
                // the request will be remove by default
                boolean isRequestSuccess = true;
                if (ToolsConsent.HTTP_POST.equals(net.method)) {
                    isRequestSuccess = new Kina.Builder()
                            .setParams(net.toParams())
                            .setCallback(net.callback)
                            .setAccept(net.accept)
                            .setContentType(net.contentType)
                            .setHttpDns(net.context, net.enableHttpDns)
                            .build().post(net.domain, false, true).second;
                } else if (ToolsConsent.HTTP_GET.equals(net.method)) {
                    isRequestSuccess = new Kina.Builder()
                            .setParams(net.toParams())
                            .setCallback(net.callback)
                            .setAccept(net.accept)
                            .setHttpDns(net.context, net.enableHttpDns)
                            .setContentType(net.contentType)
                            .build().get(net.domain, false, true).second;
                } else {
                    Logger.error("Unsupported query request type: %s for url: %s", net.method, net.domain);
                }
                if (isRequestSuccess) {
                    synchronized (nets) {
                        nets.remove(net);
                        i--;
                    }
                }
            }
        }, 1, QUERY_INTERVAL, TimeUnit.MILLISECONDS);
        isInit = true;
    }

    /**
     * Add a request to {@link NetworkSchedule#nets} and start the request thread if there is no running request thread
     * @param ntr the basic request values
     */
    synchronized static void add(Net ntr) {
        // It make no sense to schedule require it, if domain is empty or this request does not have callback
        if(ntr == null || TextUtils.isEmpty(ntr.domain) || ntr.callback == null) return;
        synchronized (nets) {
            for (Net net : nets) {
                if (ntr.equals(net)) {
                    return;
                }
            }
            nets.add(ntr);
            if(!isInit) init();
        }
    }

    /**
     * This method should only use in unit test. Do not call this method anywhere else.
     */
    static void setQueryInterval(int interval) {
        QUERY_INTERVAL = interval;
    }

}
