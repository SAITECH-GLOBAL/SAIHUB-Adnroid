package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogResetBinding
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Job

/**
 * 重置密码提醒弹窗
 */
class ResetDialog : BaseNormalFragment() {


    private var binding: DialogResetBinding? = null
    private var onDisMissListener: OnDisMissListener? = null
    private var onClickListener: OnClickListener? = null
    private var nameJob: Job? = null

    var confirmEvent: () -> Unit = {}


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogResetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnConfirm?.onClick(Constants.CLICK_INTERVAL) {
            confirmEvent()
            dismiss()
        }
        binding?.ivClose?.onClick {
            dismiss()
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
        nameJob?.cancel()
        onDisMissListener?.onDismiss()
    }

}