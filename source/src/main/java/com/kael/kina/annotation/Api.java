package com.kael.kina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示此注解修饰的方法会被其他项目或者外部团队调用.
 * <br>修改带有此注解的函数返回，请求参数时，<b>请谨慎</b>
 * <br>此注解只修饰 {@code public} 方法
 * @version  version 默认值表示支持该 API 的最低 SDK 版本
 */
@Retention(RetentionPolicy.SOURCE) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Api {
    String version() default "0.0.1";
}
