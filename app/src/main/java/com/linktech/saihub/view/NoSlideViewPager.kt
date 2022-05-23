package com.linktech.saihub.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class NoSlideViewPager : ViewPager {

    //是否可以进行滑动
    private val isSlide = false

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isSlide
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isSlide
    }
}