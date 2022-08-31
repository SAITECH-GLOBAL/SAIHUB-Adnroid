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
import com.linktech.saihub.databinding.ActivityReceiveBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
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
 * 收款二维码
 * Created by tromo on 2022/2/28.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_RECEIVE_ACTIVITY_PATH)
class ReceiveActivity : BaseActivity() {

    private var binding: ActivityReceiveBinding? = null

    private var childAddressList: List<String>? = null

    private var receiveAddress: String? = null

    override fun translucentStatusBar(): Boolean = true

    private val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val coin: String? by lazy {
        intent.getStringExtra(StringConstants.COIN)
    }

    private val addressShare: String? by lazy {
        intent.getStringExtra(StringConstants.ADDRESS_SHARE)
    }

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receive)
        binding?.apply {
            when (walletBean?.existType) {
                Constants.EXIST_MNEMONIC, Constants.EXIST_PUBLIC_KEY, Constants.EXIST_MULTI_SIG -> {
                    llRefresh.setVisible(true)
                }
                Constants.EXIST_PRIVATE_KEY, Constants.EXIST_ADDRESS, Constants.EXIST_LIGHTNING -> {
                    llRefresh.setVisible(false)
                }
            }
            receiveAddress = if (!TextUtils.isEmpty(addressShare)) {
                addressShare
            } else {
                walletBean?.address
            }
            receiveAddress?.let { setAddressData(it) }
            llRefresh.onClick {
                if (childAddressList?.isNotEmpty() == true) {
                    receiveAddress = childAddressList?.shuffled()?.take(1)?.get(0)
                    receiveAddress?.let { setAddressData(it) }
                    receiveAddress?.let { it1 ->
                        walletBean?.let { it2 ->
                            walletViewModel.updateMainAddressWallet(
                                it1,
                                it2
                            )
                        }
                    }
                }
            }
            llAddress.onClick(Constants.CLICK_INTERVAL_500) {
                CommonUtil.copyText(
                    this@ReceiveActivity, receiveAddress,
                    getString(R.string.copy_address_success)
                )
            }

            btnShare.onClick(Constants.CLICK_INTERVAL) {
                val shareDialog = ShareDialog()
                val bundle = Bundle()
                bundle.putString(StringConstants.COIN, coin)
                bundle.putString(StringConstants.ADDRESS, receiveAddress)
                shareDialog.arguments = bundle
                shareDialog.showNow(supportFragmentManager, "")
            }
        }
    }

    private fun setAddressData(address: String) {
        loadQrcode(address)
        binding?.tvAddress?.text = StringUtils.formatAddress(address)
    }

    override fun onData() {
        super.onData()
        getChildAddressList()

        walletViewModel.mChangeAddressData.vmObserver(this) {
            onAppSuccess = {
                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET_ADDRESS))
            }
        }
    }

    private fun getChildAddressList() {
        lifecycleScope.launch {
            val addressList = withContext(Dispatchers.IO) {
                walletBean?.addressType?.let {
                    ChildAddressDaoUtil.getChildAddressForTypeNotChange(it, walletBean?.id)
                }
            }
            childAddressList = addressList?.map {
                it.childAddress
            }
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