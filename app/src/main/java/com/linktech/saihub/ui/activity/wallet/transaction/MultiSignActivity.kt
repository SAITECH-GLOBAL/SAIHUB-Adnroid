package com.linktech.saihub.ui.activity.wallet.transaction

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.app.StringConstants.IS_SINGLE_SIG
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityMultiSignBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletTransactionViewModel
import com.linktech.saihub.qrcode.DuQRCodeEncoder
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.countDownCoroutines
import com.qmuiteam.qmui.kotlin.onClick
import com.sparrowwallet.hummingbird.URDecoder
import com.sparrowwallet.hummingbird.registry.RegistryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


/**
 * 多签交易界面 单签也用了
 * Created by tromo on 2022/3/1.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_MULTI_SIGN_ACTIVITY_PATH)
class MultiSignActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val MIN_FRAGMENT_LENGTH = 200
        const val MAX_FRAGMENT_LENGTH = 250
    }

    private var binding: ActivityMultiSignBinding? = null
    private var psbt: String? = null
    private var psbtSource: String? = null

    override fun translucentStatusBar(): Boolean = true

    private val isSingleSig: Boolean? by lazy {
        intent.getBooleanExtra(StringConstants.IS_SINGLE_SIG, false)
    }

    private val urList: ArrayList<String?> by lazy {
        intent.getStringArrayListExtra(StringConstants.MULTI_SIG_UR_CODE) as ArrayList<String?>
    }

    private val transferSendBean: TransferSendBean? by lazy {
        intent.getSerializableExtra(StringConstants.TRANSFER_SEND_DATA) as? TransferSendBean
    }

    private val tokenInfoBean: TokenInfoBean? by lazy {
        intent.getSerializableExtra(StringConstants.TOKEN_DATA) as? TokenInfoBean
    }

    private val walletTransactionViewModel by lazy {
        ViewModelProvider(this)[WalletTransactionViewModel::class.java]
    }

    private var walletBean: WalletBean? = null

    private var wvJs: WebView? = null

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multi_sign)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)
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
            btnScan.onClick(Constants.CLICK_INTERVAL) {
                startZxingScan()
            }
        }
    }

    override fun onData() {
        super.onData()

        //生成二维码
        runCatching {
            if (urList.size == 1) {
                urList[0]?.let { loadQrcode(it) }
            } else {
                var i = 0
                countDownCoroutines(150, lifecycleScope, onTick = {
                    urList[i]?.let { loadQrcode(it) }
                    i++
                    if (i == urList.size)
                        i = 0
                })
            }
        }

        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val resultType = result.data?.getStringExtra(StringConstants.RESULT_TYPE)
                        if (resultType == RegistryType.CRYPTO_PSBT.type) {
                            //解析扫描数据
                            psbt = CameraScan.parseScanResult(result.data)
                            psbtSource = getSourcePSBTData()
                            psbt?.let {
                                walletTransactionViewModel.isSameTransaction(it,
                                    psbtSource!!)
                            }

                        } else {
                            ToastUtils.shortImageToast(getString(R.string.illegal_data))
                        }

                    }
                }
            }

        walletTransactionViewModel.mSameTransactionJsData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        hideLoading()
                        if (value == "true") {
                            psbt?.let {
                                ARouter.getInstance()
                                    .build(ARouterUrl.WAL_WALLET_TRANSACTION_MULTI_SIGN_DETAIL_ACTIVITY_PATH)
                                    .withString(StringConstants.PSBT_DATA, psbt)
                                    .withString(StringConstants.SOURCE_PSBT_DATA, psbtSource)
                                    .withBoolean(IS_SINGLE_SIG, isSingleSig == true)
                                    .withSerializable(StringConstants.TRANSFER_SEND_DATA,
                                        transferSendBean)
                                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                                    .withSerializable(StringConstants.TOKEN_DATA, tokenInfoBean)
                                    .navigation()

                            }
                        } else {
                            ToastUtils.shortImageToast(getString(R.string.not_same_tip))
                        }

                    }.onFailure { throwable ->
                        hideLoading()
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
            onAppError = {
                hideLoading()
            }
        }
    }

    private fun getSourcePSBTData(): String {
        val urDecoder = URDecoder()
        for (item in urList) {
            urDecoder.receivePart(item)
        }
        return if (urDecoder.result != null) {
            urDecoder.result.ur.toString()
        } else {
            ""
        }
    }

    private fun loadQrcode(content: String) {
        lifecycleScope.launch {
            val downQrcode = withContext(Dispatchers.IO) {
                DuQRCodeEncoder.syncEncodeQRCode(
                    content,
                    PixelUtils.dp2px(250F),
                    mContext?.resources?.getColor(R.color.black)!!,
                    mContext?.resources?.getColor(R.color.white)!!,
                    null
                )
            }
            binding?.ivQrcode?.setImageBitmap(downQrcode)
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
}