package com.linktech.saihub.base.dialog

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.linktech.saihub.R
import com.linktech.saihub.util.PixelUtils


/**
 * Created by tromo on 2021/8/27.
 */
open class BaseNormalFragment : DialogFragment() {

    private val BLUR_ID = 514214;
    private val DEFAULT_BLUR_RADIUS = 5f
    private val DEFAULT_BLUR_LIVE = false
    fun liveBackground(): Boolean = DEFAULT_BLUR_LIVE
    fun blurRadius(): Float = DEFAULT_BLUR_RADIUS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.custom_dialog)
    }

    override fun onStart() {
        super.onStart()
        // 设置软键盘不自动弹出
//        dialog?.window?.attributes?.windowAnimations = R.style.normal_dialog_anim
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val window = dialog?.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val params = window?.attributes
        params?.gravity = Gravity.CENTER
        params?.width = PixelUtils.getScreenWidth() - PixelUtils.dp2px((widthMargin()))
        if (onHeight() == 0F) params?.height =
            ActionBar.LayoutParams.WRAP_CONTENT else params?.height = PixelUtils.dp2px(onHeight())
        window?.attributes = params
    }

    open fun widthMargin(): Float {
        return 80F
    }

    fun hideKeyboard(v: View?) {
        val imm = v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
        }
    }

    open fun onHeight(): Float {
        return 0F;
    }

    /**
     * 检查当前页面是否处于活跃状态
     * @param manager 当前Activity对应的fragment管理者
     * @return
     */
    protected open fun checkActivityIsActive(manager: FragmentManager): Boolean {
        return !manager.isStateSaved
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (!checkActivityIsActive(manager)) {
            return
        }
        super.show(manager, tag)
    }

}