package com.linktech.saihub.manager

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.net.interceptor.BasicParamsInterceptor
import java.util.*


class AppConfigManager {


    companion object {
        @Volatile
        private var instance: AppConfigManager? = null

        fun getInstance(): AppConfigManager? {
            if (instance == null) {
                synchronized(AppConfigManager::class.java) {
                    if (instance == null) {
                        instance = AppConfigManager()
                    }
                }
            }
            return instance
        }

        private const val apiKey = "921409feaeb811eaa61400163e06ad39"
        private const val appName = "app_api"
        private const val TOKEN_ERROR = 0x13
        private const val ACCESS_TOKEN_USER_REMOTE_LOGIN = 0x14

    }
    /*  app_name= app_api&
       api_key=921409feaeb811eaa61400163e06ad39&
       app_ver=2.1.1&
       link_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MzA5ODYxMjAsInVzZXJuYW1lIjoiMDAwMDAxMjAtMiJ9.FcnpW8OjYRR5OXJkM2ysWmaNN9OB7oUE7nDkOAHhVDc&
       udid=b60a71b2-95b7-4b0b-859e-0e71bce51326&
       lang=en&
       ts=1631689039512*/

    var headIntercept: BasicParamsInterceptor? = null
    fun init(linkChainApplication: SaiHubApplication) {
        headIntercept = createHeadInterceptor(linkChainApplication)
    }

    private fun createHeadInterceptor(context: Context): BasicParamsInterceptor {
        val headBuilder = BasicParamsInterceptor.Builder()
        //添加GET公共参数
        val pm: PackageManager = context.packageManager
        val pageInfo = pm.getPackageInfo(context.packageName, 0)

        //添加GET公共参数
        headBuilder.addQueryParam("app_name", "app_api")
        headBuilder.addQueryParam("api_key", apiKey)
        headBuilder.addQueryParam("app_ver", pageInfo.versionName)
        var uuidStr = MMKVManager.getInstance().mmkv().decodeString(Constants.UUID, "")
        if (TextUtils.isEmpty(uuidStr)) {
            uuidStr = UUID.randomUUID().toString()
            MMKVManager.getInstance().mmkv().encode(Constants.UUID, uuidStr)
        }
        headBuilder.addQueryParam("udid", uuidStr)
        headBuilder.addQueryParam(
            "lang",
            RateAndLocalManager.getInstance(context)?.curLocalLanguageKind?.code
        )
        headBuilder.addQueryParam("ts", System.currentTimeMillis().toString() + "")
        return headBuilder.build()
    }

    fun getHeadIntercepet(context: Context): BasicParamsInterceptor {
        return headIntercept ?: createHeadInterceptor(context)
    }
}