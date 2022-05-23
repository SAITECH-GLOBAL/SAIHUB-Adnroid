package com.linktech.saihub.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Created by tromo on 2021/8/25.
 */
abstract class BaseLazyLoadFragment : Fragment() {

    private var rootView: View? = null
    private var isViewCreated = false
    private var isLoadDataCompleted = false
    private var isVisibleToUser = false //所有的Fragment最开始都是不可见状态
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreated && !isLoadDataCompleted) lazyLoadData()

//        DebugLog.e("XXXXX", this.toString() + "    isVisibleToUser  " + isVisibleToUser);
        if (isViewCreated && isAdded) {
            setVisibleChanged(isVisibleToUser && !realHidden())
        }
    }

    /**
     * 要同时对他的parent进行计算
     *
     * @return
     */
    fun realHidden(): Boolean {
        return when (parentFragment) {
            null -> {
                isHidden
            }
            is BaseLazyLoadFragment -> {
                (parentFragment as BaseLazyLoadFragment?)!!.realHidden() || isHidden
            }
            else -> {
                isHidden && requireParentFragment().isHidden
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isViewCreated = true
        if (rootView == null) {
            setRootView(createView(inflater, container, savedInstanceState))
            //createView(getLayoutId(),container,false)
        }
        if (rootView!!.parent != null) {
            (rootView!!.parent as ViewGroup).removeView(rootView)
        }
        return rootView
    }

    protected abstract fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (userVisibleHint && !isLoadDataCompleted) {
            lazyLoadData()
        }
    }

    private fun lazyLoadData() {
        isLoadDataCompleted = true
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setVisibleChanged(!hidden)
    }

    /**
     * [.isVisible]
     */
    override fun onResume() {
        super.onResume()
        if (isAdded) setVisibleChanged(!realHidden() && userVisibleHint)
    }

    override fun onPause() {
        super.onPause()
        if (isAdded) {
            setVisibleChanged(false)
        }
    }

    /**
     * 没有使用 [.setUserVisibleHint] 是因为它会公布到Fragment外部调用，混在一起太混乱了
     *
     * @param visible
     */
    private fun setVisibleChanged(visible: Boolean) {
        if (isVisibleToUser == visible) // 解决重复调用问题
        {
            return
        }
        isVisibleToUser = visible
        onVisibleChanged(visible)
    }

    /**
     * Fragment的可见性变化回调
     *
     * @param visible
     */
    open fun onVisibleChanged(visible: Boolean) {}

    open fun setRootView(rootView: View?) {
        this.rootView = rootView
    }

    open fun getRootView(): View? {
        return rootView
    }
}