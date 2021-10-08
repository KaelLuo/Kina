package com.kael.kina.proxy;

import android.text.TextUtils;

import androidx.annotation.RestrictTo;


import com.kael.kina.tools.Logger;
import com.kael.kina.annotation.JsonResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 此类会对继承该类的、且具有 {@link JsonResponse} 标记的 成员变量 通过 {@link #initSelfByJson(JSONObject)}
 * 以及其多态方法传递过来的参数进行赋值。
 * </p>
 * <p>
 * 若指定的值在给定 json 中不存在，则不会对该值做任何动作
 * </p>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
public abstract class ResponseTools {

    protected void initSelfByString(String data) {
        if (TextUtils.isEmpty(data)) return;
        try {
            JSONObject jsonData = new JSONObject(data);
            initSelfByJson(jsonData);
        } catch (JSONException e) {
            Logger.warning("Passed String can not convert to Json in class: %s", this.getClass().getName(), e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void initSelfByJson(JSONObject json) {
        if (json == null) return;
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(this.getClass().getFields()));
        fields.addAll(Arrays.asList(this.getClass().getInterfaces().getClass().getDeclaredFields()));

        for (Field field : fields) {
            JsonResponse response = field.getAnnotation(JsonResponse.class);
            if (response == null) continue;
            String[] nests = response.value();
            if (nests == null || nests.length == 0) continue;
            JSONObject nakJson = json;
            int i = 0;
            for (; i < nests.length - 1; i++) {
                if (nakJson != null && nakJson.has(nests[i])) {
                    nakJson = nakJson.optJSONObject(nests[i]);
                } else {
                    Logger.warning("Can not parse field: %s under json name: %s in class: %s", field.getName(), nests[i], this.getClass().getName());
                    break;
                }
            }
            if (i != nests.length - 1 || nakJson == null) continue;
            String key = nests[i];
            if (!nakJson.has(key)) {
                String[] replaces = response.replacement();
                if (replaces.length > 0 && !replaces[0].isEmpty()) {
                    for (String replace : replaces) {
                        if (nakJson.has(replace)) {
                            key = replace;
                            break;
                        }
                    }
                }
            }
            if (!nakJson.has(key)) continue; //if key still not exist after search replacement, skip
            String type = field.getGenericType().toString();
            field.setAccessible(true);
            try {
                switch (type) {
                    case "class java.lang.String":
                        String value = nakJson.getString(key);
                        if (!TextUtils.isEmpty(value)) field.set(this, value);
                        break;

                    case "int":
                    case "class java.lang.Integer":
                        int intValue = nakJson.getInt(key);
                        field.set(this, intValue);
                        break;

                    case "float":
                    case "class java.lang.Float":
                        float floatValue = (float) nakJson.getDouble(key);
                        field.set(this, floatValue);
                        break;

                    case "double":
                    case "class java.lang.Double":
                        double doubleValue = nakJson.getDouble(key);
                        field.set(this, doubleValue);
                        break;

                    case "boolean":
                    case "class java.lang.Boolean":
                        boolean bValue = nakJson.getBoolean(key);
                        field.set(this, bValue);
                        break;

                    case "class org.json.JSONArray":
                        JSONArray arrayValue = nakJson.getJSONArray(key);
                        field.set(this, arrayValue);
                        break;

                    default:
                        Logger.warning("Do not support type %s of JsonResponse", type);
                        break;
                }
            } catch (JSONException e) {
                Logger.warning("Exception happen when parse field: %s under json name: %s in class: %s", field.getName(), key, this.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                Logger.warning("Exception happen when set filed %s", field.getName(), e);
            }
        }
    }
}
