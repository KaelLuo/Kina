package com.kael.kina.annotation;

import android.util.Log;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 以 {@link IntDef} 方式实现的 LogLevel 枚举
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT})
public @interface LogLevel {
}
