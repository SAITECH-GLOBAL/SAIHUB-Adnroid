package com.linktech.saihub.ui.activity.setting

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySettingUnlockBinding
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.ui.dialog.AddWalletDialog
import com.linktech.saihub.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_ACTIVITY_PATH)
class UnlockSettingActivity : BaseActivity() {

    private var binding: ActivitySettingUnlockBinding? = null

    private var isVerifyPwd: Boolean = false

    var fingerprintIdentify: FingerprintIdentify? = null

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_unlock)
        initFingerPrint()
    }

    override fun onData() {
        super.onData()

        walletAssetViewModel.mWalletListData.vmObserver(this) {
            onAppSuccess = {
                //如果钱包列表为空，弹出引导创建钱包弹窗
                if (it.isEmpty()) {
                    val addWalletDialog = AddWalletDialog()
                    addWalletDialog.confirmEvent = {
                        ARouter.getInstance().build(ARouterUrl.WAL_WALLET_ADD_ACTIVITY_PATH)
                            .navigation()
                    }
                    addWalletDialog.showNow(supportFragmentManager, "")
                } else {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
                        .withInt(
                            StringConstants.VERIFY_STATUS,
                            if (!isVerifyPwd) UnlockPwdActivity.UNLOCK_PWD else UnlockPwdActivity.UNLOCK_VERIFY
                        )
                        .navigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setSwtich()
    }

    private fun setSwtich() {
        isVerifyPwd = MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_PWD_LOGIN, false)
        binding?.svPwd?.setImageResource(if (isVerifyPwd) R.mipmap.icon_switch_open else R.mipmap.icon_switch_close)

        val isVerifyPFing =
            MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_TOUCH_ID_LOGIN, false)
        if (!isVerifyPwd) {
            binding?.svTouchId?.setImageResource(R.mipmap.icon_switch_enable)
        } else {
            binding?.svTouchId?.setImageResource(if (isVerifyPFing) R.mipmap.icon_switch_open else R.mipmap.icon_switch_close)
        }
        //密码
        binding?.llPwd?.onClick(Constants.CLICK_INTERVAL) {
            isVerifyPwd =
                MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_PWD_LOGIN, false)
            if (!isVerifyPwd) {
                walletAssetViewModel.getWalletList()
            } else {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
                    .withInt(
                        StringConstants.VERIFY_STATUS,
                        if (!isVerifyPwd) UnlockPwdActivity.UNLOCK_PWD else UnlockPwdActivity.UNLOCK_VERIFY
                    )
                    .navigation()
            }


        }
        //指纹
        binding?.llTouchId?.onClick(Constants.CLICK_INTERVAL) {
            val isVerifyPwd =
                MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_PWD_LOGIN, false)
            if (isVerifyPwd) {
                //未开通跳转 开通流程
                if (fingerprintIdentify?.isFingerprintEnable != true) {
                    ToastUtils.shortToast(getString(R.string.open_touchid_hint))
                    return@onClick
                }
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
                    .withInt(StringConstants.VERIFY_STATUS, UnlockPwdActivity.UNLOCK_TOUCH_ID)
                    .navigation()
            } else {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
                    .withInt(
                        StringConstants.VERIFY_STATUS,
                        if (!isVerifyPwd) UnlockPwdActivity.UNLOCK_PWD else UnlockPwdActivity.UNLOCK_VERIFY
                    )
                    .navigation()
            }
        }

    }

    private fun initFingerPrint() {
        fingerprintIdentify = FingerprintIdentify(mContext)
        fingerprintIdentify?.setSupportAndroidL(true)
        fingerprintIdentify?.init()
    }


}