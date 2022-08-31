package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class DecodeInvoiceEntity(
    @SerializedName("cltv_expiry")
    val cltvExpiry: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("description_hash")
    val descriptionHash: String?,
    @SerializedName("destination")
    val destination: String?,
    @SerializedName("expiry")
    val expiry: String?,
    @SerializedName("fallback_addr")
    val fallbackAddr: String?,
    @SerializedName("num_msat")
    val numMsat: String?,
    @SerializedName("num_satoshis")
    val numSatoshis: String?,
    @SerializedName("payment_hash")
    val paymentHash: String?,
    @SerializedName("route_hints")
    val routeHints: List<Any>?,
    @SerializedName("timestamp")
    val timestamp: String?,
    var payReq: String?
) : BaseLnResp()
