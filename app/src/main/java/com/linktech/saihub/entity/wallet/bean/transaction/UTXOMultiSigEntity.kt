package com.linktech.saihub.entity.wallet.bean.transaction

import java.io.Serializable


/**
 * 多签 utxo
 * Created by tromo on 2022/3/17.
 */
class UTXOMultiSigEntity : Serializable {
    var address: String? = ""
    var confirmations: Int? = 0
    var txId: String? = ""
    var txid: String? = ""
    var txhex: String? = ""
    var value: Long? = 0
    var vout: Long? = 0
    var wif: Boolean = false
}
