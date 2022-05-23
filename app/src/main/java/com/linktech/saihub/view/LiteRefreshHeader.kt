package com.linktech.saihub.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.linktech.saihub.R
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

/**
 * Created by tromo on 2021/9/3.
 */
class LiteRefreshHeader(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet), RefreshHeader {

    var inflate: View? = null
        private set

    //    private var avi: AVLoadingIndicatorView? = null
    private var lvRefresh: LottieAnimationView? = null

    init {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_refresh_header, this)
        lvRefresh = inflate?.findViewById(R.id.lv_refresh)
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {}


    override fun getView(): View {
        return inflate!!
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {}

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {}

    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {}

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {}

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
//        avi?.smoothToShow()
        lvRefresh?.playAnimation()
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
//        avi?.smoothToHide()
        lvRefresh?.cancelAnimation()
        return 0
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {}


    override fun isSupportHorizontalDrag(): Boolean = false
}