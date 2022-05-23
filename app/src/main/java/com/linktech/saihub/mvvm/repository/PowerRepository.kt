package com.linktech.saihub.mvvm.repository

import com.linktech.saihub.app.SaihubNetUrl
import com.linktech.saihub.entity.power.DeviceInfoData
import com.linktech.saihub.entity.power.DeviceInfoEntity
import com.linktech.saihub.entity.power.DeviceInfoResult
import com.linktech.saihub.mvvm.service.PowerService
import com.linktech.saihub.net.BaseResp
import com.linktech.saihub.net.helper.RetrofitHelper


class PowerRepository() : BaseRepository() {



    suspend fun getDeviceInfo(deviceCode: String): BaseResp<DeviceInfoResult> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, PowerService::class.java)
            .getDeviceInfo(deviceCode)
    }

}