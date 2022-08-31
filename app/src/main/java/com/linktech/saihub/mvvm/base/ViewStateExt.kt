package com.linktech.saihub.mvvm.base

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.linktech.saihub.entity.event.ErrorEvent
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_REFRESH_TOKEN_LN_WALLET
import com.linktech.saihub.net.BaseLnResp
import com.linktech.saihub.net.BaseResp
import com.linktech.saihub.net.ex.ApiException
import org.greenrobot.eventbus.EventBus
import retrofit2.HttpException

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

fun <T> VmLiveData<T>.paresVmNewResult(result: T) {
    value = if (result is BaseLnResp)
        if (TextUtils.isEmpty(result.message)) {
            VmState.Success(result)
        } else {
            if (result.error == true) {
                //认证失败重新获取token
                if (result.message == "bad auth" && result.code == 1) {
                    EventBus.getDefault().post(
                        MessageEvent.getInstance(MESSAGE_ID_REFRESH_TOKEN_LN_WALLET)
                    )
                }
                VmState.Error(ApiException(result.code!!, result.message))
            } else {
                VmState.Success(result)
            }
        }
    else
        VmState.Success(result)
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

/**
 * 异常转换异常处理
 */
fun <T> VmLiveData<T>.paresVmNewException(e: Throwable) {
    if (e is HttpException && e.code() == 400 && !TextUtils.isEmpty(
            e.response()?.errorBody()?.string()
        )
    ) {
        runCatching {
            Gson().fromJson(e.response()?.errorBody()?.string(), BaseLnResp::class.java)
        }.onSuccess {
            this.value = VmState.Error(ApiException(it.code!!, it.message))
        }.onFailure {
            this.value = VmState.Error(ApiException(e))
        }
    } else {
        this.value = VmState.Error(ApiException(e))
    }
}