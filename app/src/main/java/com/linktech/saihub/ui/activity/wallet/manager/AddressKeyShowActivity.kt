package com.linktech.saihub.ui.activity.wallet.manager

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityAddressShowBinding
import com.linktech.saihub.manager.AES
import com.linktech.saihub.qrcode.DuQRCodeEncoder
import com.linktech.saihub.util.CopyUtil
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_KEY_ACTIVITY_PATH)
class AddressKeyShowActivity : BaseActivity() {
    var binding: ActivityAddressShowBinding? = null


    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_address_show)

        val addressKey = intent.getStringExtra(StringConstants.ADDRESS_PRIVATE_KEY)

        binding?.apply {
            tvAddressKey.text = AES.decrypt(addressKey)
            lifecycleScope.launch {
                val downQrcode = withContext(Dispatchers.IO) {
                    DuQRCodeEncoder.syncEncodeQRCode(
                        AES.decrypt(addressKey),
                        PixelUtils.dp2px(199F),
                        mContext?.resources?.getColor(R.color.black)!!,
                        mContext?.resources?.getColor(R.color.white)!!,
                        null
                    )
                    //CodeUtils.createQRCode(addressKey, PixelUtils.dp2px(193F))
                }
                ivQrcode.setImageBitmap(downQrcode)
                binding?.ivQrcode?.setPadding(0, 0, 0, 0)
            }

            tvConfirm.onClick {
                mContext?.let { it1 ->
                    if (addressKey != null) {
                        CopyUtil.copyCotent(it1, AES.decrypt(addressKey))
                    }
                }
            }

            clHide.onClick {
                clHide.setVisible(false)
            }
        }

    }
}