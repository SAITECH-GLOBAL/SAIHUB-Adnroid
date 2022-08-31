package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class InvoiceEntity(
    @SerializedName("add_index")
    val addIndex: String?,
    @SerializedName("pay_req")
    val payReq: String?,
    @SerializedName("payment_request")
    val paymentRequest: String?
) : BaseLnResp()

data class RHash(
    @SerializedName("data")
    val `data`: List<Int>?,
    @SerializedName("type")
    val type: String?
)