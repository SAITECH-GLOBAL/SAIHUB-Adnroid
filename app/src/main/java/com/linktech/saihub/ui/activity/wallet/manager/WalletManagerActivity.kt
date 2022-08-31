package com.linktech.saihub.ui.activity.wallet.manager

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.EXIST_MULTI_SIG
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityWalletManagerBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.manager.ActivityManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.ui.dialog.ConfirmDialog
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.ui.dialog.SetWalletNameDialog
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.getRandomName
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ACTIVITY_PATH)
class WalletManagerActivity : BaseActivity() {

    var binding: ActivityWalletManagerBinding? = null

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    var walletBean: WalletBean? = null
    override fun onInit() {
        super.onInit()
        EventBus.getDefault().register(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_manager)
        walletBean = intent?.getParcelableExtra(StringConstants.WALLET_DATA)
        //todo zxx
        walletBean = WalletDaoUtils.getCurrent()

        initFing()
        addEvent()
        setWalletPermission()
    }

    private fun setWalletPermission() {
        binding?.apply {
            walletBean?.let {
                when (it.existType) {
                    //助记词类型
                    Constants.EXIST_MNEMONIC -> {
                        llManager.setVisible(true)
                        tvBackupPrivateKeyOrMnemonicHint.text =
                            getString(R.string.backup_recovery_phrase)
                        clBackupPrivateKeyOrMnemonic.setVisible(true)
                        clModifyPassword.setVisible(true)
                        clTouchId.setVisible(true)
                        clPassphrase.setVisible(true)
                        clAddressManager.setVisible(true)
                    }
                    //私钥类型
                    Constants.EXIST_PRIVATE_KEY -> {
                        llManager.setVisible(true)
                        tvBackupPrivateKeyOrMnemonicHint.text =
                            getString(R.string.backup_private_key)
                        clBackupPrivateKeyOrMnemonic.setVisible(true)
                        clModifyPassword.setVisible(true)
                        clTouchId.setVisible(true)
                        clPassphrase.setVisible(false)
                        clAddressManager.setVisible(false)
                    }
                    //拓展公钥类型 多签类型
                    Constants.EXIST_PUBLIC_KEY, EXIST_MULTI_SIG -> {
                        llManager.setVisible(true)
                        clBackupPrivateKeyOrMnemonic.setVisible(false)
                        clModifyPassword.setVisible(false)
                        clTouchId.setVisible(false)
                        clPassphrase.setVisible(false)
                        clAddressManager.setVisible(true)
                    }
                    //普通地址类型
                    Constants.EXIST_ADDRESS -> {
                        llManager.setVisible(false)
                        clBackupPrivateKeyOrMnemonic.setVisible(false)
                        clModifyPassword.setVisible(false)
                        clTouchId.setVisible(false)
                        clPassphrase.setVisible(false)
                        clAddressManager.setVisible(false)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshWalletData(event: MessageEvent) {
        when (event.id) {
            MessageEvent.MESSAGE_ID_CHANGE_RESET_PWD_WALLET,
            MessageEvent.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET,
            MessageEvent.MESSAGE_ID_CHANGE_PWD_WALLET -> {
                walletBean = event.walletBean
                refreshUi()
            }
        }

    }

    private fun refreshUi() {
        binding?.ivTouchidFing?.setImageResource(
            if (fingerprintIdentify?.isFingerprintEnable == true && walletBean?.isOpenTouchIdPay == true)
                R.mipmap.icon_switch_open else R.mipmap.icon_switch_close
        )
    }

    private var fingerprintIdentify: FingerprintIdentify? = null
    private fun initFing() {
        fingerprintIdentify = FingerprintIdentify(SaiHubApplication.getInstance())
        fingerprintIdentify?.setSupportAndroidL(true)
        fingerprintIdentify?.init()
        refreshUi()
    }

    private fun addEvent() {

        binding?.apply {
            tvWalletName.text = walletBean?.name

            clWalletName.onClick(Constants.CLICK_INTERVAL) {
                val setWalletNameDialog = SetWalletNameDialog(tvWalletName.text.toString())
                setWalletNameDialog.confirmEvent = {
                    if (!TextUtils.isEmpty(it))
                        walletBean?.let { it1 -> walletViewModel.updateWalletName(it!!, it1) }
                }
                setWalletNameDialog.showNow(supportFragmentManager, "")
            }
        }

        walletViewModel.mChangeNameData.vmObserver(this) {
            onAppSuccess = {
                walletBean = it
                binding?.tvWalletName?.text = walletBean?.name
                ToastUtils.shortRightImageToast(getString(R.string.save_success))
                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET_ADDRESS))
            }
        }

        //备份私钥或者助记词
        binding?.clBackupPrivateKeyOrMnemonic?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .withInt(
                    StringConstants.VERIFY_TYPE, if (walletBean?.mnemonic?.isNotEmpty() == true)
                        VerifyPwdActivity.VERIFY_PHRASE else VerifyPwdActivity.VERIFY_PRIVATEKEY
                )
                .navigation()
        }
        //修改密码
        binding?.clModifyPassword?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_MODIFY_PASSWORD_ACTIVITY_PATH)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .navigation()
        }
        //touchID
        binding?.clTouchId?.onClick(Constants.CLICK_INTERVAL) {
            if (fingerprintIdentify?.isFingerprintEnable != true) {
                ToastUtils.shortToast(getString(R.string.open_touchid_hint))
                return@onClick
            }

            if (walletBean?.isOpenTouchIdPay == true) {
                walletBean?.isOpenTouchIdPay = false
                lifecycleScope.launch {
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
                return@onClick
            }

            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                .withInt(StringConstants.VERIFY_TYPE, VerifyPwdActivity.VERIFY_TOUCHID)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .navigation()
        }
        //钱包设置盐
        binding?.clPassphrase?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .withInt(StringConstants.VERIFY_TYPE, VerifyPwdActivity.VERIFY_PASSPHRASE)
                .navigation()
        }
        //管理地址
        binding?.clAddressManager?.onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_LIST_ACTIVITY_PATH)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .navigation()
        }
        //删除钱包
        binding?.tvDelete?.onClick(Constants.CLICK_INTERVAL) {

            val dialog = if (walletBean?.existType == Constants.EXIST_MNEMONIC)
                ConfirmDialog(isShowBack = true)
            else
                ConfirmDialog()

            dialog.confirmEvent = {
                if (walletBean?.isObserver == true || walletBean?.existType == EXIST_MULTI_SIG) {
                    deleteWallet()
                } else {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                        .withInt(
                            StringConstants.VERIFY_TYPE,
                            VerifyPwdActivity.VERIFY_DELETE_WALLET
                        )
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                }
            }

            dialog.backupEvent = {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withInt(
                        StringConstants.VERIFY_TYPE, if (walletBean?.mnemonic?.isNotEmpty() == true)
                            VerifyPwdActivity.VERIFY_PHRASE else VerifyPwdActivity.VERIFY_PRIVATEKEY
                    )
                    .navigation()
            }

            dialog.show(supportFragmentManager, "")

        }

    }

    private fun deleteWallet() {
        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                WalletDaoUtils.deleteBackWallet(this@WalletManagerActivity.walletBean)
                walletBean?.id?.let {
                    TokenDaoUtil.deleteWallet(it)
                    ChildAddressDaoUtil.deleteChildAddressForWalletId(it)
                }
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