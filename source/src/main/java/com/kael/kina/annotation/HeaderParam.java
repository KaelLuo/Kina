package com.kael.kina.annotation;

import androidx.annotation.RestrictTo;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     以此 Annotation 修饰的变量，会在网络请求中添加进<b>请求参数</b>中。
 * </p>
 * <p>
 *     此 Annotation 需要配合 {@link RequestTools} 使用。
 * </p>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public @interface HeaderParam {
    /**
     *  组装成请求的的 Json key 字段.
     */
    String value() default "";
}
