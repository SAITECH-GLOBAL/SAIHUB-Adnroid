package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/6/20.
 */
data class KeySendCallbackEntity(
    @SerializedName("pr")
    val pr: String?,
    @SerializedName("routes")
    val routes: List<Any>?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("successAction")
    val successAction: SuccessAction?,
    @SerializedName("reason")
    val reason: String?
)

data class SuccessAction(
    @SerializedName("message")
    val message: String?,
    @SerializedName("tag")
    val tag: String?
)