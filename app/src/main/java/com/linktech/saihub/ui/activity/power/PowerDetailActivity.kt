package com.linktech.saihub.ui.activity.power

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.linktech.saihub.BuildConfig
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPowerDetailBinding
import com.linktech.saihub.db.bean.PowerBean
import com.linktech.saihub.entity.power.DeviceInfoResult
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.PowerViewModel
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.util.system.setVisible

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_POWER_DETAIL_ACTIVITY_PATH)
class PowerDetailActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[PowerViewModel::class.java] }

    private var binding: ActivityPowerDetailBinding? = null
    var powerBean: PowerBean? = null
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_power_detail)
        powerBean = intent.getSerializableExtra(StringConstants.POWER_DATA) as PowerBean?

        binding?.tvPowerNumber?.text = powerBean?.name


        powerBean?.number?.let { viewModel.getDeviceInfo(it) }
        viewModel.mDeviceInfoData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                showSuccessUi()
                setVieForData(it)
//                debugProcessData(it)
                binding?.lcDefiDetail?.setData(it.hourDataList)
                hideLoading()
            }
            onAppError = {
                hideLoading()
                showErrorUi();
            }

            onAppComplete = {
                hideLoading()
                binding?.srlTrading?.finishRefresh()
            }
        }

        binding?.srlTrading?.setOnRefreshListener {
            powerBean?.number?.let { viewModel.getDeviceInfo(it) }
        }
    }

    private fun showSuccessUi() {
        binding?.llContent?.setVisible(true)
        binding?.clEmpty?.setVisible(false)
    }

    private fun showErrorUi() {
        binding?.llContent?.setVisible(false)
        binding?.clEmpty?.setVisible(true)
        binding?.ivCircleStatus?.setImageResource(R.mipmap.icon_power_offline)
        binding?.tvPowerStatus?.text = getString(R.string.offline)
        binding?.tvPowerStatus?.setTextColor(
            mContext?.resources?.getColor(R.color.color_FFFF3750)!!
        )
        val dialog = NomalSuccessFailDialog()
        dialog.show(supportFragmentManager, "")
    }

    private fun debugProcessData(deviceInfoResult: DeviceInfoResult) {
        if (BuildConfig.DEBUG) {
            deviceInfoResult.hourDataList?.forEachIndexed { index, item ->
                when (index) {
                    0 -> {
                        item.unitEnergyConsumption = "350"
                        item.outputHeat = "200"
                    }
                    1 -> {
                        item.unitEnergyConsumption = "310"
                        item.outputHeat = "240"
                    }
                    2 -> {
                        item.unitEnergyConsumption = "330"
                        item.outputHeat = "230"
                    }
                    3 -> {
                        item.unitEnergyConsumption = "360"
                        item.outputHeat = "250"
                    }
                    4 -> {
                        item.unitEnergyConsumption = "380"
                        item.outputHeat = "210"
                    }
                    5 -> {
                        item.unitEnergyConsumption = "300"
                        item.outputHeat = "280"
                    }
                }
            }
        }
    }


    private fun setVieForData(infoEntity: DeviceInfoResult?) {
        infoEntity ?: return
        //????????????
        binding?.ivCircleStatus?.setImageResource(if (infoEntity?.online == true) R.mipmap.icon_power_online else R.mipmap.icon_power_offline)
        binding?.tvPowerStatus?.text =
            getString(if (infoEntity.online == true) R.string.online else R.string.offline)
        binding?.tvPowerStatus?.setTextColor(
            mContext?.resources
                ?.getColor(if (infoEntity.online == true) R.color.color_FF00C873 else R.color.color_FFFF3750)!!
        )
        //?????????
        binding?.tvTotalPower?.text = "${infoEntity?.unitEnergyConsumption}"  //????????????
        binding?.tvTotalPowerUnit?.text = " ${infoEntity?.consumptionUnit}"

        //???????????????
        binding?.tvOutputHeat?.text = "${infoEntity?.outputHeat}"
        binding?.tvOutputHeatUnit?.text = " ${infoEntity?.outputHeatUnit}"

        //????????????
        binding?.tvInTemp?.text =
            "${infoEntity?.inletWaterTemperature} ${infoEntity?.temperatureUnit}"
        //????????????
        binding?.tvOutTemp?.text =
            "${infoEntity?.outWaterTemperature} ${infoEntity?.temperatureUnit}"
        //????????????
        binding?.tvWaterPIn?.text = "${infoEntity?.waterInletPressure} ${infoEntity?.pressureUnit}"
        //????????????
        binding?.tvWaterPOut?.text = "${infoEntity?.waterOutPressure} ${infoEntity?.pressureUnit}"
        //????????????
        binding?.tvTempIndoor?.text =
            "${infoEntity?.indoorTemperature} ${infoEntity?.temperatureUnit}"
        //????????????
        binding?.tvTempOutdoor?.text =
            "${infoEntity?.outdoorTemperature} ${infoEntity?.temperatureUnit}"
        //????????????
        binding?.tvTempCabinet?.text =
            "${infoEntity?.cabinetTemperature} ${infoEntity?.temperatureUnit}"
    }


}