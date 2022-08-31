package com.linktech.saihub.ui.activity.wallet.manager

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySetPasswordBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 *  闪电钱包-设置密码
 * Created by tromo on 2022/6/23.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_SETTING_PWD_ACTIVITY_PATH)
class SetLNPasswordActivity : BaseActivity() {

    private var binding: ActivitySetPasswordBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_password)
        binding?.apply {
            clPassphrase.setVisible(false)

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
                walletBean?.id?.let { it1 ->
                    lightningViewModel.setWalletPwd(
                        it1,
                        etWalletPassword.getText()
                    )
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
        //密码设置观察者
        lightningViewModel.mPwdResultData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                if (it) {
                    EventBus.getDefault()
                        .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_PAY_PWD_LN_WALLET))
                    finish()
                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }

        }
    }

}