package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseNormalFragment
import com.linktech.saihub.databinding.DialogAppVersionBinding
import com.qmuiteam.qmui.kotlin.onClick

class AppVersionDialog : BaseNormalFragment() {


    private var binding: DialogAppVersionBinding? = null
    var confirmEvent: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAppVersionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnConfirm?.onClick(Constants.CLICK_INTERVAL) {
            confirmEvent()
            dismiss()
        }
        binding?.tvCancel?.onClick {
            dismiss()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}