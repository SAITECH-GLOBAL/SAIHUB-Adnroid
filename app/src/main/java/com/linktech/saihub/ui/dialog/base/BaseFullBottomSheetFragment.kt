package com.linktech.saihub.ui.dialog.base

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linktech.saihub.R

/**
 * Created by tromo on 2021/8/27.
 */
open class BaseFullBottomSheetFragment : BottomSheetDialogFragment() {

    /**
     * 顶部向下偏移量
     */
    private var topOffset = 0
    private var behavior: BottomSheetBehavior<FrameLayout>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (context == null) {
            super.onCreateDialog(savedInstanceState)
        } else BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        // 设置软键盘不自动弹出
        dialog?.window?.attributes?.windowAnimations = R.style.bottom_dialog_anim;
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog?.delegate?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val layoutParams = bottomSheet.layoutParams as ViewGroup.LayoutParams
            layoutParams.height = if (topOffset == 0) WRAP_CONTENT else topOffset
            behavior = BottomSheetBehavior.from(bottomSheet)
            // 初始为展开状态
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        onData()
    }

    open fun onData() {}

    /**
     * 获取屏幕高度
     *
     * @return height
     */
    private val height: Int
        private get() {
            var height = 1920
            if (context != null) {
                val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val point = Point()
                if (wm != null) {
                    // 使用Point已经减去了状态栏高度
                    wm.defaultDisplay.getSize(point)
                    height = point.y - topOffset
                }
            }
            return height
        }

    open fun getTopOffset(): Int {
        return topOffset
    }

    open fun setTopOffset(topOffset: Int) {
        this.topOffset = topOffset
    }

    open fun getBehavior(): BottomSheetBehavior<FrameLayout>? {
        return behavior
    }


    fun setDim() {
        val window = dialog?.window
        if (window != null) {
            var params = window.attributes
            if (params == null) {
                params = WindowManager.LayoutParams()
            }
            params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            window.setDimAmount(0.6f)
            window.attributes = params
        }
    }
}