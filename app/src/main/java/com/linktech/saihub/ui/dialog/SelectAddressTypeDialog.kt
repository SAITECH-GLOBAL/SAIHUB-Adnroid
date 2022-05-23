package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogResetBinding
import com.linktech.saihub.databinding.DialogSelectAddressTypeBinding
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Job

/**
 * 导入-选择地址类型弹窗
 */
class SelectAddressTypeDialog : BaseNormalFragment() {


    private var binding: DialogSelectAddressTypeBinding? = null
    private var onDisMissListener: OnDisMissListener? = null
    private var onClickListener: OnClickListener? = null
    private var nameJob: Job? = null

    var confirmEvent: () -> Unit = {}

    //地址类型
    private var mAddressType = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSelectAddressTypeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {

            btnImport.onClick(Constants.CLICK_INTERVAL) {
                onClickListener?.onConfirmClick(mAddressType)
                dismiss()
            }

            ivClose.onClick {
                dismiss()
            }

            rbNative.onClick(Constants.CLICK_INTERVAL) {
                mAddressType = Constants.CHILD_ADDRESS_NATIVE
                checkButtonState()
            }

            rbNested.onClick(Constants.CLICK_INTERVAL) {
                mAddressType = Constants.CHILD_ADDRESS_NESTED
                checkButtonState()
            }
        }

    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnImport.isEnabled = mAddressType != -1
        }
    }

    interface OnDisMissListener {
        fun onDismiss()
    }

    interface OnClickListener {
        fun onConfirmClick(mAddressType: Int)
    }

    fun setOnDisMissListener(onDisMissListener: OnDisMissListener) {
        this.onDisMissListener = onDisMissListener
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onDestroy() {
        super.onDestroy()
        nameJob?.cancel()
        onDisMissListener?.onDismiss()
    }

}