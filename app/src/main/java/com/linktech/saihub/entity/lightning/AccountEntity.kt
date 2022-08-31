package com.linktech.saihub.entity.lightning

import com.google.gson.annotations.SerializedName
import com.linktech.saihub.net.BaseLnResp


/**
 * Created by tromo on 2022/6/17.
 */
data class AccountEntity(
    @SerializedName("login")
    val login: String?,
    @SerializedName("password")
    val password: String?
) : BaseLnResp()