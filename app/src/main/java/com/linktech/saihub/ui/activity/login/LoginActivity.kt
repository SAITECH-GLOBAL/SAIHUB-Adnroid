package com.linktech.saihub.ui.activity.login

import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityLoginBinding
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.ui.dialog.ResetDialog
import com.linktech.saihub.util.KeyBoardUtils
import com.linktech.saihub.util.MD5Utils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.ed.OnInputListener
import com.qmuiteam.qmui.kotlin.onClick
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint

/**
 * Created by tromo on 2022/2/18.
 */
@Route(path = ARouterUrl.WAL_WALLET_LOGIN_ACTIVITY_PATH)
class LoginActivity : BaseActivity() {

    private var binding: ActivityLoginBinding? = null

    var loginType = LOGIN_TYPE_PWD
    var pwdTouchErrorNumber = 0
    var errorTimeTemp = 0L

    companion object {
        const val LOGIN_TYPE_PWD = 0
        const val LOGIN_TYPE_TOUCHID = 1
    }

    var fingerprintIdentify: FingerprintIdentify? = null

    private val resetDialog by lazy {
        ResetDialog()
    }

    override fun getStatusBlackMode(): Boolean = true

    override fun onInit() {
        super.onInit()
        loginType = intent.getIntExtra(StringConstants.LOGIN_TYPE, LOGIN_TYPE_PWD)
        resetDialog.confirmEvent = {
            ARouter.getInstance().build(ARouterUrl.WAL_WALLET_RESET_ACTIVITY_PATH).navigation()
        }

        fingerprintIdentify = FingerprintIdentify(mContext)
        fingerprintIdentify!!.setSupportAndroidL(true)
        fingerprintIdentify!!.init()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding?.apply {

            if (loginType == LOGIN_TYPE_PWD) {
                tvLoginType.setVisible(false)
            }

            //输入密码
            etPassword.setOnInputListener(object : OnInputListener() {
                override fun onInputFinished(content: String?) {

                    val pwdContentLocal = MMKVManager.getInstance().mmkv()
                        .decodeString(Constants.VERIFY_PWD_LOCAL, "")
                    if (MD5Utils.digest(content)?.equals(pwdContentLocal) == false) {
                        binding?.etPassword?.setText("")

                        if (System.currentTimeMillis() - errorTimeTemp > Constants.VERIFY_PWD_TIME) {
                            pwdTouchErrorNumber = 0
                        }
                        pwdTouchErrorNumber++

                        if (pwdTouchErrorNumber >= 3) {

                            KeyBoardUtils.closeKeybord(binding?.etPassword, mContext)

                            resetDialog.show(supportFragmentManager, "")

                        } else {
                            ToastUtils.shortImageToast(getString(R.string.password_error_try_again))
                        }
                        errorTimeTemp = System.currentTimeMillis()
                        return
                    }
                    ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                        .navigation()
                    finish()
                }
            })

            changeLoginView(loginType)

            ivFingerprint?.onClick(Constants.CLICK_INTERVAL) {
                startFing()
            }

            //切换密码和指纹登录方式
            tvLoginType.onClick(Constants.CLICK_INTERVAL) {
                when (loginType) {
                    LOGIN_TYPE_PWD -> {
                        loginType = LOGIN_TYPE_TOUCHID
                        KeyBoardUtils.closeKeybord(etPassword, mContext)
                        startFing()
                        changeLoginView(loginType)
                    }
                    LOGIN_TYPE_TOUCHID -> {
                        loginType = LOGIN_TYPE_PWD
                        changeLoginView(loginType)
                        try {
                            fingerprintIdentify?.cancelIdentify()
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
    }

    private var verifyErrorNumber = 0
    private fun startFing() {
        fingerprintIdentify!!.startIdentify(Constants.MAX_AVAILABLE_TIMES,
            object : BaseFingerprint.IdentifyListener {
                override fun onSucceed() {
                    ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                        .navigation()
                    ToastUtils.shortRightImageToast(getString(R.string.fingerprint_verification_success))
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

    private fun changeLoginView(type: Int) {
        binding?.run {
            when (type) {
                LOGIN_TYPE_PWD -> {
                    tvLoginType.text = getString(R.string.login_touch)
                    etPassword.setVisible(true)
                    ivFingerprint.setVisible(false)
                }
                LOGIN_TYPE_TOUCHID -> {
                    tvLoginType.text = getString(R.string.login_password)
                    etPassword.setVisible(false)
                    ivFingerprint.setVisible(true)
                    startFing()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fingerprintIdentify?.cancelIdentify()
        } catch (e: Exception) {
        }
    }

    override fun onData() {
        super.onData()
    }

}