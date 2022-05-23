package com.linktech.saihub.entity.wallet.bean
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/4.
 */
data class RateEntity(
    @SerializedName("btcCny")
    var btcCny: String?,
    @SerializedName("btcRub")
    var btcRub: String?,
    @SerializedName("btcUsd")
    var btcUsd: String?,
    @SerializedName("usdtCny")
    var usdtCny: String?,
    @SerializedName("usdtRub")
    var usdtRub: String?,
    @SerializedName("usdtUsd")
    var usdtUsd: String?
)