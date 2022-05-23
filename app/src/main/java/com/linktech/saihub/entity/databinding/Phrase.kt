package com.linktech.saihub.entity.databinding

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by tromo on 2022/2/23.
 */
@Parcelize
data class Phrase(
    @SerializedName("content")
    var content: String?,
    @SerializedName("index")
    var index: Int?
) : Parcelable
