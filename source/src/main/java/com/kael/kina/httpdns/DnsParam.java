package com.kael.kina.httpdns;



import com.kael.kina.annotation.FormatType;
import com.kael.kina.annotation.RequestParam;
import com.kael.kina.constant.ToolsConsent;
import com.kael.kina.proxy.RequestTools;
import com.kael.kina.tools.DES;
import com.kael.kina.tools.KinaUtils;

import java.nio.charset.StandardCharsets;

public class DnsParam extends RequestTools {

    @RequestParam("dn") public String domain;
    @RequestParam("id") public String id;
    @RequestParam("alg") public String algorithm;
    @RequestParam("timeout") public String timeout;


    public DnsParam(String domain, String id, String key) {
        contentType = FormatType.FORM;
        timeout = String.valueOf(ToolsConsent.DNS_TIMEOUT);
        this.id = id;
        this.domain = KinaUtils.bytesToHex(DES.encrypt(domain.getBytes(StandardCharsets.UTF_8), key));
        this.algorithm = "des";
    }

}
