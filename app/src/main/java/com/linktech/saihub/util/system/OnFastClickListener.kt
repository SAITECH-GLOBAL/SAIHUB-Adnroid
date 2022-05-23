package com.linktech.saihub.util.system

import android.view.View
import com.linktech.saihub.util.Pub

/**
 * Created by tromo on 2021/5/13.
 */
abstract class OnFastClickListener : View.OnClickListener {
    override fun onClick(v: View) {
        if (!Pub.isFastDoubleClick(v.id)) {
            onClickNoFast(v)
        }
    }

    abstract fun onClickNoFast(v: View?)
}