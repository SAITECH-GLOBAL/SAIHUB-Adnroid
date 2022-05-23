package com.linktech.saihub.entity.wallet.js
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/18.
 */
data class JsMultiSigAddressEntity(
    @SerializedName("externalAddresses")
    var externalAddresses: List<String>?,
    @SerializedName("internalAddress")
    var internalAddress: List<String>?
)