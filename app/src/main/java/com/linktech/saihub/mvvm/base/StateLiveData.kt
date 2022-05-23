package com.linktech.saihub.mvvm.base

import androidx.lifecycle.MutableLiveData
import com.linktech.saihub.net.BaseResp

/**
 * Created by tromo on 2021/8/29.
 */
class StateLiveData<T> : MutableLiveData<BaseResp<T>>() {
}