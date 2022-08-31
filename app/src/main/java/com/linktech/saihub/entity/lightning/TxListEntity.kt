package com.linktech.saihub.entity.lightning

import android.os.Parcelable
import com.linktech.saihub.net.BaseLnResp
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.math.BigDecimal


/**
 * Created by tromo on 2022/6/21.
 */
data class TxListEntity(
    var array: List<TxEntity>?,
) : BaseLnResp()

@Parcelize
class TxEntity : Parcelable {
    var fee: Int = 0
    var memo: String? = ""
    val timestamp: Any? = null
    var time: Long? = null
    var type: String? = ""
    var value: Long? = null
    var amount: BigDecimal = BigDecimal.ZERO
    var isReceive: Boolean? = false
    var isPaid: Boolean? = true
    var isOutDate: Boolean? = false
    var payReq: String? = ""
    var expireTime: Int? = null
}
