package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/20.
 */
data class DecodePayInvoiceEntity(
    @SerializedName("decoded")
    val decoded: PayInvoiceEntity?
) : BaseLnResp()

