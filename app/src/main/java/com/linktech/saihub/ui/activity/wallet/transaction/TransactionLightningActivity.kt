package com.linktech.saihub.ui.activity.wallet.transaction

import `in`.xiandan.countdowntimer.CountDownTimerSupport
import `in`.xiandan.countdowntimer.OnCountDownTimerListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import com.google.gson.Gson
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityTransactionLightningBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.lightning.DecodeInvoiceEntity
import com.linktech.saihub.entity.lightning.KeySendEntity
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity.Companion.TYPE_IMPORT
import com.linktech.saihub.util.DateUtils
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.SoftInputUtil
import com.linktech.saihub.util.system.appendAuth
import com.linktech.saihub.util.system.getKeySend
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


/**
 *  闪电钱包转账
 * Created by tromo on 2022/6/23.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_TRANSACTION_ACTIVITY_PATH)
class TransactionLightningActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val LN_PAY_CODE = 0x777
    }

    private var binding: ActivityTransactionLightningBinding? = null

    private var mTimer: CountDownTimerSupport? = null

    private var mInputType: String? = null

    private var mCallback: String? = null

    private var mInvoice: String? = null

    private var mUsedInput: String? = ""

    private var wvJs: WebView? = null

    private var mIsSoftInputShow: Boolean = false

    override fun translucentStatusBar(): Boolean = true

    private val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_lightning)
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
        binding?.apply {

            etInput.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    if (TextUtils.isEmpty(s)) {
                        resetUI()
                    } else {
                        if (!mIsSoftInputShow)
                            analyzeInput()
                    }
                    checkButtonState()
                }
            })

            val softInputUtil = SoftInputUtil()
            softInputUtil.attachSoftInput(etInput.etSwitch) { isSoftInputShow, softInputHeight, viewOffset ->
                mIsSoftInputShow = isSoftInputShow
                if (isSoftInputShow) {
//                    editText2.setTranslationY(et2.getTranslationY() - viewOffset)
                } else {
                    if (mUsedInput == etInput.getText()) {
                        return@attachSoftInput
                    }
                    mUsedInput = etInput.getText()
                    analyzeInput()
                }
            }

            etAmount.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    if (!TextUtils.isEmpty(s)) {
                        val rateEntity = CacheListManager.instance?.rateEntity
                        tvConvert.text =
                            when (RateAndLocalManager.getInstance(this@TransactionLightningActivity).curRateKind) {
                                RateAndLocalManager.RateKind.CNY -> "¥${
                                    NumberCountUtils.getLNConvert(
                                        s,
                                        rateEntity?.btcCny
                                    )
                                }"
                                RateAndLocalManager.RateKind.USD -> "$${
                                    NumberCountUtils.getLNConvert(
                                        s,
                                        rateEntity?.btcUsd
                                    )
                                }"
                                RateAndLocalManager.RateKind.RUB -> "₽${
                                    NumberCountUtils.getLNConvert(
                                        s,
                                        rateEntity?.btcRub
                                    )
                                }"
                            }
                    }
                    checkButtonState()
                }
            })

            ivScan.onClick(Constants.CLICK_INTERVAL) {
                startZxingScan()
            }

            btnPay.onClick(Constants.CLICK_INTERVAL) {
                if (walletBean?.isLNOpenPwdPay == true)
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_LN_WALLET_TRANSACTION_CONFIRM_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation(this@TransactionLightningActivity, LN_PAY_CODE)
                else
                    payInvoice()
            }

        }
    }

    private fun analyzeInput() {
        binding?.apply {
            if (!TextUtils.isEmpty(etInput.getText()))
                lightningViewModel.analyzeInput(
                    walletBean?.host!!,
                    appendAuth(walletBean?.accessToken!!),
                    etInput.getText()
                )
            else
                resetUI()
        }
    }

    private fun payInvoice() {
        if (mInputType == StringConstants.INPUT_TYPE_INVOICE) {
            mInvoice?.let { it1 ->
                lightningViewModel.payInvoice(
                    walletBean?.host!!,
                    appendAuth(walletBean?.accessToken!!),
                    it1, binding?.etAmount?.text.toString()
                )
            }
        } else {
            mCallback?.let {
                lightningViewModel.getKeySendCallback(
                    walletBean?.host!!,
                    it,
                    appendAuth(walletBean?.accessToken!!),
                    binding?.etAmount?.text.toString().toLong() * 1000,
                    System.currentTimeMillis(),
                    ""
                )
            }
        }
    }

    override fun onData() {
        super.onData()
        lightningViewModel.mInputTypeData.vmObserver(this) {
            onAppSuccess = {
                mInputType = it
            }
            onAppError = {
                dismissSoftKeyboard()
                resetUI()
                ToastUtils.shortImageToast("not a valid invoice")
            }
        }
        //解析invoice
        lightningViewModel.mDecodeData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                dismissSoftKeyboard()
                hideLoading()
                initInvoiceData(it)
            }
            onAppError = {
                dismissSoftKeyboard()
                hideLoading()
                resetUI()
                ToastUtils.shortToast(it.errorMsg)
            }
        }
        //解析keySend
        lightningViewModel.mKeySendResultData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                dismissSoftKeyboard()
                hideLoading()
                initKeySendData(it)
            }
            onAppError = {
                dismissSoftKeyboard()
                hideLoading()
                resetUI()
                ToastUtils.shortToast(it.errorMsg)
            }
        }
        //支付结果观察者
        lightningViewModel.mPayResultData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                ToastUtils.shortRightImageToast(getString(R.string.pay_success))
                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_REFRESH_RECORD_LN_WALLET))
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

        //decodeInvoice js观察者
        lightningViewModel.mDecodeLocalData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        val decodeInvoiceEntity =
                            Gson().fromJson(value, DecodeInvoiceEntity::class.java)
                        dismissSoftKeyboard()
                        hideLoading()
                        initInvoiceData(decodeInvoiceEntity)
                    }.onFailure {
                        dismissSoftKeyboard()
                        hideLoading()
                        ToastUtils.shortImageToast("not a valid invoice")
                    }

                }
            }
            onAppError = {
                dismissSoftKeyboard()
                hideLoading()
                resetUI()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val addressStr = CameraScan.parseScanResult(result.data)
                        binding?.etInput?.setContentText(addressStr)
                        analyzeInput()
                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initKeySendData(keySendEntity: KeySendEntity) {
        binding?.apply {
            llOutTime.setVisible(false)
            llPayFee.setVisible(false)
            llPayDomain.setVisible(true)
            llPayInterval.setVisible(true)
            tvPayInterval.text =
                "${keySendEntity.minSendable?.div(1000)}-${keySendEntity.maxSendable?.div(1000)}"
            tvDomain.text =
                "${getKeySend(etInput.getText())[0]}  ${getKeySend(etInput.getText())[1]}"
            mCallback = keySendEntity.callback
            etAmount.isEnabled = true
            btnPay.isEnabled = true
        }
    }

    private fun initInvoiceData(decodeInvoiceEntity: DecodeInvoiceEntity) {
        binding?.apply {
            llOutTime.setVisible(true)
            llPayFee.setVisible(true)
            llPayDomain.setVisible(false)
            llPayInterval.setVisible(false)
            etAmount.setText(decodeInvoiceEntity.numSatoshis)
            startTimer(
                decodeInvoiceEntity.timestamp?.toLong(),
                decodeInvoiceEntity.expiry?.toLong()
            )
            tvFee.text = NumberCountUtils.getLnFee(decodeInvoiceEntity.numSatoshis)
            mInvoice = etInput.getText()
            etAmount.isEnabled = false
            btnPay.isEnabled = true
        }
    }

    private fun resetUI() {
        binding?.apply {
            llOutTime.setVisible(false)
            llPayFee.setVisible(false)
            llPayDomain.setVisible(false)
            llPayInterval.setVisible(false)
            etAmount.setText("0")
            mTimer?.stop()
            etAmount.isEnabled = true
            btnPay.isEnabled = false
        }
    }

    private fun startTimer(timestamp: Long?, expiry: Long?) {
        mTimer?.stop()
        val millisInFuture =
            (timestamp!! + expiry!!) * 1000 - System.currentTimeMillis()
        if (millisInFuture < 0) {
            binding?.tvTime?.text = getString(R.string.out_of_date)
            return
        }
        //总时长 间隔时间
        mTimer = CountDownTimerSupport(millisInFuture, 1000)
        mTimer?.setOnCountDownTimerListener(object : OnCountDownTimerListener {
            override fun onTick(millisUntilFinished: Long) {
                // 倒计时间隔
                binding?.tvTime?.text =
                    DateUtils.getCountDownTimeFormat(millisUntilFinished)
            }

            override fun onFinish() {
                // 倒计时结束
                binding?.tvTime?.text = getString(R.string.out_of_date)
            }

            override fun onCancel() {
                // 倒计时手动停止
            }
        })
        mTimer?.start()
    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnPay.isEnabled = !TextUtils.isEmpty(etAmount.text)
                    && !TextUtils.isEmpty(etInput.getText())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == LN_PAY_CODE)
            payInvoice()
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
        startScanActivity()
    }

    private fun startScanActivity() {
        val intent = Intent(this, WeChatQRCodeActivity::class.java)
        intent.putExtra(StringConstants.LOAD_TYPE, TYPE_IMPORT)
        betweenActivityResultLauncher?.launch(intent)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        val permsRequest =
            mutableListOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        if (EasyPermissions.somePermissionPermanentlyDenied(this, permsRequest)) {
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
        if (EasyPermissions.hasPermissions(
                this,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            startScanActivity()
        } else {
            // 没有权限，进行权限请求
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.confirm),
                Constants.REQUEST_CODE_SCAN,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.stop()
    }
}