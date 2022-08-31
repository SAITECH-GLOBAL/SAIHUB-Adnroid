package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class BTCAddressEntity(
    val array: List<AddressEntity>?,
) : BaseLnResp()

data class AddressEntity(
    @SerializedName("address")
    val address: String?
)