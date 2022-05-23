package com.linktech.saihub.ui.activity.wallet.imp

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.github.iielse.switchbutton.SwitchView
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySetPasswordBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 *  导入钱包-设置密码
 * Created by tromo on 2022/2/24.
 */
@Route(path = ARouterUrl.WAL_WALLET_IMPORT_SET_PWD_ACTIVITY_PATH)
class SetPasswordActivity : BaseActivity() {

    private var binding: ActivitySetPasswordBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val importType: Int? by lazy {
        intent.getIntExtra(StringConstants.IMPORT_TYPE, Constants.VALIDATE_MNEMONIC)
    }

    private val importData: String? by lazy {
        intent.getStringExtra(StringConstants.IMPORT_DATA)
    }

    private val walletIntentBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_password)
        binding?.apply {
            //私钥类型导入 隐藏passphrase
            clPassphrase.setVisible(importType == Constants.VALIDATE_MNEMONIC)

            btnConfirm.onClick(Constants.CLICK_INTERVAL) {
                if (etWalletPassword.getText().length < 8) {
                    etWalletPassword.setErrorTipLine()
                    etWalletPasswordRepeat.setErrorOnlyTip(getString(R.string.pwd_tip))
//                    ToastUtils.shortImageToast(getString(R.string.pwd_tip))
                    return@onClick
                }
                if (etWalletPassword.getText() != etWalletPasswordRepeat.getText()) {
                    etWalletPasswordRepeat.setErrorTip(getString(R.string.pwd_repeat_tip))
//                    ToastUtils.shortImageToast(getString(R.string.pwd_repeat_tip))
                    return@onClick
                }
                if (importType == Constants.VALIDATE_MNEMONIC) {
                    walletIntentBean.apply {
                        this?.password = etWalletPassword.getText()
                        this?.passphrase = etPassphrase.getText()
                    }?.let { it1 ->
                        importData?.let { it2 ->
                            walletViewModel.importWallet(
                                importType!!,
                                it1, it2, this@SetPasswordActivity
                            )
                        }
                    }
                } else {
                    walletIntentBean.apply {
                        this?.password = etWalletPassword.getText()
                    }?.let { it1 ->
                        importData?.let { it2 ->
                            importType?.let { it3 ->
                                walletViewModel.importWallet(
                                    it3,
                                    it1,
                                    it2, this@SetPasswordActivity
                                )
                            }
                        }
                    }
                }

            }

            tvCancel.onClick(Constants.CLICK_INTERVAL) {
                finish()
            }

            etWalletPassword.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    psvPwd.setStrengthMode(RegexUtils.checkPassWord(s))
                    checkButtonState()
                }
            })

            etWalletPasswordRepeat.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            svPassphrase.setOnStateChangedListener(object : SwitchView.OnStateChangedListener {
                override fun toggleToOn(view: SwitchView?) {
                    etPassphrase.setVisible(true)
                    svPassphrase.toggleSwitch(true)
                }

                override fun toggleToOff(view: SwitchView?) {
                    etPassphrase.setVisible(false)
                    svPassphrase.toggleSwitch(false)
                }

            })

        }
    }

    private fun checkButtonState() {
        binding?.apply {
            btnConfirm.isEnabled =
                !TextUtils.isEmpty(etWalletPassword.getText()) && !TextUtils.isEmpty(
                    etWalletPasswordRepeat.getText()
                )
        }
    }

    override fun onData() {
        super.onData()
        //导入结果观察者
        walletViewModel.mImportData.vmObserver(this) {
            onAppLoading = {
                showLoadingFullScreen()
                loadingFullScreenDialog?.setText(getString(R.string.import_loading))
            }
            onAppSuccess = {
                hideLoadingFullScreen()
                if (it) {
                    EventBus.getDefault().postSticky(SocketEvent())
                    EventBus.getDefault()
                        .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET))
                    ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                        .navigation()
                }
            }
            onAppError = {
                hideLoadingFullScreen()
                ToastUtils.shortImageToast(it.errorMsg)
            }

        }
    }
}