package com.linktech.saihub.entity.wallet.bean
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/7.
 */
data class OMNIBalanceEntity(
    @SerializedName("balance")
    var balance: String?,
    @SerializedName("frozen")
    var frozen: String?,
    @SerializedName("reserved")
    var reserved: String?
)