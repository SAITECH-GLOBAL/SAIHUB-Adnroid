package com.linktech.saihub.entity.wallet.js

import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/17.
 */
data class JsMultiSigEntity(
    @SerializedName("fee")
    var fee: Int?,
    @SerializedName("urcode")
    var urcode: List<String>?
)