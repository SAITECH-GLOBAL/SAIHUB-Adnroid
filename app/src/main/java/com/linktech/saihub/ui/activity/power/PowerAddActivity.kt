package com.linktech.saihub.ui.activity.power

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.king.mlkit.vision.camera.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPowerAddBinding
import com.linktech.saihub.db.bean.PowerBean
import com.linktech.saihub.db.utils.PowerDbUtil
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_POWER_ADD_ACTIVITY_PATH)
class PowerAddActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    private var binding: ActivityPowerAddBinding? = null

    var powerEditData: PowerBean? = null
    var isEdit = false

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_power_add)

        powerEditData = intent.getSerializableExtra(StringConstants.POWER_DATA) as PowerBean?

        binding?.etName?.addTextChangedListener {
            setBtEnable()
        }
        binding?.etNumber?.addTextChangedListener {
            setBtEnable()
        }

        //扫描
        betweenActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val addressStr = CameraScan.parseScanResult(result.data)
                    binding?.etNumber?.setText(addressStr)
                }
            }
        }
        binding?.ivScan?.onClick(Constants.CLICK_INTERVAL) {
            startZxingScan()
        }

        if (powerEditData != null) {
            isEdit = true
            binding?.topbar?.setTitle(getString(R.string.edit_contact))
            binding?.etName?.setText(powerEditData?.name)
            binding?.etNumber?.setText(powerEditData?.number)
        }

        binding?.tvSave?.onClick(Constants.CLICK_INTERVAL_500) {
            var powerBean = if (powerEditData == null) {
                PowerBean()
            } else {
                powerEditData?.cloneData()
            }
            powerBean?.name = binding?.etName?.text.toString()
            powerBean?.number = binding?.etNumber?.text.toString()

            lifecycleScope.launch {
                if (!isEdit) {
                    val powerNumber = withContext(Dispatchers.IO) {
                        PowerDbUtil.havePowerCount()
                    }
                    //判断是否数量上限
                    if (powerNumber >= Constants.POLL_POWER_UP_NUMBER) {
                        ToastUtils.shortImageToast(getString(R.string.list_is_full))
                        return@launch
                    }
                }

                val haveSameAddress = withContext(Dispatchers.IO) {
                    PowerDbUtil.haveSamePowerNumber(powerBean?.number ?: "", powerEditData?.number ?: "", isEdit)
                }
                if (haveSameAddress) {
                    ToastUtils.shortImageToast(getString(R.string.device_already_exists))
                    return@launch
                }

                var insertId = withContext(Dispatchers.IO) {
                    powerBean?.let { it1 -> PowerDbUtil.savePollItem(it1) }
                }
                if (insertId != null && insertId != -1L) {
                    finish()
                } else {
                    ToastUtils.shortImageToast(getString(R.string.power_save_error))
                }
            }
        }
    }

    private fun setBtEnable() {
        binding?.tvSave?.isEnabled = binding?.etName?.text?.length!! > 0 && binding?.etNumber?.text?.length!! > 0
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
}