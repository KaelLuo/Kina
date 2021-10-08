package com.kael.kina.constant;

import androidx.annotation.RestrictTo;


/**
 * All Network related Error code define here and with prefix 20, basically there are three types of Error:
 * 1. Client Error {@link #CLIENT_ERROR}:
 * from 600 to 699, include all the client exception, or http implement error.
 * A Client Error usually means code implement is not correct, and should not happen in release environment
 * All Client Error should covered by client side when implement features.
 * A good practices to throw client error is to let developer know it happens on which line
 *
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) @SuppressWarnings({"WeakerAccess"})
public abstract class NetworkCode {

    //No Error
    public static final int SUCCESS = 200;
    public static final int RESPOND_OK = 1;
    public static final int RESPOND_LOCKED = 1053;  // server locked this request, we can usually try it later
    public static final int CLIENT_ERROR_UNDEFINE = 20911;// This error happen if error code in UserInfo haven't init
    public static final int UN_CATCH_EXCEPTION = 20119;

    //Client Error
    public static final int CLIENT_ERROR = 20600;
    public static final int URL_BUILD_EXCEPTION = 20602;
    public static final int URL_CONNECTION_OPEN_EXCEPTION = 20610;
    public static final int URL_CONNECTION_CONNECT_EXCEPTION = 20611;
    public static final int RESPONSE_READ_EXCEPTION = 20612;
    public static final int RESPONSE_EMPTY = 20613;
    public static final int HTTP_METHOD_SET_EXCEPTION = 20615;
    public static final int EMPTY_DOMAIN = 20620;
    public static final int PARSE_JSON_EXCEPTION = 20631;
    public static final int PERMISSION_MISS = 20641;
    public static final int NOT_SUPPORT_DEVICE = 20660;
    public static final int OAID_ERROR = 20661;
    public static final int PARAM_NOT_READY = 20662;

}
