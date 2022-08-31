package com.linktech.saihub.ui.activity.wallet.transaction

import android.os.Bundle
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityLndHubBinding
import com.linktech.saihub.databinding.ActivityReceiveBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.qrcode.DuQRCodeEncoder
import com.linktech.saihub.ui.dialog.wallet.ShareDialog
import com.linktech.saihub.util.CommonUtil
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * lndhub导出
 * Created by tromo on 2022/6/23.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_LNDHUB_ACTIVITY_PATH)
class ExportLndHubActivity : BaseActivity() {

    private var binding: ActivityLndHubBinding? = null

    private var receiveAddress: String? = null

    override fun translucentStatusBar(): Boolean = true

    private val addressShare: String? by lazy {
        intent.getStringExtra(StringConstants.ADDRESS_SHARE)
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lnd_hub)
        binding?.apply {
            tvHub.text = addressShare

            llHub.onClick(Constants.CLICK_INTERVAL_500) {
                CommonUtil.copyText(
                    this@ExportLndHubActivity, addressShare,
                    getString(R.string.content_copy_success)
                )
            }
            addressShare?.let { loadQrcode(it) }
        }
    }

    private fun loadQrcode(address: String) {
        lifecycleScope.launch {
            val downQrcode = withContext(Dispatchers.IO) {
                DuQRCodeEncoder.syncEncodeQRCode(
                    address,
                    PixelUtils.dp2px(193F),
                    mContext?.resources?.getColor(R.color.black)!!,
                    mContext?.resources?.getColor(R.color.white)!!,
                    null
                )
                //CodeUtils.createQRCode(addressKey, PixelUtils.dp2px(193F))
            }
            binding?.ivQrcode?.setImageBitmap(downQrcode)
            binding?.ivQrcode?.setPadding(0, 0, 0, 0)
        }
    }
}