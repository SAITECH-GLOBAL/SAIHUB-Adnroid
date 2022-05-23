package com.linktech.saihub.mvvm.base

import androidx.lifecycle.ViewModel
import com.linktech.saihub.app.SaiHubApplication

/**
 * Created by tromo on 2021/8/29.
 */
open class BaseViewModel : ViewModel() {


    fun getString(id: Int): String? {
        return SaiHubApplication.getInstance()?.getString(id)
    }
}