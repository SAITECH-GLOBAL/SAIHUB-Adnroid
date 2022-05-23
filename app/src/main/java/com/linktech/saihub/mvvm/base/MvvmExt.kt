package com.linktech.saihub.mvvm.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.net.BaseResp
import com.linktech.saihub.net.ex.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * 显示页面状态，这里有个技巧，成功回调在第一个，其后两个带默认值的回调可省
 * @param viewState 接口返回值
 * @param onLoading 加载中
 * @param onSuccess 成功回调
 * @param onError 失败回调
 *
 */
fun <T> BaseActivity.parseState(
    viewState: ViewState<T>,
    onSuccess: (T) -> Unit,
    onError: ((ApiException) -> Unit)? = null,
    onLoading: (() -> Unit)? = null
) {
    when (viewState) {
        is ViewState.Loading -> {
            showLoading()
            onLoading?.run { this }
        }
        is ViewState.Success -> {
            hideLoading()
            onSuccess(viewState.data)
        }
        is ViewState.Error -> {
            hideLoading()
            onError?.run { this(viewState.error) }
        }
    }
}

fun <T> BaseFragment.parseState(
    viewState: ViewState<T>,
    onSuccess: (T) -> Unit,
    onError: ((ApiException) -> Unit)? = null,
    onLoading: (() -> Unit)? = null
) {
    (activity as? BaseActivity)?.parseState(viewState, onSuccess, onError, onLoading)
}


fun Throwable?.parseErrorString(): String {
    return when (this) {
        is ConnectException -> "连接有问题"
        is UnknownHostException -> "未知主机"
        else -> "服务器错误"
    }
}


@MainThread
inline fun <T> VmLiveData<T>.vmObserver(owner: LifecycleOwner, vmResult: VmResult<T>.() -> Unit) {
    val result = VmResult<T>();
    result.vmResult();
    observe(owner = owner) {
        when (it) {
            is VmState.Loading -> {
                result.onAppLoading()
            }
            is VmState.Success -> {
                result.onAppSuccess(it.data)
                result.onAppComplete()
            }
            is VmState.Error -> {
                result.onAppError(it.error)
                result.onAppComplete()
            }
        }
    }
}

/**
 * net request
 * @param request request method
 * @param viewState request result
 * @param showLoading 配置是否显示等待框
 */
@Deprecated("回调麻烦了点", ReplaceWith(expression = "launchVmRequest"))
fun <T> BaseViewModel.launchRequest(
    request: suspend () -> BaseResp<T>,
    viewState: MutableLiveData<ViewState<T>>,
    showLoading: Boolean = true
) {
    viewModelScope.launch {
        runCatching {
            if (showLoading) viewState.value = ViewState.onAppLoading()
            request()
        }.onSuccess {
            viewState.paresResult(it)
        }.onFailure {
            viewState.paresException(it)
        }
    }
}


/**
 * net request
 * @param request request method
 * @param viewState request result
 */
fun <T> BaseViewModel.launchVmRequest(
    request: suspend () -> BaseResp<T>,
    viewState: VmLiveData<T>
) {
    viewModelScope.launch {
        runCatching {
            viewState.value = VmState.Loading
            request()
        }.onSuccess {
            viewState.paresVmResult(it)
        }.onFailure {
            viewState.paresVmException(it)
        }
    }
}

/**
 * net request
 * @param request request method
 */
fun <T> BaseViewModel.launchRequestNoState(
    request: suspend () -> BaseResp<T>
) {
    viewModelScope.launch {
        runCatching {
            request()
        }
    }
}


/**
 * 以协程形式执行
 */
fun BaseViewModel.launchBlock(block: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) { block() }
}