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
import com.linktech.saihub.databinding.ActivityResetPwdMnemonicPrivatekeyBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.wallet.MnemonicUtil
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.util.walutils.PrivateKeyCheckUtil
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus


@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_RESET_PWD_MNEMONIC_PRIVATEKEY_ACTIVITY_PATH)
class ResetPwdMnemonicPrivateKeyActivity : BaseActivity() {

    var binding: ActivityResetPwdMnemonicPrivatekeyBinding? = null
    var walletBean: WalletBean? = null

    override fun onInit() {
        super.onInit()
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_reset_pwd_mnemonic_privatekey)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)

        setFunction()
    }

    private fun setFunction() {

        binding?.swEtNewPwd1?.etSwitch?.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                binding?.psvPwd?.setStrengthMode(RegexUtils.checkPassWord(s))
                setConfirmEnable()
            }
        })
        binding?.swEtNewPwd2?.etSwitch?.addTextChangedListener {
            setConfirmEnable()
        }
        binding?.etInput?.addTextChangedListener {
            setConfirmEnable()
        }

        binding?.tvConfirm?.onClick(Constants.CLICK_INTERVAL) {

            if (!checkMnemonicOrPrivateKey(binding?.etInput?.text?.toString()?.trim().toString())) {
                ToastUtils.shortImageToast(getString(R.string.please_enter_right_phrase_or_privatekey))
                return@onClick
            }

            val newPwq1 = binding?.swEtNewPwd1?.etSwitch?.text?.toString()?.trim()
            val newPwq2 = binding?.swEtNewPwd2?.etSwitch?.text?.toString()?.trim()
            if (!RegexUtils.checkPasswd(newPwq1)) {
                binding?.swEtNewPwd1?.setErrorTipLine()
                binding?.tvErrorHint?.setVisible(true)
                binding?.tvErrorHint?.text = getString(R.string.pwd_tip_0)
                return@onClick
            }
            if (!RegexUtils.checkPasswd(newPwq2)) {
                binding?.swEtNewPwd2?.setErrorTipLine()
                binding?.tvErrorHint?.setVisible(true)
                binding?.tvErrorHint?.text = getString(R.string.pwd_tip_0)
                return@onClick
            }
            if (!newPwq1.equals(newPwq2)) {
                binding?.tvErrorHint?.setVisible(true)
                binding?.tvErrorHint?.text = getString(R.string.the_two_passwords_do_not_match)

                return@onClick
            }
            resetPwd(binding?.etInput?.text?.trim().toString(),
                binding?.swEtNewPwd1?.etSwitch?.text?.trim().toString())

        }

        binding?.tvCancel?.onClick(Constants.CLICK_INTERVAL) {
            finish()
        }
    }

    private fun resetPwd(contentStr: String, newPwd: String) {
        lifecycleScope.launch {

            val walletMenmonic = AES.decrypt(walletBean?.mnemonic)?.replace("", " ")
            val walletPrivateKey = AES.decrypt(walletBean?.privateKey)?.replace("", " ")
            val inputStr = contentStr.replace("", " ")

            if (inputStr != walletMenmonic && inputStr != walletPrivateKey) {
                ToastUtils.shortImageToast(getString(R.string.please_enter_right_phrase_or_privatekey))
                return@launch
            }

            withContext(Dispatchers.IO) {
                walletBean?.password = AES.encrypt(newPwd.trim())
                WalletDaoUtils.updateWallet(walletBean)
            }
            ToastUtils.shortRightImageToast(getString(R.string.password_reset_successful))
            walletBean?.let {
                EventBus.getDefault()
                    .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_RESET_PWD_WALLET,
                        walletBean!!))
            }
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ACTIVITY_PATH)
                .navigation()
        }
    }

    private fun checkMnemonicOrPrivateKey(contentStr: String): Boolean {
        if (MnemonicUtil.checkMnemonic(contentStr)) {
            return true
        }
        if (PrivateKeyCheckUtil.checkBTCPrivateKey(contentStr)) {
            return true
        }
        return false
    }

    private fun setConfirmEnable() {
        binding?.tvConfirm?.isEnabled =
            binding?.swEtNewPwd1?.etSwitch?.text?.trim()?.isNotEmpty() == true &&
                    binding?.swEtNewPwd2?.etSwitch?.text?.trim()?.isNotEmpty() == true &&
                    binding?.etInput?.text?.trim()?.isNotEmpty() == true
    }

}