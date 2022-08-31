package com.linktech.saihub.ui.activity.login

import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.CLICK_INTERVAL
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityGuideBinding
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.ui.dialog.SelectWalletTypeDialog
import com.linktech.saihub.util.system.getAgreementUrl
import com.linktech.saihub.util.system.getBitcoinUrl
import com.qmuiteam.qmui.kotlin.onClick

/**
 * Created by tromo on 2022/2/18.
 */
@Route(path = ARouterUrl.WAL_WALLET_GUIDE_ACTIVITY_PATH)
class GuideActivity : BaseActivity() {

    private var binding: ActivityGuideBinding? = null

    override fun translucentStatusBar(): Boolean = true


    override fun onInit() {
        super.onInit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_guide)
        binding?.apply {

            btnAgree.onClick(CLICK_INTERVAL) {
                if (cbAgreement.isChecked) {
                    //创建或导入过钱包直接进入主页
                    if (CacheListManager.instance?.walletFlag == 0) {
                        val sizeList =
                            arrayListOf(0, 0)
                        val selectWalletTypeDialog = SelectWalletTypeDialog.newInstance(sizeList)
                        selectWalletTypeDialog.showNow(supportFragmentManager, "")
                    } else {
                        ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                            .navigation()
                    }
                }
            }
            tvAgreement.onClick(CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
                    .withString(StringConstants.TITLE, "")
                    .withString(
                        StringConstants.KEY_URL,
                        getAgreementUrl(RateAndLocalManager.getInstance(this@GuideActivity).curLocalLanguageKind)
                    )
                    .navigation()
            }
        }

    }

    override fun onData() {
        super.onData()
    }

}