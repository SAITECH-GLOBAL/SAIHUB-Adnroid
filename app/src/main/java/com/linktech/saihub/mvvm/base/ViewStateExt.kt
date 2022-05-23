package com.linktech.saihub.mvvm.base

import androidx.lifecycle.MutableLiveData
import com.linktech.saihub.net.BaseResp
import com.linktech.saihub.net.ex.ApiException

/**
 * Created by tromo on 2021/8/29.
 */
/**
 * 处理返回值
 *
 * @param result 请求结果
 */
fun <T> MutableLiveData<ViewState<T>>.paresResult(result: BaseResp<T>) {
    value =
        if (result.code == 0 && result.data?.status == 0) ViewState.onAppSuccess(result.data?.result!!) else
            ViewState.onAppError(ApiException(result.data?.status!!, result.data?.message))
}

/**
 * 处理返回值
 *
 * @param result 请求结果
 */
fun <T> VmLiveData<T>.paresVmResult(result: BaseResp<T>) {
    value =
        if (result.code == 0 && result.data?.status == 0) VmState.Success(result.data?.result!!) else
            VmState.Error(ApiException(result.data?.status!!, result.data?.message))
}

/**
 * 异常转换异常处理
 */
fun <T> MutableLiveData<ViewState<T>>.paresException(e: Throwable) {
    this.value = ViewState.onAppError(ApiException(e))
}

/**
 * 异常转换异常处理
 */
fun <T> VmLiveData<T>.paresVmException(e: Throwable) {
    this.value = VmState.Error(ApiException(e))
}