package com.linktech.saihub.ui.activity.wallet.transaction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.gson.Gson
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityTransactionBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.databinding.FeeEntity
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.entity.wallet.js.JsFeeEntity
import com.linktech.saihub.entity.wallet.js.JsMultiSigEntity
import com.linktech.saihub.entity.wallet.js.JsSignEntity
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.model.WalletTransactionViewModel
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.formatJsReturn
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * 转账
 * Created by tromo on 2022/2/28.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_ACTIVITY_PATH)
class TransactionActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val RESULT_CODE = 0x12
    }

    private var binding: ActivityTransactionBinding? = null
    private var feeEntity: FeeEntity? = null
    private var transferSendBean: TransferSendBean? = null
    private var wvJs: WebView? = null

    override fun translucentStatusBar(): Boolean = true

    private var walletBean: WalletBean? = null

    private val feeIntentList: MutableList<FeeEntity>? by lazy {
        mutableListOf()
    }

    private var tokenBean: TokenInfoBean? = null

    private val walletTransactionViewModel by lazy {
        ViewModelProvider(this)[WalletTransactionViewModel::class.java]
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction)

        wvJs = WebView(this)
        val webSettings = wvJs?.settings
        //允许使用JS
        webSettings?.javaScriptEnabled = true
        wvJs?.loadUrl("file:///android_asset/web/index.html")
        wvJs?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        tokenBean = intent.getSerializableExtra(StringConstants.TOKEN_DATA) as? TokenInfoBean
        binding?.apply {

            feeIntentList?.add(FeeEntity())
            feeIntentList?.add(FeeEntity())
            feeIntentList?.add(FeeEntity())


            tvBalance.text = tokenBean?.tokenBalance
            tvCoin.text = tokenBean?.tokenShort
            walletBean?.let {
                if (tokenBean?.tokenShort == StringConstants.BTC)
                    walletAssetViewModel.getBTCBalance(it)
                else
                    walletAssetViewModel.getUSDTBalance(it)
            }

            //输入框初始化
            etAmount.etSwitch?.inputType = 8194
            etAmount.etSwitch?.typeface =
                Typeface.createFromAsset(assets, "fonts/montserrat_regular.ttf")

            etAddress.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            etAmount.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            //gas列表初始化
            rvFee.grid(3).setup {
                addType<FeeEntity>(R.layout.item_miner_fee)
                onClick(R.id.cl_root) {
                    val data = this.adapter._data as? List<FeeEntity>
                    for ((index, item) in data?.withIndex()!!) {
                        if (index == this.layoutPosition) {
                            item.isSelect = true
                            feeEntity = item
                        } else {
                            item.isSelect = false
                        }
                    }
                    rvFee.models = data
                }
            }.models = feeIntentList

            ivScan.onClick(Constants.CLICK_INTERVAL) {
                startZxingScan()
            }

            ivAddressBook.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ACTIVITY_PATH)
                    .withInt(StringConstants.LOAD_TYPE, Constants.ADDRESS_BOOK_SELECT)
                    .navigation(this@TransactionActivity, RESULT_CODE)
            }

            btnNext.onClick(Constants.CLICK_INTERVAL) {
                walletTransactionViewModel.submitTransaction(
                    etAddress.getText(),
                    etAmount.getText(),
                    tokenBean,
                    walletBean, feeEntity?.gasPrice
                )
            }
        }
        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val addressStr = CameraScan.parseScanResult(result.data)
                        binding?.etAddress?.setContentText(addressStr)
                    }
                }
            }
    }

    override fun onData() {
        super.onData()
        walletBean?.address?.let {
            tokenBean?.tokenShort?.let { it1 ->
                walletTransactionViewModel.getGasPrice(
                    walletBean!!,
                    it1
                )
            }
        }

        walletTransactionViewModel.mGasListData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                binding?.rvFee?.models = it
                feeEntity = it[1]
            }
            onAppError = {
                hideLoading()
            }
        }

        walletAssetViewModel.mBalanceData.vmObserver(this) {
            onAppSuccess = {
                walletBean?.id?.let { it1 ->
                    walletTransactionViewModel.getSingleBalanceData(
                        it1,
                        tokenBean?.tokenShort == StringConstants.BTC
                    )
                }
            }
        }

        walletTransactionViewModel.mTokenData.vmObserver(this) {
            onAppSuccess = {
                tokenBean = it
            }
        }

        walletTransactionViewModel.mFeeJsData.vmObserver(this) {
            onAppSuccess = {
                transferSendBean = it.second
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it.first) { value ->
                    runCatching {
                        LogUtils.e(value)
                        val jsFeeEntity =
                            Gson().fromJson(formatJsReturn(value), JsFeeEntity::class.java)
                        if (jsFeeEntity.isEnough == true) {
                            walletBean?.apply {
                                when (walletBean?.existType) {
                                    Constants.EXIST_MULTI_SIG ->
                                        walletTransactionViewModel.signMultiSigJs(it.second, this)
                                    Constants.EXIST_PUBLIC_KEY ->
                                        walletTransactionViewModel.signSingleSigJs(it.second, this)
                                    else ->
                                        walletTransactionViewModel.signTransactionJs(
                                            it.second,
                                            this
                                        )
                                }
                            }

                        } else {
                            ToastUtils.shortImageToast(getString(R.string.balance_not_sufficient))
                        }
                    }.onFailure { throwable ->
                        hideLoading()
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
        }

        walletTransactionViewModel.mSubmitData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                transferSendBean = it
                //判断钱包类型 并签名交易
                walletBean?.apply {
                    when (walletBean?.existType) {
                        Constants.EXIST_MULTI_SIG -> walletTransactionViewModel.signMultiSigJs(
                            it,
                            this
                        )

                        Constants.EXIST_PUBLIC_KEY -> walletTransactionViewModel.signSingleSigJs(
                            it,
                            this
                        )
                        else -> {
                            when (this.addressType) {
                                Constants.CHILD_ADDRESS_NESTED -> walletTransactionViewModel.signTransaction(
                                    it,
                                    this
                                )

                                Constants.CHILD_ADDRESS_NATIVE -> walletTransactionViewModel.signTransactionJs(
                                    it,
                                    this
                                )

                            }
                        }
                    }
                }

            }

            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        walletTransactionViewModel.mSignHashData.vmObserver(this) {
            onAppSuccess = {
                hideLoading()
                toVerifyPassword(it)
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        //交易签名 js观察者
        walletTransactionViewModel.mJsData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        val jsSignEntity =
                            if (it.contains("createSingleTransaction"))
                                Gson().fromJson(formatJsReturn(value), JsSignEntity::class.java)
                            else
                                Gson().fromJson(value, JsSignEntity::class.java)
                        jsSignEntity.fee?.let { it1 -> transferSendBean?.setGasSpend(it1) }
                        jsSignEntity.hex?.let { it1 ->
                            toVerifyPassword(it1)
                        }
                        hideLoading()
                    }.onFailure { throwable ->
                        hideLoading()
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        //多签创建交易观察者
        walletTransactionViewModel.mMultiSigUrCodeData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        hideLoading()
                        val jsMultiSigEntity =
                            Gson().fromJson(formatJsReturn(value), JsMultiSigEntity::class.java)
                        jsMultiSigEntity.fee?.let { it1 -> transferSendBean?.setGasSpend(it1) }
                        //获取到urcode跳转多签生成二维码界面
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_WALLET_TRANSACTION_MULTI_SIGN_ACTIVITY_PATH)
                            .withStringArrayList(
                                StringConstants.MULTI_SIG_UR_CODE,
                                jsMultiSigEntity.urcode as ArrayList<String>?
                            )
                            .withBoolean(StringConstants.IS_SINGLE_SIG, false)
                            .withSerializable(StringConstants.TRANSFER_SEND_DATA, transferSendBean)
                            .withParcelable(StringConstants.WALLET_DATA, walletBean)
                            .withSerializable(StringConstants.TOKEN_DATA, tokenBean)
                            .navigation()
                    }.onFailure { throwable ->
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        //单签创建交易观察者
        walletTransactionViewModel.mSingleSigUrCodeData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        hideLoading()
                        val jsMultiSigEntity =
                            Gson().fromJson(formatJsReturn(value), JsMultiSigEntity::class.java)
                        jsMultiSigEntity.fee?.let { it1 -> transferSendBean?.setGasSpend(it1) }
                        //获取到urcode跳转多签生成二维码界面
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_WALLET_TRANSACTION_MULTI_SIGN_ACTIVITY_PATH)
                            .withStringArrayList(
                                StringConstants.MULTI_SIG_UR_CODE,
                                jsMultiSigEntity.urcode as ArrayList<String>?
                            )
                            .withSerializable(StringConstants.TRANSFER_SEND_DATA, transferSendBean)
                            .withBoolean(StringConstants.IS_SINGLE_SIG, true)
                            .withParcelable(StringConstants.WALLET_DATA, walletBean)
                            .withSerializable(StringConstants.TOKEN_DATA, tokenBean)
                            .navigation()
                    }.onFailure { throwable ->
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        walletAssetViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                walletBean = it
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshWallet(event: MessageEvent) {
        when (event.id) {
            MessageEvent.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET -> {
                //创建钱包操作
                walletAssetViewModel.getCurrentWallet(false)
            }
        }
    }

    private fun toVerifyPassword(txHex: String) {
        ARouter.getInstance()
            .build(ARouterUrl.WAL_WALLET_TRANSACTION_DETAIL_ACTIVITY_PATH)
            .withSerializable(StringConstants.TRANSFER_SEND_DATA, transferSendBean)
            .withString(StringConstants.TRANSACTION_HEX, txHex)
            .withParcelable(StringConstants.WALLET_DATA, walletBean)
            .withSerializable(StringConstants.TOKEN_DATA, tokenBean)
            .navigation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CODE) {
            val address = data?.getStringExtra(StringConstants.ADDRESS)
            binding?.etAddress?.setContentText(address)
        }
    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnNext.isEnabled =
                !TextUtils.isEmpty(etAddress.getText()) && !TextUtils.isEmpty(etAmount.getText())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        val intent = Intent(this, WeChatQRCodeActivity::class.java)
        betweenActivityResultLauncher?.launch(intent)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        val perms = mutableListOf(Manifest.permission.CAMERA)
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setTitle(getString(R.string.confirm))
                .setRationale(getString(R.string.confirm))
                .setNegativeButton(getString(R.string.cancel))
                .setPositiveButton(getString(R.string.confirm))
                .build().show()
        }
    }

    var betweenActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private fun startZxingScan() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            val intent = Intent(this, WeChatQRCodeActivity::class.java)
            betweenActivityResultLauncher?.launch(intent)
        } else {
            // 没有权限，进行权限请求
            EasyPermissions.requestPermissions(
                this, getString(R.string.confirm),
                Constants.REQUEST_CODE_SCAN, Manifest.permission.CAMERA
            )
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}