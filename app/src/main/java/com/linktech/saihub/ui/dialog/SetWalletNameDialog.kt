package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogConfirmBinding
import com.linktech.saihub.databinding.DialogSetNameBinding
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 更改钱包名弹框
 */

class SetWalletNameDialog(
    var walletName: String? = null
) : BaseNormalFragment() {

    private var binding: DialogSetNameBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSetNameBinding.inflate(inflater, container, false)
        return binding?.root
    }

    var cancelEvent: () -> Unit = {}
    var confirmEvent: (walletName: String?) -> Unit = {}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etWalletName?.etSwitch?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
        walletName?.let {
            binding?.etWalletName?.setHintText(it)
        }

        binding?.tvCancel?.onClick {
            cancelEvent()
            dismiss()
        }
        binding?.tvConfirm?.onClick(Constants.CLICK_INTERVAL) {
            confirmEvent(binding?.etWalletName?.getText())
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}