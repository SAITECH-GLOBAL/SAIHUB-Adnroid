package com.linktech.saihub.entity.wallet
import com.google.gson.annotations.SerializedName


/**
 * Created by tromo on 2022/3/30.
 */
data class ExtPubKeyEntity(
    @SerializedName("AccountKeyPath")
    var accountKeyPath: String?,
    @SerializedName("ExtPubKey")
    var extPubKey: String?,
    @SerializedName("MasterFingerprint")
    var masterFingerprint: String?
)