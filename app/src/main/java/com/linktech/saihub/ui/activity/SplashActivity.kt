package com.linktech.saihub.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySplahBinding
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.ui.activity.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : BaseActivity() {

    private var binding: ActivitySplahBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isTaskRoot) {
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                finish()
                return
            }
        }


    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splah)
        lifecycleScope.launch(Dispatchers.IO) {

            //检查语言
            val currentLanguage =
                MMKVManager.getInstance().mmkv().decodeString(Constants.CURRENT_LANGUAGE)

            if (!TextUtils.isEmpty(currentLanguage)) {
                val localKindByLangCode =
                    RateAndLocalManager.getLocalKindByLangCode(currentLanguage)
                RateAndLocalManager.getInstance(mContext)
                    .setCurLocalKindExt(mContext, localKindByLangCode)
            } else {
                MMKVManager.getInstance().mmkv()
                    .encode(Constants.CURRENT_LANGUAGE, RateAndLocalManager.LocalKind.en_US.code)
                RateAndLocalManager.getInstance(mContext)
                    .setCurLocalKindExt(mContext, RateAndLocalManager.LocalKind.en_US)
            }

//            LocalManageUtil.setApplicationLanguage(mContext)

            //检查标志位
            val walletFlag = CacheListManager.instance?.walletFlag
            if (walletFlag == -1) {
                CacheListManager.instance?.saveWalletFlag(0)
            }

            val walletList = withContext(Dispatchers.IO) {
                WalletDaoUtils.loadAll()
            }
            val isOpenPwdLogin =
                MMKVManager.getInstance().mmkv().getBoolean(Constants.VERIFY_PWD_LOGIN, false)
            val isOpenTouchIdLogin = MMKVManager.getInstance().mmkv()
                .getBoolean(Constants.VERIFY_TOUCH_ID_LOGIN, false)

            //没有创建或导入过进入用户协议界面
            if ((walletList == null || walletList.isEmpty()) && !isOpenPwdLogin && CacheListManager.instance?.walletFlag == 0) {

                //用户协议界面
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_GUIDE_ACTIVITY_PATH)
                    .navigation(this@SplashActivity, object : NavCallback() {
                        override fun onArrival(postcard: Postcard?) {
                            this@SplashActivity.finish()
                        }

                    })
//                overridePendingTransition(0, 0)

            } else {

                if (isOpenPwdLogin) {
                    ARouter.getInstance().build(ARouterUrl.WAL_WALLET_LOGIN_ACTIVITY_PATH)
                        .withInt(StringConstants.LOGIN_TYPE,
                            if (isOpenTouchIdLogin) LoginActivity.LOGIN_TYPE_TOUCHID else LoginActivity.LOGIN_TYPE_PWD)
                        .navigation(this@SplashActivity, object : NavCallback() {
                            override fun onArrival(postcard: Postcard?) {
                                this@SplashActivity.finish()
                            }

                        })
//                    overridePendingTransition(0, 0)
                    return@launch
                }

                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
//                overridePendingTransition(0, 0)
//                finish()
            }

        }
    }
}