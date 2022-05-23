package com.linktech.saihub.mvvm.service

import com.linktech.saihub.entity.power.DeviceInfoResult
import com.linktech.saihub.net.BaseResp
import retrofit2.http.GET
import retrofit2.http.Query


interface PowerService {


    @GET("data/get/device/info")
    suspend fun getDeviceInfo(@Query("deviceCode") deviceCode: String): BaseResp<DeviceInfoResult>

}