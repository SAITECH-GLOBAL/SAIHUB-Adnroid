package com.linktech.saihub.entity.wallet.bean

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.app.Constants
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.SegwitAddress
import org.bouncycastle.util.encoders.Hex


/**
 * Created by tromo on 2022/3/9.
 */

data class UTXOEntity(
    @SerializedName("address")
    var address: String?,
    @SerializedName("amount")
    var amount: Int?,
    @SerializedName("scriptPubKey")
    var scriptPubKey: String?,
    @SerializedName("txId")
    var txId: String?,
    @SerializedName("vout")
    var vout: Int?,
    var sequence: Long = 4294967295L
) {
    fun getAddress(type: Int): String {
        val substring: String = this.scriptPubKey?.substring(4,
            this.scriptPubKey?.length?.minus(2) ?: 5)!!
        return if (type == Constants.CHILD_ADDRESS_NATIVE) {
            LegacyAddress.fromPubKeyHash(ParamsManagerBtc.getParams(), Hex.decode(substring))
                .toString()
        } else {
            SegwitAddress.fromHash(ParamsManagerBtc.getParams(), Hex.decode(substring)).toString()
        }
    }
}