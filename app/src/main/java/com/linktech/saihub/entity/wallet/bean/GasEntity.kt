package com.linktech.saihub.entity.wallet.bean
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/9.
 */
data class GasEntity(
    @SerializedName("fastGasPrice")
    var fastGasPrice: String?,
    @SerializedName("lastBlock")
    var lastBlock: Any?,
    @SerializedName("proposeGasPrice")
    var proposeGasPrice: String?,
    @SerializedName("safeGasPrice")
    var safeGasPrice: String?
)