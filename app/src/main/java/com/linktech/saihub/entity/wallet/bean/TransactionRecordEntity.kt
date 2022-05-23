package com.linktech.saihub.entity.wallet.bean
import com.google.gson.annotations.SerializedName
import com.linktech.saihub.db.bean.TransferServerBean


/**
 * Created by tromo on 2022/3/8.
 */
data class TransactionRecordEntity(
    @SerializedName("pages")
    var pages: Int?,
    @SerializedName("records")
    var records: List<TransferServerBean>?,
    @SerializedName("total")
    var total: Int?
)

data class Record(
    @SerializedName("amount")
    var amount: String?,
    @SerializedName("blockNumber")
    var blockNumber: Long?,
    @SerializedName("coin")
    var coin: String?,
    @SerializedName("content")
    var content: String?,
    @SerializedName("contractAddress")
    var contractAddress: Any?,
    @SerializedName("fee")
    var fee: String?,
    @SerializedName("fromAddress")
    var fromAddress: String?,
    @SerializedName("fromAddressList")
    var fromAddressList: List<String>?,
    @SerializedName("hash")
    var hash: String?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("isDeleted")
    var isDeleted: Any?,
    @SerializedName("status")
    var status: Int?,
    @SerializedName("time")
    var time: Long?,
    @SerializedName("timestamp")
    var timestamp: Long?,
    @SerializedName("toAddress")
    var toAddress: String?,
    @SerializedName("toAddressList")
    var toAddressList: List<String>?,
    @SerializedName("trxContractType")
    var trxContractType: String?,
    @SerializedName("type")
    var type: Int?
)