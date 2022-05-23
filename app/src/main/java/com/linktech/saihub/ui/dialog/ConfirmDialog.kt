package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogConfirmBinding
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 删除确认弹框
 */

class ConfirmDialog(
    var titleStr: String? = null,
    var contentStr: String? = null,
    var cancelStr: String? = null,
    var confirmStr: String? = null,
    var isShowBack: Boolean = false
) : BaseNormalFragment() {


    private var binding: DialogConfirmBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogConfirmBinding.inflate(inflater, container, false)
        return binding?.root
    }

    var cancelEvent: () -> Unit = {}
    var confirmEvent: () -> Unit = {}
    var backupEvent: () -> Unit = {}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleStr?.let {
            binding?.tvTitle?.text = titleStr
        }
        contentStr?.let {
            binding?.tvContent?.text = contentStr
        }
        cancelStr?.let {
            binding?.tvCancel?.text = cancelStr
        }
        confirmStr?.let {
            binding?.tvConfirm?.text = confirmStr
        }

        binding?.tvBackup?.setVisible(isShowBack)

        binding?.tvCancel?.onClick {
            cancelEvent()
            dismiss()
        }
        binding?.tvConfirm?.onClick(Constants.CLICK_INTERVAL) {
            confirmEvent()
            dismiss()
        }
        binding?.tvBackup?.onClick(Constants.CLICK_INTERVAL) {
            backupEvent()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}