package com.linktech.saihub.ui.dialog.common

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.linktech.saihub.R
import com.linktech.saihub.databinding.DialogLoadingNormalBinding

/**
 * Created by tromo on 2021/9/28.
 */
class LoadingDialog() : DialogFragment() {

    private var binding: DialogLoadingNormalBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_loading_normal, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.dialogLoading);
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        dialog?.setCanceledOnTouchOutside(false)
        binding?.lvLoading?.playAnimation()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding?.lvLoading?.cancelAnimation()
    }
}