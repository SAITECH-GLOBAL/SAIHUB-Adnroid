package com.linktech.saihub.ui.dialog.common

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.linktech.saihub.R
import com.linktech.saihub.databinding.DialogLoadingFullScreenBinding
import com.linktech.saihub.util.PixelUtils


/**
 * Created by tromo on 2021/9/28.
 */
class LoadingFullScreenDialog() : DialogFragment() {

    private var binding: DialogLoadingFullScreenBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_loading_full_screen, container, false)
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
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        //减去状态栏高度
        val screenHeight: Int = PixelUtils.getScreenHeight()
        val statusBarHeight: Int = PixelUtils.getStatusBarHeight(context)
        val dialogHeight = screenHeight - statusBarHeight

        val params = dialog?.window?.attributes
        params?.gravity = Gravity.CENTER
        params?.width = MATCH_PARENT
        params?.height = if (dialogHeight == 0) MATCH_PARENT else dialogHeight
        params?.dimAmount = 0.0f
        dialog?.window?.attributes = params
        dialog?.setCanceledOnTouchOutside(false)

        binding?.lvLoading?.playAnimation()
    }

    fun setText(content: String) {
        binding?.tvContent?.text = content
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding?.lvLoading?.cancelAnimation()
    }
}