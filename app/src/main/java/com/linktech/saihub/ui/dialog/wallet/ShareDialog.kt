package com.linktech.saihub.ui.dialog.wallet

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.linktech.saihub.R
import com.linktech.saihub.app.Constants.WEBSITE_URL
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogShareBinding
import com.linktech.saihub.qrcode.DuQRCodeEncoder
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.SystemShareUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.screenshot.BitmapUtils
import com.linktech.saihub.util.screenshot.ShareHelperUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * 分享弹窗
 */
class ShareDialog : BaseNormalFragment(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val RC_WRITE_EXTERNAL = 10002
    }

    private var binding: DialogShareBinding? = null
    private var onDisMissListener: OnDisMissListener? = null
    private var onClickListener: OnClickListener? = null
    private var nameJob: Job? = null
    private var createShareQRCode: Bitmap? = null
    private var addressQrcode: Bitmap? = null

    var confirmEvent: () -> Unit = {}

    private var address: String? = null
    private var coin: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogShareBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            address = it.getString(StringConstants.ADDRESS)
            coin = it.getString(StringConstants.COIN)
        }
        binding?.tvCoin?.text = coin
        binding?.tvNet?.text = WEBSITE_URL
        address?.let { loadQrcode(it, binding?.ivAddressQrcode) }
        loadQrcode(WEBSITE_URL, binding?.ivQrcode)
        binding?.tvAddress?.text = address
        binding?.llPoster?.postDelayed({
            createShareQRCode = ShareHelperUtil.createViewBitmap(binding?.llPoster)
            if (createShareQRCode != null) {
                systemShare(createShareQRCode)
            }

        }, 500)
    }

    private fun loadQrcode(address: String, imageView: ImageView?) {
        lifecycleScope.launch {
            addressQrcode = withContext(Dispatchers.IO) {
                DuQRCodeEncoder.syncEncodeQRCode(
                    address,
                    PixelUtils.dp2px(191F),
                    context?.resources?.getColor(R.color.black)!!,
                    context?.resources?.getColor(R.color.white)!!,
                    null
                )
                //CodeUtils.createQRCode(addressKey, PixelUtils.dp2px(193F))
            }
            imageView?.setImageBitmap(addressQrcode)
            imageView?.setPadding(0, 0, 0, 0)
        }
    }

    /**
     * 自己：截屏分享
     */
    private fun systemShare(resultBitmap: Bitmap?) {
        if (resultBitmap == null || activity == null) {
            return
        }
        if (!EasyPermissions.hasPermissions(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            EasyPermissions.requestPermissions(this@ShareDialog,
                getString(R.string.file_read_write_permission_hint),
                RC_WRITE_EXTERNAL,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            return
        }
        lifecycleScope.launch {
            var resultPath: String?
            withContext(Dispatchers.IO) {
                //弹出分享对话框
                resultPath = BitmapUtils.saveBitmap(activity, resultBitmap)
            }
            if (resultPath == null) {
                ToastUtils.shortToast(getString(R.string.unable_to_find_file_path))
                return@launch
            }
            SystemShareUtils.shareImage(activity, resultPath)
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == RC_WRITE_EXTERNAL) {
            // 权限请求成功
            systemShare(createShareQRCode)
        } else {
            Toast.makeText(activity,
                getString(R.string.request_permission_failed),
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setTitle(getString(R.string.swap_title_settings_dialog))
                .setRationale(getString(R.string.swap_rationale_ask_again))
                .setNegativeButton(getString(R.string.cancel))
                .setPositiveButton(getString(R.string.confirm))
                .build().show()
        }
    }

    interface OnDisMissListener {
        fun onDismiss()
    }

    interface OnClickListener {
        fun onConfirmClick(isCheck: Boolean)
    }

    fun setOnDisMissListener(onDisMissListener: OnDisMissListener) {
        this.onDisMissListener = onDisMissListener
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onDestroy() {
        super.onDestroy()
        if (addressQrcode?.isRecycled == false) {
            addressQrcode?.recycle()
        }
        if (createShareQRCode?.isRecycled == false) {
            createShareQRCode?.recycle()
        }
        nameJob?.cancel()
        onDisMissListener?.onDismiss()
    }

}