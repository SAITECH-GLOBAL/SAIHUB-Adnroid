package com.linktech.saihub.ui.activity.wallet.transaction

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityTransactionLnConfirmBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.manager.AES
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint


/**
 * 交易详情
 * Created by tromo on 2022/3/1.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_TRANSACTION_CONFIRM_ACTIVITY_PATH)
class LnTransactionConfirmActivity : BaseActivity() {

    private var binding: ActivityTransactionLnConfirmBinding? = null

    private var walletBean: WalletBean? = null

    override fun translucentStatusBar(): Boolean = true

    var fingerprintIdentify: FingerprintIdentify? = null

    private var isPassword = true

    @SuppressLint("SetTextI18n")
    override fun onInit() {
        super.onInit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_ln_confirm)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)

        binding?.apply {

            etWalletPassword.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    tvConfirm.isEnabled = !TextUtils.isEmpty(s)
                }

            })
            isPassword = walletBean?.isOpenTouchIdPay == false
            initUI()

            //切换支付方式
            tvType.onClick {
                setVerifyType()
            }

            tvConfirm.onClick(Constants.CLICK_INTERVAL) {
                //验证密码
                if (etWalletPassword.getText() == AES.decrypt(walletBean?.password)) {
                    //success
                    setResult(TransactionLightningActivity.LN_PAY_CODE)
                    finish()
                } else {
                    etWalletPassword.setErrorTip(getString(R.string.password_error_try_again))
                }
            }

            tvCancel.onClick(Constants.CLICK_INTERVAL) {
                finish()
            }

            ivFingerprint.onClick(Constants.CLICK_INTERVAL) {
                startFinger()
            }

        }
    }

    override fun onData() {
        super.onData()
    }

    private fun setVerifyType() {
        isPassword = !isPassword
        changeType()
    }

    private fun changeType() {
        binding?.apply {
            if (isPassword) {
                llPwd.setVisible(true)
                ivFingerprint.setVisible(false)
                tvType.text = getString(R.string.touch_pay)
                etWalletPassword.etSwitch?.setText("")
                fingerprintIdentify?.cancelIdentify()
            } else {
                llPwd.setVisible(false)
                ivFingerprint.setVisible(true)
                tvType.text = getString(R.string.pwd_pay)
                startFinger()
            }
        }
    }

    private fun initUI() {
        initFingerPrint()
        binding?.apply {
            tvType.setVisible(walletBean?.isOpenTouchIdPay == true)
        }
        changeType()
    }

    private fun initFingerPrint() {
        fingerprintIdentify = FingerprintIdentify(mContext)
        fingerprintIdentify?.setSupportAndroidL(true)
        fingerprintIdentify?.init()
    }

    private var verifyErrorNumber = 0
    private fun startFinger() {
        fingerprintIdentify?.startIdentify(Constants.MAX_AVAILABLE_TIMES,
            object : BaseFingerprint.IdentifyListener {
                override fun onSucceed() {
                    //指纹验证成功
                    fingerprintIdentify?.cancelIdentify()
                    setResult(TransactionLightningActivity.LN_PAY_CODE)
                    finish()
                }

                override fun onNotMatch(availableTimes: Int) {
                    ToastUtils.shortImageToast(getString(R.string.fingerprint_verification_failed))
                }

                override fun onFailed(isDeviceLocked: Boolean) {
                    verifyErrorNumber++
                    ToastUtils.shortImageToast(getString(R.string.fingerprint_verification_failed))
                }

                override fun onStartFailedByDeviceLocked() {
                    ToastUtils.shortImageToast(getString(R.string.device_locked))
                }
            })
    }

}