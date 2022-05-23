package com.linktech.saihub.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.databinding.FragmentMainSettingBinding
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.ui.dialog.AddWalletDialog
import com.linktech.saihub.util.CommonUtil
import com.linktech.saihub.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick

class MainSettingFragment : BaseFragment() {

    private var binding: FragmentMainSettingBinding? = null

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainSettingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun initViews() {

        binding?.tvSettingBottom?.text =
            "${getString(R.string.saihub_app)} ${CommonUtil.getVersionName(context)}"
    }

    private fun setLangUnitView() {
        binding?.tvLanguage?.text =
            RateAndLocalManager.getInstance(activity).curLocalLanguageKind.name
        binding?.tvUnit?.text = RateAndLocalManager.getInstance(activity).curRateKind.name
    }

    override fun addEvent() {
        binding?.ivSettingTwitter?.onClick(Constants.CLICK_INTERVAL) {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/SAI2TECH"))
            startActivity(browserIntent)
        }

        binding?.ivSettingChat?.onClick(Constants.CLICK_INTERVAL) {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("http://discord.gg/JM68xXPs7E"))
            startActivity(browserIntent)
        }

        binding?.clLanguage?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_LANG_ACTIVITY_PATH)
                .navigation()
        }

        binding?.clCurrencyUnit?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNIT_ACTIVITY_PATH)
                .navigation()
        }

        binding?.clAddressBook?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ACTIVITY_PATH)
                .withInt(StringConstants.LOAD_TYPE, Constants.ADDRESS_BOOK_EDIT)
                .navigation()
        }

        binding?.clUnlockSetting?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_ACTIVITY_PATH)
                .navigation()

        }
        binding?.clAppVersion?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_APPVERSION_ACTIVITY_PATH)
                .navigation()
        }

    }


    override fun onResume() {
        super.onResume()
        setLangUnitView()
    }
}