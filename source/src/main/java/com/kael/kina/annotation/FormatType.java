package com.kael.kina.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ContentType {

    String JSON = "application/json";

    String FORM = "application/x-www-form-urlencoded";


    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @StringDef({JSON, FORM})
    @interface Type {
    }
}
