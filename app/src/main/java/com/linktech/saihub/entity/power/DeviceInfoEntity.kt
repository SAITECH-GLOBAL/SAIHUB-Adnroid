package com.linktech.saihub.entity.power

import com.google.gson.annotations.SerializedName


data class DeviceInfoEntity(
    @SerializedName("code")
    var code: Int?,
    @SerializedName("data")
    var data: DeviceInfoData?,
    @SerializedName("msg")
    var msg: String?
)

data class DeviceInfoData(
    @SerializedName("message")
    var message: String?,
    @SerializedName("result")
    var result: DeviceInfoResult?,
    @SerializedName("status")
    var status: Int?
)

data class DeviceInfoResult(
    @SerializedName("cabinetTemperature")
    var cabinetTemperature: String?,
    @SerializedName("consumptionUnit")
    var consumptionUnit: String?,
    @SerializedName("deviceCode")
    var deviceCode: String?,
    @SerializedName("deviceGroupTag")
    var deviceGroupTag: String?,
    @SerializedName("hotMeterInletWaterTemperature")
    var hotMeterInletWaterTemperature: String?,
    @SerializedName("hotMeterOutWaterTemperature")
    var hotMeterOutWaterTemperature: String?,
    @SerializedName("hourDataList")
    var hourDataList: List<DeviceInfoHourData>?,
    @SerializedName("id")
    var id: Long?,
    @SerializedName("indoorTemperature")
    var indoorTemperature: String?,
    @SerializedName("inletWaterTemperature")
    var inletWaterTemperature: String?,
    @SerializedName("online")
    var online: Boolean?,
    @SerializedName("outWaterTemperature")
    var outWaterTemperature: String?,
    @SerializedName("outdoorTemperature")
    var outdoorTemperature: String?,
    @SerializedName("outputHeat")
    var outputHeat: String?,
    @SerializedName("outputHeatUnit")
    var outputHeatUnit: String?,
    @SerializedName("powerUnit")
    var powerUnit: String?,
    @SerializedName("pressureUnit")
    var pressureUnit: String?,
    @SerializedName("temperatureUnit")
    var temperatureUnit: String?,
    @SerializedName("time")
    var time: String?,
    @SerializedName("timestamp")
    var timestamp: Long?,
    @SerializedName("totalPower")
    var totalPower: String?,
    @SerializedName("unitEnergyConsumption")
    var unitEnergyConsumption: String?,
    @SerializedName("waterInletPressure")
    var waterInletPressure: String?,
    @SerializedName("waterOutPressure")
    var waterOutPressure: String?
)

data class DeviceInfoHourData(
    @SerializedName("outputHeat")
    var outputHeat: String?,
    @SerializedName("time")
    var time: String?,
    @SerializedName("timestamp")
    var timestamp: Long?,
    @SerializedName("unitEnergyConsumption")
    var unitEnergyConsumption: String?
)