package com.linktech.saihub.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linktech.saihub.util.system.ActivitiesStack

open class BaseWindowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 默认为竖屏
        ActivitiesStack.getInstance().push(this)
    }

    override fun onDestroy() {
        ActivitiesStack.getInstance().pop(this)
        super.onDestroy()
    }
}