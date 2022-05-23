package com.linktech.saihub.ui.activity.setting

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
import com.king.zxing.CameraScan
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityAddressAddBinding
import com.linktech.saihub.db.bean.WalletAddressBean
import com.linktech.saihub.db.utils.AddressDaoUtil
import com.linktech.saihub.ui.activity.WeChatQRCodeActivity
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.walutils.AddressCheckUtils
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ADD_ACTIVITY_PATH)
class AddressAddActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    private var binding: ActivityAddressAddBinding? = null

    var addressBookEditData: WalletAddressBean? = null
    var isEdit = false

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_address_add)

        addressBookEditData =
            intent.getSerializableExtra(StringConstants.ADDRESS_BOOK_DATA) as WalletAddressBean?


        binding?.etAddressName?.addTextChangedListener {
            setBtEnable()
        }
        binding?.etAddress?.addTextChangedListener {
            setBtEnable()
        }

        if (addressBookEditData != null) {
            isEdit = true
            binding?.topbar?.setTitle(getString(R.string.edit_address))
            binding?.etAddressName?.setText(addressBookEditData?.addressName)
            binding?.etAddress?.setText(addressBookEditData?.address)
        }

        binding?.tvSave?.onClick(Constants.CLICK_INTERVAL_500) {

            val addressBookBean = if (addressBookEditData == null) {
                WalletAddressBean()
            } else {
                addressBookEditData?.cloneData()
            }
            addressBookBean?.addressName = binding?.etAddressName?.text.toString()
            addressBookBean?.address = binding?.etAddress?.text.toString()

            //检查地址格式是否正确
            if (!AddressCheckUtils.isBTCValidAddress(addressBookBean?.address)) {
                ToastUtils.shortImageToast(getString(R.string.addres_format_error))
                return@onClick
            }


            lifecycleScope.launch {

                if (!isEdit) {
                    val addressNumber = withContext(Dispatchers.IO) {
                        AddressDaoUtil.haveAddressNumber()
                    }
                    if (addressNumber >= Constants.ADDRESS_UP_NUMBER) {
                        ToastUtils.shortImageToast(getString(R.string.up_to_20_addresses))
                        return@launch
                    }
                }

                val haveSameAddress = withContext(Dispatchers.IO) {
                    AddressDaoUtil.haveSameAddressBook(addressBookBean?.address,
                        addressBookEditData?.address,
                        isEdit)
                }
                if (haveSameAddress) {
                    val dialog = NomalSuccessFailDialog(null,
                        getString(R.string.address_already_exists),
                        null)
                    dialog.show(supportFragmentManager, "")
//                    ToastUtils.shortImageToast(getString(R.string.address_already_exists))
                    return@launch
                }
                val insertId = withContext(Dispatchers.IO) {
                    addressBookBean?.let { it1 -> AddressDaoUtil.saveAddressItem(it1) }
                }
                if (insertId != null && insertId != -1L) {
                    finish()
                } else {
                    ToastUtils.shortImageToast(getString(R.string.address_book_save_error))
                }
            }
        }

        //扫描
        betweenActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val addressStr = CameraScan.parseScanResult(result.data)
                        binding?.etAddress?.setText(addressStr)
                    }
                }
            }
        binding?.ivAddressScan?.onClick(Constants.CLICK_INTERVAL)
        {
            startZxingScan()
        }
    }

    private fun setBtEnable() {
        binding?.tvSave?.isEnabled =
            binding?.etAddressName?.text?.length!! > 0 && binding?.etAddress?.text?.length!! > 0
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

}