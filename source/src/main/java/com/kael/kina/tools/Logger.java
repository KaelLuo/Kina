package com.kael.kina.tools;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;


import com.kael.kina.annotation.LogLevel;

import java.net.UnknownHostException;


/**
 * <p> 自定义的日志类，对原有日志显示形式进行了优化，可通过 IDE 快速定位日志代码。且支持 C++ 形式的 String Format </p>
 * <p> 此日志默认情况下只开启 {@link Log#WARN} 及以上级别的日志，若需要开启更低级别的日志，请使用 {@link #setLogEnable(boolean)} 激活。
 * <br>若想关闭 {@link Log#WARN} 级别以上的日志，需要手动调用 {@link #disableError(boolean)} 禁用。 </p>
 *
 * <p> 使用场景:
 * <ol>
 * 默认 TAG 日志:
 * <blockquote> {@code Logger.verbose("This is a message to log")} </blockquote>
 * <blockquote> {@code Logger.verbose("This is a message to log", exception)} </blockquote>
 * <blockquote> {@code Logger.verbose("This is a %s %s", "message", "to log")} </blockquote>
 * <blockquote> {@code Logger.verbose("This is a %s %s", "message", "to log", exception)} </blockquote>
 * </ol>
 * <ol>
 * 定制 TAG 日志:
 * <blockquote> {@code Logger.verbose(Logger.tag("CustomTag"), "This is a message to log")} </blockquote>
 * <blockquote> {@code Logger.verbose(Logger.tag("CustomTag"), "This is a message to log", exception)} </blockquote>
 * <blockquote> {@code Logger.verbose(Logger.tag("CustomTag"), "This is a %s %s", "message", "to log")} </blockquote>
 * <blockquote> {@code Logger.verbose(Logger.tag("CustomTag"), "This is a %s %s", "message", "to log", exception)} </blockquote>
 * </ol>
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
public abstract class Logger {

    private static String globalTag = "kina_tag";

    @LogLevel
    private static int logLevel = Log.VERBOSE;
    private static volatile boolean isDebug = false;
    private static volatile boolean isError = true;

    public static Tag tag(@NonNull String tag) {
        return new Tag(tag);
    }

    public static void disableError(boolean disable) {
        isError = !disable;
    }

    public static void setLogEnable(boolean enable) {
        isDebug = enable;
    }

    public static void setLogLevel(@LogLevel int level) {
        logLevel = level;
    }

    public static void setTag(@NonNull String tag) {
        if (!TextUtils.isEmpty(tag)) globalTag = tag;
    }

    public static boolean isVerboseEnabled() {
        return isDebug && logLevel <= Log.VERBOSE;
    }

    public static boolean isDebugEnabled() {
        return isDebug && logLevel <= Log.DEBUG;
    }

    public static boolean isInfoEnabled() {
        return isDebug && logLevel <= Log.INFO;
    }

    public static boolean isWarningEnabled() {
        return isError && logLevel <= Log.WARN;
    }

    public static boolean isErrorEnabled() {
        return isError && logLevel <= Log.ERROR;
    }

    public static void verbose(String message, Object... params) {
        if (isVerboseEnabled()) {
            doLog(Log.VERBOSE, null, message, params);
        }
    }

    public static void verbose(Tag tag, String message, Object... params) {
        if (isVerboseEnabled()) {
            doLog(Log.VERBOSE, tag.tag, message, params);
        }
    }

    public static void debug(String message, Object... params) {
        if (isDebugEnabled()) {
            doLog(Log.DEBUG, null, message, params);
        }
    }

    public static void debug(Tag tag, String message, Object... params) {
        if (isDebugEnabled()) {
            doLog(Log.DEBUG, tag.tag, message, params);
        }
    }

    public static void info(String message, Object... params) {
        if (isInfoEnabled()) {
            doLog(Log.INFO, null, message, params);
        }
    }

    public static void info(Tag tag, String message, Object... params) {
        if (isInfoEnabled()) {
            doLog(Log.INFO, tag.tag, message, params);
        }
    }

    public static void warning(String message, Object... params) {
        if (isWarningEnabled()) {
            doLog(Log.WARN, null, message, params);
        }
    }

    public static void warning(Tag tag, String message, Object... params) {
        if (isWarningEnabled()) {
            doLog(Log.WARN, tag.tag, message, params);
        }
    }

    public static void error(String message, Object... params) {
        if (isErrorEnabled()) {
            doLog(Log.ERROR, null, message, params);
        }
    }

    public static void error(Tag tag, String message, Object... params) {
        if (isErrorEnabled()) {
            doLog(Log.ERROR, tag.tag, message, params);
        }
    }

    private static void doLog(@LogLevel int level, String tag, String message, Object[] params) {
        Throwable throwable = extractThrowable(params);
        if (throwable != null) {
            params = trimParams(params);
        }


        String formattedMsg = "";
        if (message != null && params != null) {
            formattedMsg = String.format(message, params);
        } else if (message != null) {
            formattedMsg = message;
        }

        String msgWithTrace = formatTrace(formattedMsg);

        String finalTag = tag != null ? formatTag(tag) : formatTag(globalTag);

        if (throwable == null) {
            androidLog(level, finalTag, msgWithTrace);
        } else {
            //fix android log bug when meet UnknowHostException, this could be removed in the future when android team fix this bug
            if (throwable instanceof UnknownHostException) {
                msgWithTrace = msgWithTrace + "\nException: " + throwable.toString();
            }
            androidLog(level, finalTag, msgWithTrace, throwable);
        }
    }

    private static void androidLog(@LogLevel int level, String finalTag, String msgWithTrace) {
        switch (level) {
            case Log.VERBOSE:
                Log.v(finalTag, msgWithTrace);
                break;
            case Log.DEBUG:
                Log.d(finalTag, msgWithTrace);
                break;
            case Log.INFO:
                Log.i(finalTag, msgWithTrace);
                break;
            case Log.WARN:
                Log.w(finalTag, msgWithTrace);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(finalTag, msgWithTrace);
                break;
        }
    }

    private static void androidLog(@LogLevel int level, String finalTag, String msgWithTrace, Throwable throwable) {
        switch (level) {
            case Log.VERBOSE:
                Log.v(finalTag, msgWithTrace, throwable);
                break;
            case Log.DEBUG:
                Log.d(finalTag, msgWithTrace, throwable);
                break;
            case Log.INFO:
                Log.i(finalTag, msgWithTrace, throwable);
                break;
            case Log.WARN:
                Log.w(finalTag, msgWithTrace, throwable);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(finalTag, msgWithTrace, throwable);
                break;
        }
    }

    private static Throwable extractThrowable(Object[] params) {
        if (params == null || params.length == 0) return null;

        Object last = params[params.length - 1];

        return last instanceof Throwable ? (Throwable) last : null;
    }

    private static Object[] trimParams(Object[] params) {
        if (params == null || params.length == 0) {
            throw new IllegalArgumentException("params is null or empty");
        }

        Object[] trimmedParams = new Object[params.length - 1];
        System.arraycopy(params, 0, trimmedParams, 0, trimmedParams.length);

        return trimmedParams;
    }

    private static String formatTrace(String message) {
        return formatTrace(message, 4);
    }

    @SuppressWarnings("SameParameterValue")
    private static String formatTrace(String message, int level) {
        Throwable throwable = new Throwable();
        StackTraceElement traceElement = throwable.getStackTrace()[Math.min(level, throwable.getStackTrace().length - 1)]; // to prevent crashing when inlined by Proguard
        return "[(" + traceElement.getFileName() + ":" + traceElement.getLineNumber() + "): " + traceElement.getMethodName() + "()]: " + message;
    }

    private static String formatTag(String tag) {
        return tag + ": " + Thread.currentThread().getName();
    }

    private static class Tag {
        public String tag;

        public Tag(@NonNull String tag) {
            this.tag = tag;
        }
    }

}