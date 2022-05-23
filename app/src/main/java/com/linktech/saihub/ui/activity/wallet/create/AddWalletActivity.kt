package com.linktech.saihub.ui.activity.wallet.create

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityAddWalletBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.util.RegexUtils
import com.linktech.saihub.util.system.getAgreementUrl
import com.linktech.saihub.util.system.getRandomName
import com.linktech.saihub.util.system.getTermServiceUrl
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick

/**
 *  创建钱包
 * Created by tromo on 2022/2/18.
 */
@Route(path = ARouterUrl.WAL_WALLET_ADD_ACTIVITY_PATH)
class AddWalletActivity : BaseActivity() {

    private var binding: ActivityAddWalletBinding? = null

    override fun translucentStatusBar(): Boolean = true


    //地址类型
    private var mAddressType = -1

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_wallet)
        binding?.apply {
            //生成随机钱包名称
            tvWalletName.text = "BTC-${getRandomName()}"

            tvImport.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_IMPORT_ACTIVITY_PATH)
                    .navigation()
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

            btnCreate.onClick(Constants.CLICK_INTERVAL) {
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

                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_PHRASE_TYPE_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, WalletBean().apply {
                        this.name = tvWalletName.text.toString()
                        this.password = etWalletPassword.getText()
                        this.addressType = mAddressType
                    })
                    .navigation()
//                etWalletPassword.setErrorTip(getString(R.string.wallet_pwd_error))
            }

            rbNative.onClick(Constants.CLICK_INTERVAL) {
                mAddressType = Constants.CHILD_ADDRESS_NATIVE
                checkButtonState()
            }

            rbNested.onClick(Constants.CLICK_INTERVAL) {
                mAddressType = Constants.CHILD_ADDRESS_NESTED
                checkButtonState()
            }

            cbAgreement.setOnCheckedChangeListener { _, _ ->
                checkButtonState()
            }

            tvTerm.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
                    .withString(StringConstants.TITLE, "")
                    .withString(
                        StringConstants.KEY_URL,
                        getTermServiceUrl(RateAndLocalManager.getInstance(this@AddWalletActivity).curLocalLanguageKind)
                    )
                    .navigation()
            }
        }
    }

    override fun onData() {
        super.onData()
    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnCreate.isEnabled =
                mAddressType != -1 && !TextUtils.isEmpty(etWalletPassword.getText()) && !TextUtils.isEmpty(
                    etWalletPasswordRepeat.getText()
                ) && cbAgreement.isChecked
        }
    }

}