package com.linktech.saihub.entity.lightning
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/6/20.
 */
data class KeySendEntity(
    @SerializedName("callback")
    val callback: String?,
    @SerializedName("commentAllowed")
    val commentAllowed: Int?,
    @SerializedName("maxSendable")
    val maxSendable: Int?,
    @SerializedName("metadata")
    val metadata: String?,
    @SerializedName("minSendable")
    val minSendable: Int?,
    @SerializedName("payerData")
    val payerData: PayerData?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("tag")
    val tag: String?
)

data class PayerData(
    @SerializedName("email")
    val email: Email?,
    @SerializedName("name")
    val name: Name?
)

data class Email(
    @SerializedName("mandatory")
    val mandatory: Boolean?
)

data class Name(
    @SerializedName("mandatory")
    val mandatory: Boolean?
)