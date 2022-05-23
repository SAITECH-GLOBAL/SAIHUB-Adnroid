package com.linktech.saihub.ui.activity.wallet.transaction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityMultiSignDetailBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.databinding.MultiSigNumEntity
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.model.WalletTransactionViewModel
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import com.sparrowwallet.hummingbird.registry.RegistryType
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * 多签交易详情界面 单签也用了
 * Created by tromo on 2022/3/1.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_MULTI_SIGN_DETAIL_ACTIVITY_PATH)
class MultiSignDetailActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    private var binding: ActivityMultiSignDetailBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val transferSendBean: TransferSendBean? by lazy {
        intent.getSerializableExtra(StringConstants.TRANSFER_SEND_DATA) as? TransferSendBean
    }

    private val isSingleSig: Boolean? by lazy {
        intent.getBooleanExtra(StringConstants.IS_SINGLE_SIG, false)
    }

    private val psbtSource: String? by lazy {
        intent.getStringExtra(StringConstants.SOURCE_PSBT_DATA)
    }

    private val dataList: MutableList<MultiSigNumEntity>? by lazy {
        mutableListOf()
    }

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    private var wvJs: WebView? = null

    private var psbtData: String? = null

    private var txHex: String? = null

    //是否签名完成
    private var isSigned: Boolean = false

    private val tokenInfoBean: TokenInfoBean? by lazy {
        intent.getSerializableExtra(StringConstants.TOKEN_DATA) as? TokenInfoBean
    }

    private var walletBean: WalletBean? = null

    private val walletTransactionViewModel by lazy {
        ViewModelProvider(this)[WalletTransactionViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multi_sign_detail)
        wvJs = WebView(this)
        val webSettings = wvJs?.settings
        //允许使用JS
        webSettings?.javaScriptEnabled = true
        wvJs?.loadUrl("file:///android_asset/web/index.html")
        wvJs?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isSingleSig == true) {
                    psbtData?.let { parsePSBTSingleSig(it) }
                } else {
                    psbtData?.let { parsePSBT(it) }
                }
            }
        }
        showLoading()
        psbtData = intent.getStringExtra(StringConstants.PSBT_DATA)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)

        binding?.apply {

            //顶部信息
            tvAddress.text = transferSendBean?.toAddress
            tvCoin.text = transferSendBean?.tokenName
            tvNum.text = transferSendBean?.moneyNumber
            tvConvert.text = transferSendBean?.convert
            tvFee.text =
                "${getString(R.string.fee_desc)}${transferSendBean?.gas}${StringConstants.BTC}(${transferSendBean?.gasConvert})"

            rvSignResult.linear().setup {
                addType<MultiSigNumEntity>(R.layout.item_sign_result)
            }.models = dataList

            btnResult.onClick(Constants.CLICK_INTERVAL) {
                if (isSigned) {
                    txHex?.let { walletTransactionViewModel.sendTransaction(it) }
                } else {
                    startZxingScan()
                }
            }

            btnNext.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WALLET_TRANSACTION_RECORD_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withSerializable(StringConstants.TOKEN_DATA, tokenInfoBean)
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .navigation(this@MultiSignDetailActivity)
            }
        }

    }

    //解析psbt获取hex
    private fun parsePSBTSingleSig(psbtData: String) {
        isSigned = true
        binding?.btnResult?.isEnabled = true
        binding?.btnResult?.text = getString(R.string.send_now)
        setSingleSingerData()
        walletTransactionViewModel.singlePsbtToHex(psbtData)
    }

    //解析psbt获取多签签名信息
    private fun parsePSBT(psbtData: String) {
        walletBean?.let { it1 ->
            walletTransactionViewModel.getTransactionSignedNum(psbtData,
                it1)
        }
    }

    override fun onData() {
        super.onData()
        walletTransactionViewModel.mTxHashData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                transferSendBean?.let { it1 ->
                    walletAssetViewModel.saveLocalTransactionRecord(
                        it1,
                        it, walletBean?.id
                    )
                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }
        walletTransactionViewModel.mMultiSigNumData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        setSignerData(value.toInt())
                        binding?.btnResult?.isEnabled = true
                        if ((walletBean?.policy?.split(",")?.get(0)?.toInt())?.let { it1 ->
                                value.toInt().compareTo(it1)
                            }!! >= 0) {
                            isSigned = true
                            binding?.btnResult?.text = getString(R.string.send_now)
                            psbtData?.let { it1 -> walletTransactionViewModel.psbtBase64ToHex(it1) }
                        } else {
                            isSigned = false
                            binding?.btnResult?.text = getString(R.string.resignature)
                            hideLoading()
                        }

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

        walletTransactionViewModel.mMultiSigHexData.vmObserver(this) {
            onAppSuccess = {

                //hex
                wvJs?.evaluateJavascript(it) { value ->
                    txHex = value.replace("\"", "")
                }
                hideLoading()
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        walletAssetViewModel.mInsertRecordData.vmObserver(this) {
            onAppSuccess = {
                hideLoading()
//                ToastUtils.shortRightImageToast(getString(R.string.send_success_tip))
                binding?.llSuccess?.setVisible(true)
                binding?.gpSign?.setVisible(false)
            }
            onAppError = {
                hideLoading()
                it.errorMsg?.let { it1 -> ToastUtils.shortImageToast(it1) }
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
                            psbtData = CameraScan.parseScanResult(result.data)
                            psbtData?.let {
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
            onAppSuccess = { js ->
                wvJs?.evaluateJavascript(js) { value ->
                    runCatching {
                        LogUtils.e(value)
                        hideLoading()
                        if (value == "true") {
                            psbtData?.let { parsePSBT(it) }
                        } else {
                            hideLoading()
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

    private fun setSignerData(signNum: Int) {
        dataList?.clear()
        val m = walletBean?.policy?.split(",")?.get(0)?.toInt()
        if (signNum >= m!!) {
            for (i in 1 until signNum + 1) {
                dataList?.add(MultiSigNumEntity().apply {
                    this.index = i
                    this.isSigned = true
                })
            }
        } else {
            for (i in 1 until m + 1) {
                dataList?.add(MultiSigNumEntity().apply {
                    this.index = i
                    this.isSigned = (i <= signNum)
                })
            }
        }
        binding?.rvSignResult?.models = dataList
        if (dataList?.size!! >= 5) {
            binding?.apply {
                val set = ConstraintSet()
                set.clone(clParent)
                set.clear(R.id.btn_result, ConstraintSet.TOP)
                set.connect(R.id.btn_result, ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                set.setMargin(R.id.btn_result, ConstraintSet.BOTTOM, PixelUtils.dp2px(20f))
                set.applyTo(clParent)
            }
        }
        binding?.gpSign?.setVisible(true)
    }

    private fun setSingleSingerData() {
        dataList?.clear()
        dataList?.add(MultiSigNumEntity().apply {
            this.index = 1
            this.isSigned = true
        })
        binding?.rvSignResult?.models = dataList
        binding?.gpSign?.setVisible(true)
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