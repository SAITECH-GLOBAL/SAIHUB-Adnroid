package com.linktech.saihub.mvvm.model

import com.linktech.saihub.entity.power.DeviceInfoResult
import com.linktech.saihub.mvvm.base.BaseViewModel
import com.linktech.saihub.mvvm.base.VmLiveData
import com.linktech.saihub.mvvm.base.launchVmRequest
import com.linktech.saihub.mvvm.repository.PowerRepository


class PowerViewModel() : BaseViewModel() {

    private val powerRepository by lazy { PowerRepository() }

    val mDeviceInfoData by lazy { VmLiveData<DeviceInfoResult>() }

    fun getDeviceInfo(deviceCode: String) {
        launchVmRequest({powerRepository.getDeviceInfo(deviceCode)},mDeviceInfoData)
    }

}