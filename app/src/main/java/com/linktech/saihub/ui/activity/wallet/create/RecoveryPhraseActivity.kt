package com.linktech.saihub.ui.activity.wallet.create

import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPhraseRecoverBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.databinding.Phrase
import com.linktech.saihub.ui.adapter.vp2.RvAdapter
import com.linktech.saihub.util.system.averageAssign
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 备份助记词
 * Created by tromo on 2022/2/23.
 */
@Route(path = ARouterUrl.WAL_WALLET_PHRASE_RECOVER_ACTIVITY_PATH)
class RecoveryPhraseActivity : BaseActivity() {

    private var binding: ActivityPhraseRecoverBinding? = null

    private var phraseList: List<List<Phrase>> = mutableListOf()

    private var dataList: List<Phrase> = mutableListOf()

    private val phraseData by lazy {
        intent.getStringArrayListExtra(StringConstants.PHRASE_DATA)
    }

    private val walletIntentData: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val phraseType: Int by lazy {
        intent.getIntExtra(StringConstants.PHRASE_TYPE, Constants.PHRASE_TYPE_CREATE_WALLET)
    }


    override fun translucentStatusBar(): Boolean = true

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phrase_recover)
        binding?.apply {
            dataList = phraseData?.mapIndexed { index, s ->
                Phrase(s, index + 1)
            }!!
            phraseList = averageAssign(dataList, if (dataList.size == 12) 3 else 4)

            vpPhrase.adapter = RvAdapter(phraseList)
            vpPhrase.isUserInputEnabled = false

            val dp8 = resources.getDimensionPixelOffset(R.dimen.dp_8)

            indicatorView.apply {
                setIndicatorGap(dp8)
                setIndicatorDrawable(R.drawable.shape_indicator_nor, R.drawable.shape_indicator_sel)
                setIndicatorSize(
                    dp8,
                    dp8,
                    resources.getDimensionPixelOffset(R.dimen.dp_16),
                    dp8
                )
                setupWithViewPager(vpPhrase)
            }

            btnContinue.onClick {
                if (vpPhrase.currentItem + 1 != vpPhrase.adapter?.itemCount) {
                    vpPhrase.currentItem = vpPhrase.currentItem + 1
                    if (vpPhrase.currentItem + 1 == vpPhrase.adapter?.itemCount) {
                        btnContinue.text = getString(R.string.complete_tip)
                    }
                } else {
                    ARouter.getInstance().build(ARouterUrl.WAL_WALLET_PHRASE_VERIFY_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, walletIntentData)
                        .withParcelableArrayList(StringConstants.PHRASE_DATA, ArrayList(dataList))
                        .withInt(StringConstants.PHRASE_TYPE, phraseType)
                        .navigation()
                }
            }
        }
    }

    override fun onData() {
        super.onData()
    }
}