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
import com.linktech.saihub.databinding.ActivityManagerPassphraseBinding
import com.linktech.saihub.db.bean.ChildAddressBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.wallet.ManagerBTCWallet
import com.linktech.saihub.manager.wallet.MnemonicUtil
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_PASSPHRASE_ACTIVITY_PATH)
class ManagerPassphraseActivity : BaseActivity() {

    var binding: ActivityManagerPassphraseBinding? = null

    val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_passphrase)

        binding?.apply {
            swEtPhassphrase1.etSwitch?.addTextChangedListener {
                binding?.tvErrorHint?.setVisible(false)
                setEnableBt()
            }
            swEtPhassphrase2.etSwitch?.addTextChangedListener {
                binding?.tvErrorHint?.setVisible(false)
                setEnableBt()
            }
        }
        setFuncation()
    }

    private fun setEnableBt() {
        binding?.tvConfrim?.isEnabled =
            binding?.swEtPhassphrase1?.etSwitch?.text?.isNotEmpty() == true &&
                    binding?.swEtPhassphrase2?.etSwitch?.text?.isNotEmpty() == true
    }

    private fun setFuncation() {
        binding?.tvConfrim?.onClick(Constants.CLICK_INTERVAL) {
            val passphrase1 = binding?.swEtPhassphrase1?.etSwitch?.text?.trim()?.toString()
            val passphrase2 = binding?.swEtPhassphrase2?.etSwitch?.text?.trim()?.toString()
            if (!passphrase1.equals(passphrase2)) {
                binding?.tvErrorHint?.setVisible(true)
                binding?.tvErrorHint?.text = getString(R.string.pasaphrase_does_not_match)
//                ToastUtils.shortImageToast(getString(R.string.pasaphrase_does_not_match))
                return@onClick
            }
            if (passphrase1?.length!! > 128) {
                binding?.tvErrorHint?.setVisible(true)
                binding?.swEtPhassphrase1?.setErrorTipLine()
                binding?.swEtPhassphrase2?.setErrorTipLine()
                binding?.tvErrorHint?.text =
                    getString(R.string.passphrase_cannot_exceed_128_characters)
//                ToastUtils.shortImageToast(getString(R.string.passphrase_cannot_exceed_128_characters))
                return@onClick
            }
            convertWalletForPassphrase(passphrase1)
        }
    }

    private fun convertWalletForPassphrase(passphraseStr: String) {
        lifecycleScope.launch {
            runCatching {
                showLoading()
                withContext(Dispatchers.IO) {
                    WalletDaoUtils.deleteWallet(walletBean)
                    walletBean?.id?.let {
                        TokenDaoUtil.deleteWallet(it)
                        ChildAddressDaoUtil.deleteChildAddressForWalletId(it)
                    }
                    val walletDataForCreate =
                        ManagerBTCWallet.getInstance().createWalletForMnemonic(
                            walletBean?.name,
                            AES.decrypt(walletBean?.password),
                            MnemonicUtil.createMnemonic(AES.decrypt(walletBean?.mnemonic)),
                            walletBean?.addressType ?: 0,
                            passphraseStr
                        )
                    walletDataForCreate
                }
            }.onSuccess {
                withContext(Dispatchers.IO) {

                    val walletId: Long = WalletDaoUtils.insertNewWallet(it)

                    TokenDaoUtil.insertTokenForWalType(walletId, it, mContext)

                    val childAddressNormalBeans: List<ChildAddressBean> =
                        ManagerBTCWallet.getInstance().getChildAddressNormalBeans(it)

                    ChildAddressDaoUtil.insertChildAddress(childAddressNormalBeans, it)
                    WalletDaoUtils.updateCurrent(it.id)
                }

                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_SET_PASSPHRASE_CONVERT_WALLET,
                        it))
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
                hideLoading()
                binding?.tvConfrim?.postDelayed({ finish() }, 300)
            }.onFailure {
                hideLoading()
                ToastUtils.shortImageToast(getString(R.string.add_passphrase_wrong))
            }
        }
    }
}