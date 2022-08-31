package com.linktech.saihub.ui.activity.wallet.create

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.github.iielse.switchbutton.SwitchView
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityLightningSettingsBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.ui.activity.wallet.manager.VerifyPwdActivity
import com.linktech.saihub.ui.dialog.ConfirmDialog
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.ui.dialog.SetWalletNameDialog
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *  闪电钱包设置
 * Created by tromo on 2022/6/23.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_SETTINGS_ACTIVITY_PATH)
class LightningSettingsActivity : BaseActivity() {

    private var binding: ActivityLightningSettingsBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private var walletBean: WalletBean? = null

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onInit() {
        super.onInit()
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lightning_settings)
        binding?.apply {
            tvWalletName.text = walletBean?.name
            tvWalletName.onClick(Constants.CLICK_INTERVAL) {
                val setWalletNameDialog = SetWalletNameDialog(tvWalletName.text.toString())
                setWalletNameDialog.confirmEvent = { name ->
                    if (!TextUtils.isEmpty(name))
                        walletBean?.id?.let { it1 ->
                            lightningViewModel.setWalletName(
                                it1,
                                name!!
                            )
                        }
                }
                setWalletNameDialog.showNow(supportFragmentManager, "")
            }

            tvHost.text = walletBean?.host
            initUI(walletBean)

            tvDelete.onClick(Constants.CLICK_INTERVAL) {
                val dialog = ConfirmDialog(
                    null,
                    getString(R.string.delete_ln_content),
                    resources.getColor(R.color.color_FFFF3750)
                )
                dialog.confirmEvent = {
                    deleteWallet()
                }
                dialog.show(supportFragmentManager, "")
            }

            btnExport.onClick(Constants.CLICK_INTERVAL) {
                walletBean?.id?.let { it1 -> lightningViewModel.getExportContent(it1) }
            }

            svPwd.onClick(Constants.CLICK_INTERVAL) {
                if (walletBean?.isLNOpenPwdPay == true)
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                        .withInt(StringConstants.VERIFY_TYPE, VerifyPwdActivity.VERIFY_LN_CLOSE_PWD)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                else
                    ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_SETTING_PWD_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
            }

            svTouchId.onClick(Constants.CLICK_INTERVAL) {
                if (walletBean?.isOpenTouchIdPay == true) {
                    walletBean?.id?.let { lightningViewModel.closeWalletTouchId(it, false) }
                } else {
                    if (walletBean?.isLNOpenPwdPay == false) {
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_LN_WALLET_SETTING_PWD_ACTIVITY_PATH)
                            .withParcelable(StringConstants.WALLET_DATA, walletBean)
                            .navigation()
                        return@onClick
                    }
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                        .withInt(StringConstants.VERIFY_TYPE, VerifyPwdActivity.VERIFY_TOUCHID)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                }
            }
        }
    }

    private fun initUI(walletBean: WalletBean?) {
        binding?.apply {
            openOrCloseSwitch(walletBean?.isLNOpenPwdPay == true, svPwd)
            if (walletBean?.isLNOpenPwdPay == false) {
                enableSwitch(svTouchId)
            } else {
                openOrCloseSwitch(walletBean?.isOpenTouchIdPay == true, svTouchId)
            }
        }
    }

    private fun openOrCloseSwitch(isOpen: Boolean, switch: ImageView) {
        switch.setImageResource(if (isOpen) R.mipmap.icon_switch_open else R.mipmap.icon_switch_close)
    }

    private fun enableSwitch(switch: ImageView) {
        switch.setImageResource(R.mipmap.icon_switch_enable)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshWalletData(event: MessageEvent) {
        when (event.id) {
            MessageEvent.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET,
            MessageEvent.MESSAGE_ID_CHANGE_PAY_PWD_LN_WALLET -> {
                walletAssetViewModel.getCurrentWallet(false)
            }
        }

    }

    override fun onData() {
        super.onData()
        walletAssetViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                walletBean = it
                initUI(walletBean)
            }
        }
        lightningViewModel.mTouchIdResultData.vmObserver(this) {
            onAppSuccess = {
                walletAssetViewModel.getCurrentWallet(false)
            }
        }
        lightningViewModel.mNameResultData.vmObserver(this) {
            onAppSuccess = {
                binding?.tvWalletName?.text = it.name
                ToastUtils.shortRightImageToast(getString(R.string.save_success))
                walletBean = it
                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET_ADDRESS))
            }
        }
        lightningViewModel.mExportResultData.vmObserver(this) {
            onAppSuccess = {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_LNDHUB_ACTIVITY_PATH)
                    .withString(StringConstants.ADDRESS_SHARE, it)
                    .navigation()
            }
        }
    }

    private fun deleteWallet() {
        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                WalletDaoUtils.deleteBackWallet(this@LightningSettingsActivity.walletBean)
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}