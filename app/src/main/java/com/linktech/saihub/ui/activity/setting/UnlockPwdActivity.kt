package com.linktech.saihub.ui.activity.setting

import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySettingUnlockPwdBinding
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.ui.dialog.ResetDialog
import com.linktech.saihub.util.KeyBoardUtils
import com.linktech.saihub.util.MD5Utils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.ed.OnInputListener

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
class UnlockPwdActivity : BaseActivity() {

    private var binding: ActivitySettingUnlockPwdBinding? = null

    var stateUnlock = UNLOCK_PWD
    var pwdTouchErrorNumber = 0
    var errorTimeTemp = 0L

    companion object {
        const val UNLOCK_PWD = 0  //开启密码验证  输入密码
        const val UNLOCK_REPEAT = 1   //开启密码验证  再次输入密码
        const val UNLOCK_VERIFY = 2   //关闭密码验证 验证密码
        const val UNLOCK_TOUCH_ID = 3   //开启关闭 touchid 验证密码
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_unlock_pwd)
        stateUnlock = intent.getIntExtra(StringConstants.VERIFY_STATUS, UNLOCK_PWD)


        when (stateUnlock) {
            //开启密码验证  输入密码
            UNLOCK_PWD -> {
                binding?.tvPwdHint?.text = getString(R.string.password)
                binding?.etPassword?.setOnInputListener(object : OnInputListener() {
                    override fun onInputFinished(content: String?) {
                        ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNLOCK_PWD_ACTIVITY_PATH)
                            .withInt(StringConstants.VERIFY_STATUS, UNLOCK_REPEAT)
                            .withString(StringConstants.VERIFY_PWD_CONTENT, MD5Utils.digest(content))
                            .navigation()
                        finish()
                    }
                })
            }
            //再次设置密码
            UNLOCK_REPEAT -> {
                binding?.tvPwdHint?.text = getString(R.string.repeat)
                val pwdContentFirst = intent.getStringExtra(StringConstants.VERIFY_PWD_CONTENT)
                binding?.etPassword?.setOnInputListener(object : OnInputListener() {
                    override fun onInputFinished(content: String?) {
                        if (MD5Utils.digest(content)?.equals(pwdContentFirst) == true) {
                            MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOCAL, MD5Utils.digest(content))
                            MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOGIN, true)
                            finish()
                        } else {
                            binding?.etPassword?.setText("")
                            ToastUtils.shortImageToast(getString(R.string.the_two_passwords_do_not_match))
                        }
                    }
                })
            }
            //关闭密码验证 验证密码
            UNLOCK_VERIFY -> {
                binding?.tvPwdHint?.text = getString(R.string.unlock_password)
                binding?.etPassword?.setOnInputListener(object : OnInputListener() {
                    override fun onInputFinished(content: String?) {
                        val pwdContentLocal = MMKVManager.getInstance().mmkv().decodeString(Constants.VERIFY_PWD_LOCAL, "")

                        if (MD5Utils.digest(content)?.equals(pwdContentLocal) == false) {
                            binding?.etPassword?.setText("")

                            if (checkPwdNumber()) {

                                KeyBoardUtils.closeKeybord(binding?.etPassword, mContext)

                                var resetDialog = ResetDialog()
                                resetDialog?.confirmEvent = {
                                    ARouter.getInstance().build(ARouterUrl.WAL_WALLET_RESET_ACTIVITY_PATH).navigation()
                                }
                                resetDialog.show(supportFragmentManager, "")
                            } else {
                                ToastUtils.shortImageToast(getString(R.string.wrong_passwrod))
                            }
                            return
                        }

                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOCAL, "")
                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOGIN, false)
                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_TOUCH_ID_LOGIN, false)
                        finish()
                    }
                })
            }
            //开启关闭 touchid 验证密码
            UNLOCK_TOUCH_ID -> {
                binding?.topbar?.setTitle(getString(R.string.set_touch_id))
                binding?.tvPwdHint?.text = getString(R.string.unlock_password)
                binding?.etPassword?.setOnInputListener(object : OnInputListener() {
                    override fun onInputFinished(content: String?) {

                        val pwdContentLocal = MMKVManager.getInstance().mmkv().decodeString(Constants.VERIFY_PWD_LOCAL, "")

                        if (MD5Utils.digest(content)?.equals(pwdContentLocal) == false) {
                            binding?.etPassword?.setText("")

                            if (checkPwdNumber()) {

                                KeyBoardUtils.closeKeybord(binding?.etPassword, mContext)

                                //重置密码弹框
                                var resetDialog = ResetDialog()
                                resetDialog?.confirmEvent = {
                                    ARouter.getInstance().build(ARouterUrl.WAL_WALLET_RESET_ACTIVITY_PATH).navigation()
                                }
                                resetDialog.show(supportFragmentManager, "")
                            } else {
                                ToastUtils.shortImageToast(getString(R.string.wrong_passwrod))
                            }
                            return
                        }

                        val isVerifyPFing = MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_TOUCH_ID_LOGIN, false)
                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_TOUCH_ID_LOGIN, !isVerifyPFing)
                        finish()

                    }
                })
            }
        }

    }

    fun checkPwdNumber(): Boolean {
        if (System.currentTimeMillis() - errorTimeTemp > Constants.VERIFY_PWD_TIME) {
            pwdTouchErrorNumber = 0
        }
        pwdTouchErrorNumber++
        errorTimeTemp = System.currentTimeMillis()
        return pwdTouchErrorNumber >= 3
    }

}