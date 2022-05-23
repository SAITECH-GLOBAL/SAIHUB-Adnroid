package com.linktech.saihub.ui.activity.wallet.manager

import android.text.TextUtils
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
import com.linktech.saihub.databinding.ActivityVerifyPwdBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.ActivityManager
import com.linktech.saihub.ui.activity.login.GuideActivity
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

//备份私钥 验证密码
@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
class VerifyPwdActivity : BaseActivity() {

    var binding: ActivityVerifyPwdBinding? = null

    private var verifyType = VERIFY_PHRASE

    companion object {
        const val VERIFY_PHRASE = 0  //验证密码  备份助记词 备份私钥
        const val VERIFY_PRIVATEKEY = 1  //验证密码  备份助记词 备份私钥
        const val VERIFY_TOUCHID = 2 //touchid开启
        const val VERIFY_PASSPHRASE = 3 //更改助记词密码
        const val VERIFY_DELETE_WALLET = 4  //删除钱包
    }

    var walletBean: WalletBean? = null


    override fun onInit() {
        super.onInit()
        walletBean = intent?.getParcelableExtra(StringConstants.WALLET_DATA)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_pwd)
        verifyType = intent.getIntExtra(StringConstants.VERIFY_TYPE, 0)


        binding?.swEtPwd?.etSwitch?.addTextChangedListener { text ->
            binding?.tvConfirm?.isEnabled = !TextUtils.isEmpty(text)
        }

        binding?.tvConfirm?.onClick(Constants.CLICK_INTERVAL) {

            var pwdStr = binding?.swEtPwd?.etSwitch?.text?.trim().toString()

            if (!AES.encrypt(pwdStr).equals(walletBean?.password)) {
                binding?.swEtPwd?.setErrorTip(getString(R.string.wallet_pwd_error))
                return@onClick
            }

            when (verifyType) {
                //备份助记词
                VERIFY_PHRASE -> {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_BACKUP_PHRASE_HINT_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                    finish()
                }
                //备份私钥
                VERIFY_PRIVATEKEY -> {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_KEY_ACTIVITY_PATH)
                        .withString(StringConstants.ADDRESS_PRIVATE_KEY, walletBean?.privateKey)
                        .navigation()
                    finish()
                }
                //touchid
                VERIFY_TOUCHID -> {
                    //切换touchid
                    changeTouchIdPay()
                    val dialog = NomalSuccessFailDialog(
                        R.mipmap.icon_dialog_uccess,
                        getString(R.string.complete),
                        getString(R.string.ok)
                    )
                    dialog.confirmEvent = {
                        finish()
                    }
                    dialog.show(supportFragmentManager, "")
                }
                //Add Passphrase
                VERIFY_PASSPHRASE -> {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_PASSPHRASE_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                    finish()
                }
                //删除钱包
                VERIFY_DELETE_WALLET -> {
                    deleteWallet()
                }
            }

        }

        binding?.tvCancel?.onClick {
            finish()
        }

    }

    private fun changeTouchIdPay() {
        lifecycleScope.launch {
            runCatching {
                walletBean ?: return@launch
                walletBean?.isOpenTouchIdPay = !walletBean?.isOpenTouchIdPay!!
                withContext(Dispatchers.IO) {
                    WalletDaoUtils.updateWallet(walletBean)
                }
                EventBus.getDefault()
                    .post(
                        MessageEvent.getInstance(
                            MessageEvent.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET,
                            walletBean!!
                        )
                    )
            }
        }
    }

    private fun deleteWallet() {
        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                walletBean?.id?.let {
                    TokenDaoUtil.deleteWallet(it)
                    ChildAddressDaoUtil.deleteChildAddressForWalletId(it)
                }
                WalletDaoUtils.deleteBackWallet(walletBean)
            }

            EventBus.getDefault()
                .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_DELETE_WALLET))

            val dialog = NomalSuccessFailDialog(
                R.mipmap.icon_dialog_uccess,
                getString(R.string.complete),
                getString(R.string.ok)
            )
            dialog.confirmEvent = {
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
                finish()
            }
            dialog.show(supportFragmentManager, "")

        }
    }

}