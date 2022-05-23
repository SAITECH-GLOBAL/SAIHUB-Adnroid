package com.linktech.saihub.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by tromo on 2021/8/25.
 */
abstract class BaseFragment : BaseLazyLoadFragment() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun setRootView(view: View?) {
        super.setRootView(view)
        //RateAndLocalManager.GetInstance(getActivity()).SetCurLocalKind(RateAndLocalManager.GetInstance(getActivity()).getCurLocalKind());
        initViews()
        addEvent()
    }

    interface NotifyActivity {
        fun onNotifyActivity()
    }

    var mOnNotifyActivity: NotifyActivity? = null
    fun setOnNotifyActivity(onNotifyActivity: NotifyActivity?) {
        mOnNotifyActivity = onNotifyActivity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is NotifyActivity) {
            setOnNotifyActivity(activity as NotifyActivity)
        }
    }

    fun ActivityNotifyFragment() {
        if (mOnNotifyActivity != null) {
            mOnNotifyActivity!!.onNotifyActivity()
        }
    }

    abstract fun initViews()
    abstract fun addEvent()

    @JvmOverloads
    fun showProgressDialog(title: String? = "", hint: String? = "") {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.showLoading()
        }
    }

    fun dismissProgressDialog() {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.hideLoading()
        }
    }

}