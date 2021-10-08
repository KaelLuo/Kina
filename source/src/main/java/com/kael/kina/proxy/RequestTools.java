package com.kael.kina.proxy;


import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.kael.kina.tools.Logger;
import com.kael.kina.annotation.ContentType;
import com.kael.kina.annotation.RequestParam;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 此类提供的方法可将继承该类的并且具有 {@link RequestParam} 标记的成员变量生成不同类型的网络请求参数。
 * </p>
 * <p>
 * 使用此类时，请继承该类，并根据需求复写 {@link RequestTools#toPostParam(Context)} 等
 * </p>
 * <p>
 * 此类中的对应方法均为 {@code protected} 类型，其不希望外部直接调用，而需由其继承类再度封装。
 * </p>
 * <p>
 * {@link #toPostParam(Context)} 等方法，会将提供的对象中，所有非 {@code null} 的值封装成 json 类型的请求参数
 * </p>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@SuppressWarnings({"WeakerAccess"})
public abstract class RequestTools {

    @ContentType.Type public String contentType = ContentType.JSON;
    protected boolean isSign = true;

    protected HashMap<String, String> transparent;

    public String toPostParam(Context context) {
        if (ContentType.FORM.equals(contentType)) {
            return toFormParam(context);
        } else {
            return toJsonParam(context);
        }
    }

    public String toGetParam(Context context) {
        // we are using same format for post & get param for now
        return toPostParam(context);
    }

    protected String toFormParam(Context context) {
        JSONObject dataJson = generateJsonByFields(transparent);
        Iterator<String> keys = dataJson.keys();
        StringBuilder sb = new StringBuilder();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                sb.append(key).append("=").append(dataJson.getString(key)).append("&");
            } catch (JSONException e) {
                Logger.warning("RequestTools to Form Param Exception", e);
            }
        }
        sb.setLength(sb.length() - 1);
        if (isSign) {
            //TODO sign
//            String sign = EncryptApi.sign(context, dataJson);
//            sb.append("&sign=").append(sign);
        }
        return sb.toString();
    }

    @SuppressWarnings("SameParameterValue")
    protected final String toJsonParam(Context context) {
        JSONObject dataJson = generateJsonByFields(transparent);
        if(!isSign) return dataJson.toString();
        //TODO sign
//        try {
//            dataJson.put("sign", EncryptApi.sign(context, dataJson));
//        } catch (JSONException e) {
//            Logger.error("Put Sign in Json failed, this could cause network request fail");
//        }
        return dataJson.toString();
    }


    @NonNull private JSONObject generateJsonByFields(@Nullable HashMap<String, String> transparent) {
        // read all fields to List, include super classes public fields and interface
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(this.getClass().getFields()));
        fields.addAll(Arrays.asList(this.getClass().getInterfaces().getClass().getDeclaredFields()));
        //start put field with RequestParam to json
        JSONObject json = new JSONObject();
        //put transparent map data to json first
        if(transparent != null && transparent.size() != 0) {
            for(HashMap.Entry<String, String> entry: transparent.entrySet()) {
                if(TextUtils.isEmpty(entry.getKey())) continue;
                try {
                    json.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
                } catch (JSONException e) {
                    Logger.error("Put values from transparent map in JSONObject", e);
                }
            }
        }
        //put data from fields to json
        for (Field field : fields) {
            // check has RequestParam or not
            RequestParam request = field.getAnnotation(RequestParam.class);
            if(request == null) continue;
            //check field value set or not
            field.setAccessible(true);
            Object o = null;
            try {
                o = field.get(this);
            } catch (IllegalAccessException e) {
                // ignored
            }
            if(o == null) continue;
            //put value to JSONObject
            String value = request.value();
            if(TextUtils.isEmpty(value)) value = field.getName();
            try {
                json.put(value, o);
            } catch (JSONException e) {
                Logger.error("object with %s marked can not put in JSONObject", e);
            }
        }
        return json;
    }

    protected boolean isReady() {
        return true;
    }

    protected void async(RequestAsync callback) {
        callback.onReady();
    }

    public interface RequestAsync {
        void onReady();
    }

}
