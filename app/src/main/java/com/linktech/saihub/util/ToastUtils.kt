package com.linktech.saihub.util

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.linktech.saihub.R
import com.linktech.saihub.app.SaiHubApplication

/**
 * Created by tromo on 2022/2/17.
 */
object ToastUtils {
    private var toast: Toast? = null
    private var imageToast: Toast? = null

    fun cancelToast() {
        toast?.cancel()
        imageToast?.cancel()
    }

    /**
     * 初始化Toast
     */
    private fun initToast(): Toast? {
//        if (null == toast) {
            //Toast如果没有自定义view，就要makeText
            toast = Toast.makeText(SaiHubApplication.getInstance(), "", Toast.LENGTH_SHORT)
//        }
        return toast
    }

    /**
     * 默认Toast
     */
    fun shortToast(message: Any) {
        normalToast(message, Toast.LENGTH_SHORT)
    }

    fun longToast(message: Any) {
        normalToast(message, Toast.LENGTH_LONG)
    }

    /**
     * @param message 内容，string或资源id
     * @param duration Toast的显示时长
     */
    private fun normalToast(message: Any, duration: Int) {
        initToast()
        toast?.let {
            if (message is String) {
                it.setText(message)
            } else if (message is Int) {
                it.setText(message)
            }

            it.duration = duration
            it.show()
        }
    }

    /**
     * 设置Toast位置
     */
    fun setToastGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        toast?.let {
            it.setGravity(gravity, xOffset, yOffset)
        }
    }


    /**
     * 图片Toast初始化
     */
    private fun initImageToast(): Toast? {
        if (null == imageToast) {
            imageToast = Toast(SaiHubApplication.getInstance())
            imageToast?.setGravity(Gravity.CENTER, 0, 0)
        }
        return imageToast
    }

    /**
     * 默认图片Toast
     */
    fun shortRightImageToast(message: Any) {
        imageToast(message, R.mipmap.icon_toast_right, Toast.LENGTH_SHORT)
    }

    /**
     * 默认图片Toast  默认失败图片
     */
    fun shortImageToast(message: Any) {
        imageToast(message, null, Toast.LENGTH_SHORT)
    }

    /**
     * 自定义图片Toast
     */
    fun shortImageToast(message: Any, img: Int?) {
        imageToast(message, img, Toast.LENGTH_SHORT)
    }

    /**
     * 长时间图片Toast
     */
    fun longImageToast(message: Any) {
        imageToast(message, null, Toast.LENGTH_LONG)
    }

    /**
     * 长时间自定义图片Toast
     */
    fun longImageToast(message: Any, img: Int?) {
        imageToast(message, img, Toast.LENGTH_LONG)
    }

    /**
     * 设置图片Toast位置
     */
    fun setImageToastGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        imageToast?.let {
            it.setGravity(gravity, xOffset, yOffset)
        }
    }

    /**
     * @param message 内容，string或资源id
     * @param img 图片资源id
     * @param duration Toast的显示时长
     */
    private fun imageToast(message: Any, img: Int?, duration: Int) {
        initImageToast()
        imageToast?.let {
            //可以是其他自定义布局
            val rootView = LayoutInflater.from(SaiHubApplication.getInstance()).inflate(R.layout.toast_view, null)

            //设置消息
            val txtContent = rootView.findViewById<TextView>(R.id.tv_content)
            if (message is String) {
                txtContent.text = message
            } else if (message is Int) {
                txtContent.setText(message)
            }

            //设置图片
            val imgContent = rootView.findViewById<ImageView>(R.id.iv_content)
            imgContent.setImageResource(R.mipmap.icon_toast_x)
            img?.let {
                imgContent.setImageResource(it)
            }

            it.view = rootView
            it.duration = duration
            it.show()
        }
    }
}