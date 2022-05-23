package com.linktech.saihub.ui.activity.wallet.imp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
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
import com.linktech.saihub.databinding.ActivityImportWalletBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.entity.wallet.ExtPubKeyEntity
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.net.ex.ApiException
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity.Companion.TYPE_IMPORT
import com.linktech.saihub.ui.dialog.SelectAddressTypeDialog
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.*
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import com.sparrowwallet.hummingbird.registry.RegistryType
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 *  导入钱包
 * Created by tromo on 2022/2/23.
 */
@Route(path = ARouterUrl.WAL_WALLET_IMPORT_ACTIVITY_PATH)
class ImportWalletActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    private var binding: ActivityImportWalletBinding? = null

    private var webView: WebView? = null

    override fun translucentStatusBar(): Boolean = true

    private var selectAddressTypeDialog: SelectAddressTypeDialog? = null

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_import_wallet)
        webView = WebView(this)
        val webSettings = webView?.settings
        //允许使用JS
        webSettings?.javaScriptEnabled = true
        webView?.loadUrl("file:///android_asset/web/index.html")
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        binding?.apply {
            //生成随机钱包名称
            tvWalletName.text = "BTC-${getRandomName()}"

            tbImport.setRightOnClickListener(object : OnFastClickListener() {
                override fun onClickNoFast(v: View?) {
                    startZxingScan()
                }

            })

            btnImport.onClick(Constants.CLICK_INTERVAL) {
                walletViewModel.importIntentWallet(etInput.text.toString())
            }

            etInput.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    btnImport.isEnabled = !TextUtils.isEmpty(s)
                }

            })

            tvColdWallet.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
                    .withString(StringConstants.TITLE, "")
                    .withString(
                        StringConstants.KEY_URL,
                        getColdWalletUrl(RateAndLocalManager.getInstance(this@ImportWalletActivity).curLocalLanguageKind)
                    )
                    .navigation()
            }
        }
    }

    override fun onData() {
        super.onData()

        walletViewModel.mValidateData.vmObserver(this) {

            onAppSuccess = {
                val content = binding?.etInput?.text.toString()
                val walletName = binding?.tvWalletName?.text.toString()
                //如果是私钥或助记词类型，跳转设置密码
                when (it) {
                    Constants.VALIDATE_PRIVATE_KEY, Constants.VALIDATE_MNEMONIC -> {
                        selectAddressTypeDialog = SelectAddressTypeDialog()
                        selectAddressTypeDialog?.showNow(supportFragmentManager, "")
                        selectAddressTypeDialog?.setOnClickListener(object :
                            SelectAddressTypeDialog.OnClickListener {
                            override fun onConfirmClick(mAddressType: Int) {
                                ARouter.getInstance()
                                    .build(ARouterUrl.WAL_WALLET_IMPORT_SET_PWD_ACTIVITY_PATH)
                                    .withInt(StringConstants.IMPORT_TYPE, it)
                                    .withString(StringConstants.IMPORT_DATA, content)
                                    .withParcelable(
                                        StringConstants.WALLET_DATA,
                                        WalletBean().apply {
                                            this.name = walletName
                                            this.addressType = mAddressType
                                        })
                                    .navigation()
                            }

                        })
                    }
                    //公钥或地址直接导入  导入公钥不需要选择钱包地址类型
                    Constants.VALIDATE_PUBLIC_KEY -> {
                        //拓展公钥
                        val pubKey = if (content.contains("ExtPubKey")) {
                            val extPubKeyEntity =
                                Gson().fromJson(content, ExtPubKeyEntity::class.java)
                            extPubKeyEntity.extPubKey
                        } else {
                            content
                        }
                        pubKey?.let { pub ->
                            walletViewModel.importWallet(
                                it,
                                WalletBean().apply {
                                    this.name = walletName
                                    this.addressType = getTypeForPub(pub)
                                    this.publicKeyExt = content
                                }, pub, this@ImportWalletActivity
                            )
                        }
                    }
                    Constants.VALIDATE_ADDRESS -> {
                        walletViewModel.importWallet(
                            it, WalletBean().apply {
                                this.name = walletName
                                this.addressType = getTypeForAddress(content)
                            }, content, this@ImportWalletActivity
                        )
                    }
                    Constants.VALIDATE_MULTI_SIG -> {
                        walletViewModel.getMultiSigAddress(content)
                    }
                }

            }
            onAppError = {
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        walletViewModel.mMultiSigData.vmObserver(this) {
            onAppLoading = {
                showLoadingFullScreen()
                loadingFullScreenDialog?.setText(getString(R.string.import_loading))
            }
            onAppSuccess = {
                val content = binding?.etInput?.text.toString()
                val walletName = binding?.tvWalletName?.text.toString()
                webView?.evaluateJavascript(it.second) { value ->
                    LogUtils.e(value)
                    walletViewModel.importMultiSigWallet(WalletBean().apply {
                        this.name = walletName
                    }, content, value, this@ImportWalletActivity, it.first)

                }
            }
            onAppError = {
                ToastUtils.shortToast(ApiException(it.errorMsg))
                hideLoadingFullScreen()
            }
        }

        //导入结果观察者
        walletViewModel.mImportData.vmObserver(this) {
            onAppLoading = {
                showLoadingFullScreen()
                loadingFullScreenDialog?.setText(getString(R.string.import_loading))
            }
            onAppSuccess = {
                hideLoadingFullScreen()
                if (it) {
                    EventBus.getDefault().postSticky(SocketEvent())
                    EventBus.getDefault()
                        .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET))
                    ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                        .navigation()
                }

            }
            onAppError = {
                hideLoadingFullScreen()
                ToastUtils.shortImageToast(it.errorMsg)
            }
        }

        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val resultType = result.data?.getStringExtra(StringConstants.RESULT_TYPE)
                        val addressStr = CameraScan.parseScanResult(result.data)
                        if (resultType == RegistryType.CRYPTO_ACCOUNT.type) {
                            addressStr?.let { walletViewModel.convertUrAccountToAccount(it) }
                        } else {
                            binding?.etInput?.setText(addressStr)
                        }

                    }
                }
            }

        //单签 公钥导入解析内容观察者
        walletViewModel.mSingleSigJsData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                webView?.evaluateJavascript(it) { value ->
                    hideLoading()
                    LogUtils.e(value)
                    //单签导入内容
                    binding?.etInput?.setText(formatJsReturn(value))
                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(it.errorMsg)
            }
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
        startScanActivity()
    }

    private fun startScanActivity() {
        val intent = Intent(this, WeChatQRCodeActivity::class.java)
        intent.putExtra(StringConstants.LOAD_TYPE, TYPE_IMPORT)
        betweenActivityResultLauncher?.launch(intent)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        val permsRequest =
            mutableListOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
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
        if (EasyPermissions.hasPermissions(this,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
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
}