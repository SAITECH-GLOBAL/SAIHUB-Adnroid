package com.linktech.saihub.entity.wallet.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by tromo on 2022/3/14.
 */
class AddEntity {
    @SerializedName("address")
    var address: String? = ""

    @SerializedName("coin")
    var coin: String? = ""

    @SerializedName("type")
    var type: Int? = 0
}