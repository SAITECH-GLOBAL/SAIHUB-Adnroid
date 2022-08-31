package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class InvoiceListEntity(
    val array: List<InvoiceItemEntity>?,
) : BaseLnResp()

data class InvoiceItemEntity(
    @SerializedName("add_index")
    val addIndex: String?,
    @SerializedName("amt")
    val amt: Int?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("expire_time")
    val expireTime: Int?,
    @SerializedName("ispaid")
    val ispaid: Boolean?,
    @SerializedName("pay_req")
    val payReq: String?,
    @SerializedName("payment_hash")
    val paymentHash: String?,
    @SerializedName("payment_request")
    val paymentRequest: String?,
    @SerializedName("r_hash")
    val rHash: RHash?,
    @SerializedName("timestamp")
    val timestamp: Int?,
    @SerializedName("type")
    val type: String?
)

