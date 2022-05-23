package com.linktech.saihub.ui.activity.wallet.create

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPhraseTypeBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.qmuiteam.qmui.kotlin.onClick

/**
 *选择助记词
 * Created by tromo on 2022/2/18.
 */
@Route(path = ARouterUrl.WAL_WALLET_PHRASE_TYPE_ACTIVITY_PATH)
class SelectPhraseTypeActivity : BaseActivity() {

    private var binding: ActivityPhraseTypeBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    private val walletIntentData: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    //地址类型
    private var phraseType = -1  //0::12  1::24

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phrase_type)
        binding?.apply {
            btnContinue.onClick(Constants.CLICK_INTERVAL) {
                walletViewModel.createPhrase(phraseType)

//                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_PHRASE_RECOVER_ACTIVITY_PATH)
//                    .navigation()
            }
            rb12.onClick(Constants.CLICK_INTERVAL) {
                phraseType = 0
                btnContinue.isEnabled = true
            }
            rb24.onClick(Constants.CLICK_INTERVAL) {
                phraseType = 1
                btnContinue.isEnabled = true
            }
        }
    }

    override fun onData() {
        super.onData()
        walletViewModel.mPhraseData.vmObserver(this) {
            onAppSuccess = {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_PHRASE_RECOVER_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletIntentData)
                    .withStringArrayList(StringConstants.PHRASE_DATA, ArrayList(it))
                    .navigation()
            }
        }
    }
}