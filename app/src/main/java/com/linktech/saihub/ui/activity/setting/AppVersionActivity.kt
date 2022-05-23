package com.linktech.saihub.ui.activity.setting

import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivitySettingAppVersionBinding
import com.linktech.saihub.ui.dialog.AppVersionDialog
import com.linktech.saihub.util.CommonUtil
import com.qmuiteam.qmui.kotlin.onClick

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_APPVERSION_ACTIVITY_PATH)
class AppVersionActivity : BaseActivity() {

    private var binding: ActivitySettingAppVersionBinding? = null

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_app_version)


        binding?.tvCurrentVersion?.text = String.format(getString(R.string.current_version_number), CommonUtil.getVersionName(mContext))
        binding?.tvUpdate?.onClick(Constants.CLICK_INTERVAL) {
            var dialog = AppVersionDialog()
            dialog.show(supportFragmentManager, "")
        }
    }

}