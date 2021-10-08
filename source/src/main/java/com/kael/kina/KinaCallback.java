package com.kael.kina;

import androidx.annotation.RestrictTo;

import com.kael.kina.constant.NetworkCode;


/**
 * <p> 网络回调接口 </p>
 * <p> 此接口中的 {@link #onSuccess(int, byte[])} 仅仅表示网络请求成功， 具体 API 是否调用成功还需要根据返回数据判断。 </p>
 * <p>
 *     此接口中的 {@link #onFailure(int, String)} 表示网络请求失败,
 *     {@code code} 在{@link #onSuccess(int, byte[])} 表示返回的 http code,
 *     在 {@link #onFailure(int, String)} 表示 {@link NetworkCode} 中定义的错误码
 * </p>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface KinaCallback {

    void onSuccess(int code, byte[] data);

    void onFailure(int code, String message);
}
