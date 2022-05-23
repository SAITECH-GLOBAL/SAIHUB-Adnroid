package com.linktech.saihub.mvvm.base

import com.linktech.saihub.net.ex.ApiException

/**
 * Created by tromo on 2021/8/29.
 */
sealed class ViewState<out T> {
    companion object {
        fun <T> onAppSuccess(data: T): ViewState<T> = Success(data)
        fun <T> onAppLoading(): ViewState<T> = Loading
        fun <T> onAppError(error: ApiException): ViewState<T> = Error(error)
        fun <T> onAppFail(throwable: Throwable): ViewState<T> = Fail(throwable)
    }

    object Loading : ViewState<Nothing>()
    data class Success<out T>(val data: T) : ViewState<T>()
    data class Error(val error: ApiException) : ViewState<Nothing>()
    data class Fail(val throwable: Throwable) : ViewState<Nothing>()
}

class VmResult<T> {
    var onAppSuccess: (data: T) -> Unit = {}
    var onAppError: (ApiException) -> Unit = {}
    var onAppFail: (Throwable) -> Unit = {}
    var onAppLoading: () -> Unit = {}
    var onAppComplete: () -> Unit = {}
}

sealed class VmState<out T> {
    object Loading : VmState<Nothing>()
    data class Success<out T>(val data: T) : VmState<T>()
    data class Error(val error: ApiException) : VmState<Nothing>()
    data class Fail(val throwable: Throwable) : VmState<Nothing>()
}