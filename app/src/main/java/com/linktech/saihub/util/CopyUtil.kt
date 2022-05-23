package com.linktech.saihub.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.linktech.saihub.R

class CopyUtil {

    companion object {
        /**
         * 复制剪切板
         */
        fun copyCotent(context: Context, contentStr: String) {
            // 得到剪贴板管理器
            var str: ClipData = ClipData.newPlainText("Label", contentStr)
            var cm: ClipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(str)
//            ToastUtils.shortToast(context?.getString(R.string.content_copy))
            context?.resources?.getString(R.string.content_copy_success)?.let { ToastUtils.shortToast(it) }
        }

        /**
         * 提示复制成功
         */
        fun copyCotentSuccess(context: Context, contentStr: String) {
            // 得到剪贴板管理器
            var str: ClipData = ClipData.newPlainText("Label", contentStr)
            var cm: ClipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(str)
//            ToastUtils.shortToast("${context?.getString(R.string.content_copy_success)}\n${contentStr}")
            ToastUtils.shortToast("${context?.getString(R.string.content_copy_success)}")
        }
    }
}