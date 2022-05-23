package com.linktech.saihub.ui.activity.wallet.manager

import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityBackupPhraseKeyBinding

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_BACKUP_PHRASE_ACTIVITY_PATH)
class BackupPrivatePhraseActivity : BaseActivity() {

    var binding: ActivityBackupPhraseKeyBinding? = null
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_backup_phrase_key)

    }

}