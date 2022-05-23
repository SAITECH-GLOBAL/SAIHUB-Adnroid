package com.linktech.saihub.ui.activity.wallet.manager

import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityModifyPasswordBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.manager.AES
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.ui.dialog.ResetWalletPwdDialog
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_MODIFY_PASSWORD_ACTIVITY_PATH)
class ModifyPasswordActivity : BaseActivity() {

    var binding: ActivityModifyPasswordBinding? = null
    var walletBean: WalletBean? = null

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_password)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)

        walletBean ?: finish()
        setFunction()

    }

    private fun setFunction() {
        binding?.swEtOldPwd?.etSwitch?.addTextChangedListener { }
        binding?.tvCancel?.onClick {
            finish()
        }

        binding?.tvForgetPwd?.onClick(Constants.CLICK_INTERVAL) {
            val dialog = ResetWalletPwdDialog()
            dialog.confirmEvent = {

                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_RESET_PWD_MNEMONIC_PRIVATEKEY_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .navigation()
            }
            dialog.show(supportFragmentManager, "")
        }

        binding?.swEtOldPwd?.etSwitch?.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                setConfimEnable()
            }
        })

        binding?.swEtNewPwd2?.etSwitch?.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                setConfimEnable()
                binding?.tvErrorHint?.setVisible(false)
            }
        })

        binding?.swEtNewPwd1?.etSwitch?.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                binding?.tvErrorHint?.setVisible(false)
                binding?.psvPwd?.setStrengthMode(RegexUtils.checkPassWord(s))
                setConfimEnable()
                binding?.swEtNewPwd1?.resetStatus()
            }
        })

        binding?.tvConfirm?.onClick(Constants.CLICK_INTERVAL) {
            val oldPwd = binding?.swEtOldPwd?.etSwitch?.text?.toString()?.trim()
            val encryptOld = AES.encrypt(oldPwd)

            if (!RegexUtils.checkPasswd(binding?.swEtOldPwd?.etSwitch?.text?.toString()?.trim())) {
                binding?.swEtOldPwd?.setErrorTip(getString(R.string.pwd_tip_0))
                return@onClick
            }

            val newPwq1 = binding?.swEtNewPwd1?.etSwitch?.text?.toString()?.trim()
            val newPwq2 = binding?.swEtNewPwd2?.etSwitch?.text?.toString()?.trim()
            if (!RegexUtils.checkPasswd(newPwq1)) {
                binding?.swEtNewPwd1?.setErrorTipLine()
                binding?.tvErrorHint?.text = getString(R.string.pwd_tip_0)
                binding?.tvErrorHint?.setVisible(true)
                return@onClick
            }
            if (!RegexUtils.checkPasswd(newPwq2)) {
                binding?.swEtNewPwd2?.setErrorTipLine()
                binding?.tvErrorHint?.text = getString(R.string.pwd_tip_0)
                binding?.tvErrorHint?.setVisible(true)
                return@onClick
            }
            if (!newPwq1.equals(newPwq2)) {
                binding?.tvErrorHint?.text = getString(R.string.the_two_passwords_do_not_match)
                binding?.tvErrorHint?.setVisible(true)
                return@onClick
            }

            if (encryptOld?.equals(walletBean?.password) == false) {
                binding?.swEtOldPwd?.setErrorTip(getString(R.string.original_password_wrong))
                return@onClick
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    walletBean?.password = AES.encrypt(newPwq2)
                    WalletDaoUtils.updateWallet(walletBean)
                }
                walletBean?.let {
                    EventBus.getDefault()
                        .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_PWD_WALLET,
                            walletBean!!))
                }
                val dialog = NomalSuccessFailDialog(R.mipmap.icon_dialog_uccess,
                    getString(R.string.complete),
                    getString(R.string.ok))
                dialog.confirmEvent = {
                    finish()
                }
                dialog.show(supportFragmentManager, "")
            }
        }
    }

    private fun setConfimEnable() {
        binding?.tvConfirm?.isEnabled =
            binding?.swEtOldPwd?.etSwitch?.text?.trim()?.isNotEmpty() == true &&
                    binding?.swEtNewPwd1?.etSwitch?.text?.trim()?.isNotEmpty() == true &&
                    binding?.swEtNewPwd2?.etSwitch?.text?.trim()?.isNotEmpty() == true
    }
}