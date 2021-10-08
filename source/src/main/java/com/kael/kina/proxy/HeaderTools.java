package com.kael.kina.proxy;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kael.kina.annotation.HeaderParam;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO annotation
 */
public abstract class HeaderTools {

    protected HashMap<String, String> transparent;


    public HashMap<String, String> toHeader() {
        return generateMapByFields(transparent);
    }

    @NonNull
    private HashMap<String, String> generateMapByFields(@Nullable HashMap<String, String> transparent) {
        // read all fields to List, include super classes public fields and interface
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(this.getClass().getFields()));
        fields.addAll(Arrays.asList(this.getClass().getInterfaces().getClass().getDeclaredFields()));
        //start put field with RequestParam to json
        HashMap<String, String> data = transparent == null ? new HashMap<>() : new HashMap<>(transparent);

        //put data from fields to json
        for (Field field : fields) {
            // check has HeaderParam or not
            HeaderParam request = field.getAnnotation(HeaderParam.class);
            if(request == null) continue;
            //check field value set or not
            field.setAccessible(true);
            Object o = null;
            try {
                o = field.get(this);
            } catch (IllegalAccessException ignored) {}

            if(o == null) continue;
            //put value to JSONObject
            String value = request.value();
            if(TextUtils.isEmpty(value)) value = field.getName();
            data.put(value, o.toString());
        }
        return data;
    }

    @NonNull @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry: toHeader().entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        return sb.toString();
    }
}
