package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class LightningBalanceEntity(
    @SerializedName("BTC")
    val bTC: BTC?
) : BaseLnResp()

data class BTC(
    @SerializedName("AvailableBalance")
    val availableBalance: Long?
)