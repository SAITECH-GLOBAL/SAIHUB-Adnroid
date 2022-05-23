package com.linktech.saihub.entity.wallet.bean

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.app.Constants
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc
import com.linktech.saihub.util.walutils.NumericUtil
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.core.Utils
import org.bouncycastle.util.encoders.Hex
import java.io.Serializable


/**
 * Created by tromo on 2022/3/14.
 */
data class UTXODataBean(
    @SerializedName("notice")
    var notice: String?,
    @SerializedName("unspent_outputs")
    var unspentOutputs: List<UnspentOutput>?
)

data class UnspentOutput(
    @SerializedName("confirmations")
    var confirmations: Int?,
    @SerializedName("script")
    var script: String?,
    @SerializedName("tx_hash")
    var txHash: String?,
    @SerializedName("tx_hash_big_endian")
    var txHashBigEndian: String?,
    @SerializedName("tx_index")
    var txIndex: Long?,
    @SerializedName("tx_output_n")
    var txOutputN: Long?,
    @SerializedName("value")
    var value: Long?,
    @SerializedName("value_hex")
    var valueHex: String?,
    var sequence: Long = 4294967295L
) : Serializable {
    fun getAddress(type: Int): String {
        val substring: String = this.script?.substring(4,
            this.script?.length?.minus(2) ?: 5)!!
        return if (type == Constants.CHILD_ADDRESS_NATIVE) {
            SegwitAddress.fromHash(ParamsManagerBtc.getParams(), Hex.decode(this.script?.length?.let {
                this.script?.substring(4, it)
            })).toBech32()
        } else {
            LegacyAddress.fromScriptHash(ParamsManagerBtc.getParams(), Hex.decode(substring)).toString()
        }
    }
}