package com.linktech.saihub.entity.lightning

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Created by tromo on 2022/6/21.
 */
@Parcelize
class PendingInvoiceEntity(
    var memo: String? = null,
    var timestamp: Long? = null,
    var value: Long? = null,
    var payReq: String? = "",
    var expireTime: Int? = null
) : Parcelable
