package com.linktech.saihub.ui.activity.wallet.imp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityImportLightningBinding
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity.Companion.TYPE_IMPORT
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.getRandomName
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 *  导入闪电钱包
 * Created by tromo on 2022/6/21.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_IMPORT_ACTIVITY_PATH)
class ImportLightningActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    private var binding: ActivityImportLightningBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_import_lightning)
        binding?.apply {
            etWalletName.etSwitch?.inputType = InputType.TYPE_CLASS_TEXT
            etWalletName.etSwitch?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
            etWalletName.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            etLndhub.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            //生成随机钱包名称
            etWalletName.setContentText("Lightning-${getRandomName()}")

            ivScan.onClick(Constants.CLICK_INTERVAL) {
                startZxingScan()
            }

            btnImport.onClick(Constants.CLICK_INTERVAL) {
                lightningViewModel.importLnHub(etLndhub.getText(), etWalletName.getText())
            }

        }
    }

    override fun onData() {
        super.onData()

        //导入结果观察者
        lightningViewModel.mAccountData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                ToastUtils.shortToast(getString(R.string.import_success))
                EventBus.getDefault().postSticky(SocketEvent())
                EventBus.getDefault()
                    .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET))
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val resultType = result.data?.getStringExtra(StringConstants.RESULT_TYPE)
                        val addressStr = CameraScan.parseScanResult(result.data)
                        binding?.etLndhub?.setContentText(addressStr)
                    }
                }
            }
    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnImport.isEnabled = !TextUtils.isEmpty(etLndhub.getText())
                    && !TextUtils.isEmpty(etWalletName.getText())
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
}