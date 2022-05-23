package com.linktech.saihub.entity.wallet.bean

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal


/**
 * Created by tromo on 2022/3/7.
 */
data class BalanceEntity(
    @SerializedName("final_balance")
    var finalBalance: BigDecimal?,
    @SerializedName("n_tx")
    var nTx: BigDecimal?,
    @SerializedName("total_received")
    var totalReceived: BigDecimal?
)