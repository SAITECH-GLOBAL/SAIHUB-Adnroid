package com.linktech.saihub.ui.activity.wallet.manager

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.wallet.MnemonicUtil
import com.qmuiteam.qmui.kotlin.onClick
import java.util.ArrayList

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_BACKUP_PHRASE_HINT_ACTIVITY_PATH)
class BackUpPhraseHintActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back_up_phrase_hint)

        var walletBean = intent?.getParcelableExtra<WalletBean>(StringConstants.WALLET_DATA)

        findViewById<View>(R.id.tv_continue).onClick(Constants.CLICK_INTERVAL) {
            ARouter.getInstance().build(ARouterUrl.WAL_WALLET_PHRASE_RECOVER_ACTIVITY_PATH)
                .withParcelable(StringConstants.WALLET_DATA, walletBean)
                .withInt(StringConstants.PHRASE_TYPE, Constants.PHRASE_TYPE_BACKUP_WALLET)
                .withStringArrayList(StringConstants.PHRASE_DATA, ArrayList(MnemonicUtil.createMnemonic(AES.decrypt(walletBean?.mnemonic))))
                .navigation()
            finish()
        }
    }
}