package com.linktech.saihub.net.ex

import android.accounts.NetworkErrorException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.linktech.saihub.R
import com.linktech.saihub.app.SaiHubApplication
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * Created by tromo on 2021/7/27.
 */
class ApiException : RuntimeException {

    var errorMsg: String = ""


    constructor(resultCode: Int, msg: String?) {
        errorMsg = getApiExceptionMessage(resultCode, msg).toString()
    }

    constructor(msg: String?) {
        errorMsg = msg.toString()
    }

    constructor(throwable: Throwable?) : super(throwable) {
        errorMsg = parseError(throwable)
    }

    @JvmName("getErrorMsg1")
    fun getErrorMsg(): String {
        return errorMsg
    }

    private fun parseError(throwable: Throwable?): String {
        throwable?.printStackTrace()
        when (throwable) {
//            is HttpException -> {
//                catchHttpException(e.code())
//            }
            is ConnectException -> {
                return SaiHubApplication.getInstance()?.getString(R.string.error_tip_net)!!
            }
            is SocketTimeoutException -> {
                return SaiHubApplication.getInstance()?.getString(R.string.error_tip_time_out)!!
            }
            is UnknownHostException, is NetworkErrorException -> {
                return SaiHubApplication.getInstance()?.getString(R.string.error_tip_net)!!
            }
            is MalformedJsonException, is JsonSyntaxException -> {
                return SaiHubApplication.getInstance()?.getString(R.string.error_tip_json)!!
            }
            else -> {
                return SaiHubApplication.getInstance()?.getString(R.string.error_tip_net)!!
            }
        }
    }

    /**
     * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     *
     * @param code
     * @return
     */
    private fun getApiExceptionMessage(code: Int, msg: String?): String? {
        /* when (code) {
         }*/
        return msg
    }
}