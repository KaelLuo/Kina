package com.kael.kina.annotation;

import androidx.annotation.RestrictTo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 此 Annotation 修饰的数据，会根据构造函数传入的 JSON 数据进行初始化。其初始化的值等于从 {@link JsonResponse#value()}
 * 位置读取到的值。
 * </p>
 * <p>
 * 若无法读取则会尝试从 {@link JsonResponse#replacement()} 读取。
 * </p>
 * <p>
 * 若依旧无法获取，其值是 {@code null}
 * </p>
 * <p>
 * 此 Annotation 需要配合 {@link ResponseTools} 使用
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public @interface JsonResponse {

    /**
     * @return 数据在 JSON 结构中的位置
     */
    String[] value();

    /**
     * @return 数据 Key 值的备选值，若在上述位置中找不到需要解析的 JSON, 程序会自动在相同位置遍历解析此值所指定的 Key 来尝试解析。
     */
    String[] replacement() default "";
}
