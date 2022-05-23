package com.linktech.saihub.entity.databinding

import com.google.gson.annotations.SerializedName

/**
 * Created by tromo on 2022/2/23.
 */
class FeeEntity {
    @SerializedName("num")
    var num: String? = ""

    @SerializedName("type")
    var type: String? = ""

    @SerializedName("isSelect")
    var isSelect: Boolean? = false

    @SerializedName("convert")
    var convert: String? = ""

    @SerializedName("convertType")
    var convertType: String? = ""

    var gasPrice: Long? = 0
}

