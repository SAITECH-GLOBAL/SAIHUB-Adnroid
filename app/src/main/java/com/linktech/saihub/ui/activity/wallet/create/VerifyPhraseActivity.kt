package com.linktech.saihub.ui.activity.wallet.create

import android.text.TextUtils
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPhraseVerifyBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.databinding.Phrase
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 * 验证助记词
 * Created by tromo on 2022/2/23.
 */
@Route(path = ARouterUrl.WAL_WALLET_PHRASE_VERIFY_ACTIVITY_PATH)
class VerifyPhraseActivity : BaseActivity() {

    private var binding: ActivityPhraseVerifyBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val phraseData: ArrayList<Phrase>? by lazy {
        intent.getParcelableArrayListExtra(StringConstants.PHRASE_DATA)
    }

    private val walletIntentData: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    private val phraseType: Int? by lazy {
        intent.getIntExtra(StringConstants.PHRASE_TYPE, Constants.PHRASE_TYPE_CREATE_WALLET)
    }

    var verifyList: List<Phrase>? = null
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phrase_verify)
        LogUtils.e(StringConstants.SAIHUB_TAG, phraseData?.joinToString())
        //随机获取4位验证助记词
        verifyList = phraseData?.shuffled()?.take(4)?.sortedBy {
            it.index
        }
        binding?.apply {

            etPhrase0.setDesc("${verifyList?.get(0)?.index}.")
            etPhrase1.setDesc("${verifyList?.get(1)?.index}.")
            etPhrase2.setDesc("${verifyList?.get(2)?.index}.")
            etPhrase3.setDesc("${verifyList?.get(3)?.index}.")

            when (phraseType) {
                Constants.PHRASE_TYPE_CREATE_WALLET -> {
                    tvSetPhrase.setVisible(true)
                    tvPhraseDesc.setVisible(true)
                }
                Constants.PHRASE_TYPE_BACKUP_WALLET -> {
                    tvSetPhrase.setVisible(false)
                    tvPhraseDesc.setVisible(false)
                }
            }

            etPhrase0.etSwitch?.setSelectAllOnFocus(true)
            etPhrase1.etSwitch?.setSelectAllOnFocus(true)
            etPhrase2.etSwitch?.setSelectAllOnFocus(true)
            etPhrase3.etSwitch?.setSelectAllOnFocus(true)

            btnConfirm.onClick(Constants.CLICK_INTERVAL) {
                if (etPhrase0.getText() == verifyList?.get(0)?.content &&
                    etPhrase1.getText() == verifyList?.get(1)?.content &&
                    etPhrase2.getText() == verifyList?.get(2)?.content &&
                    etPhrase3.getText() == verifyList?.get(3)?.content
                ) {
                    //验证成功
                    llVerify.setVisible(false)
                    clResult.setVisible(true)
                } else {
                    if (etPhrase0.getText() != verifyList?.get(0)?.content) {
                        etPhrase0.setErrorTipLine()
                        tvErrorHint.setVisible(true)
                        tvErrorHint.text = getString(R.string.verify_phrase_tip)
                    }
                    if (etPhrase1.getText() != verifyList?.get(1)?.content) {
                        etPhrase1.setErrorTipLine()
                        tvErrorHint.setVisible(true)
                        tvErrorHint.text = getString(R.string.verify_phrase_tip)
                    }
                    if (etPhrase2.getText() != verifyList?.get(2)?.content) {
                        etPhrase2.setErrorTipLine()
                        tvErrorHint.setVisible(true)
                        tvErrorHint.text = getString(R.string.verify_phrase_tip)
                    }
                    if (etPhrase3.getText() != verifyList?.get(3)?.content) {
                        etPhrase3.setErrorTipLine()
                        tvErrorHint.setVisible(true)
                        tvErrorHint.text = getString(R.string.verify_phrase_tip)
                    }
                }
            }

            btnGoTo.onClick(Constants.CLICK_INTERVAL) {
                when (phraseType) {
                    Constants.PHRASE_TYPE_CREATE_WALLET -> {
                        walletIntentData?.let { it1 ->
                            walletViewModel.createWallet(phraseData?.map {
                                it.content
                            }, it1, this@VerifyPhraseActivity)
                        }
                    }
                    Constants.PHRASE_TYPE_BACKUP_WALLET -> {
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                            .navigation()
                    }
                }

            }

            tvSetPhrase.onClick(Constants.CLICK_INTERVAL) {
                //设置passphrase
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_SET_PASSPHRASE_ACTIVITY_PATH)
                    .withParcelableArrayList(StringConstants.PHRASE_DATA, phraseData)
                    .withParcelable(StringConstants.WALLET_DATA, walletIntentData)
                    .navigation()
            }

            //添加输入监听
            etPhrase0.etSwitch?.let { addTextWatcher(0, it) }
            etPhrase1.etSwitch?.let { addTextWatcher(1, it) }
            etPhrase2.etSwitch?.let { addTextWatcher(2, it) }
            etPhrase3.etSwitch?.let { addTextWatcher(3, it) }
        }
    }

    override fun onData() {
        super.onData()
        //钱包创建观察者
        walletViewModel.mCreateData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                if (it) {
                    EventBus.getDefault().postSticky(SocketEvent())
                    EventBus.getDefault()
                        .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET))
                    ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                        .navigation()
                }
            }
        }
    }

    private fun addTextWatcher(position: Int, editText: EditText) {
        editText.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(text: String?) {
                checkButtonState()
                binding?.tvErrorHint?.setVisible(false)
                //补全
                if (text?.length == 4 && verifyList?.get(position)?.content?.startsWith(text) == true) {
                    if (verifyList?.get(position)?.content?.length != 4)
                        editText.setText(verifyList?.get(position)?.content)
                    editText.setSelection(verifyList?.get(position)?.content?.length ?: 0)
                    changeFocus(position)
                }
            }
        })

    }

    //自动补全后焦点切换下一个输入框
    private fun changeFocus(position: Int) {
        binding?.apply {
            when (position) {
                0 -> etPhrase1.etSwitch?.requestFocus()
                1 -> etPhrase2.etSwitch?.requestFocus()
                2 -> etPhrase3.etSwitch?.requestFocus()
            }
        }

    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnConfirm.isEnabled =
                !TextUtils.isEmpty(etPhrase0.getText()) && !TextUtils.isEmpty(etPhrase1.getText())
                        && !TextUtils.isEmpty(etPhrase2.getText()) && !TextUtils.isEmpty(etPhrase3.getText())
        }
    }

}