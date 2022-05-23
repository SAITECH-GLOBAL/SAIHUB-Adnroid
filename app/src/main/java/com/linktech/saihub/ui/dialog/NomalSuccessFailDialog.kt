package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogFailHintBinding
import com.linktech.saihub.util.glide.GlideUtils
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 失败提示弹框
 */

class NomalSuccessFailDialog(var hintMsg: Int? = null, var hintStr: String? = null, var confirmStr: String? = null) : BaseNormalFragment() {


    private var binding: DialogFailHintBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogFailHintBinding.inflate(inflater, container, false)
        return binding?.root
    }

    var confirmEvent: () -> Unit = {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hintMsg?.let {
            binding?.ivMsg?.setImageResource(hintMsg!!)
        }
        hintStr?.let {
            binding?.tvHint?.text = hintStr
        }
        confirmStr?.let {
            binding?.tvConfirm?.text = confirmStr
        }
        binding?.tvConfirm?.onClick {
            confirmEvent()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}