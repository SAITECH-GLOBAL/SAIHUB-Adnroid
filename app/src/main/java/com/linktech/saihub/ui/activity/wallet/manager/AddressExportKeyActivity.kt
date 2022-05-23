package com.linktech.saihub.ui.activity.wallet.manager

import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityManagerAddressExportKeyBinding
import com.linktech.saihub.db.bean.ChildAddressBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.manager.AES
import com.qmuiteam.qmui.kotlin.onClick


@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_EXPORT_PRIVATEKEY_ACTIVITY_PATH)
class AddressExportKeyActivity : BaseActivity() {

    var binding: ActivityManagerAddressExportKeyBinding? = null

    val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }
    var addressBean: ChildAddressBean? = null

    override fun onInit() {
        super.onInit()
        addressBean = intent.getSerializableExtra(StringConstants.ADDRESS_CHILD_DATA) as ChildAddressBean?

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_address_export_key)

        binding?.apply {

            swEtPwd.etSwitch?.addTextChangedListener {
                tvConfirm.isEnabled =swEtPwd.etSwitch?.text?.isNotEmpty()==true
            }

            tvConfirm.onClick(Constants.CLICK_INTERVAL) {
                val pwdStr = swEtPwd.etSwitch?.text?.trim().toString()

                if (!AES.encrypt(pwdStr).equals(walletBean?.password)) {
                    swEtPwd.setErrorTip(getString(R.string.wallet_pwd_error))
                    return@onClick
                }

                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_KEY_ACTIVITY_PATH)
                    .withString(StringConstants.ADDRESS_PRIVATE_KEY, addressBean?.privateKey)
                    .navigation()
                finish()
            }

            tvCancel.onClick {
                finish()
            }
        }
    }


}

