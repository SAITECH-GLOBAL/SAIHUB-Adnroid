package com.linktech.saihub.ui.activity.wallet.create

import android.text.TextUtils
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.*
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.databinding.Phrase
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 * 设置密语
 * Created by tromo on 2022/2/23.
 */
@Route(path = ARouterUrl.WAL_WALLET_SET_PASSPHRASE_ACTIVITY_PATH)
class SetPassPhraseActivity : BaseActivity() {

    private var binding: ActivitySetPassphraseBinding? = null

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

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_passphrase)
        binding?.apply {
            etPassphrase.etSwitch?.let { addTextWatcher(it) }
            etPassphraseRepeat.etSwitch?.let { addTextWatcher(it) }
            btnGoTo.onClick {
                if (etPassphrase.getText() != etPassphraseRepeat.getText()) {
                    etPassphraseRepeat.setErrorTip(getString(R.string.passphrase_repeat_tip))
//                    ToastUtils.shortImageToast(getString(R.string.passphrase_repeat_tip))
                    return@onClick
                }
                walletIntentData?.passphrase = etPassphrase.getText()
                walletIntentData?.let { it1 ->
                    walletViewModel.createWallet(phraseData?.map {
                        it.content
                    }, it1, this@SetPassPhraseActivity)
                }
            }
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

    private fun addTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                checkButtonState()
            }

        })

    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnGoTo.isEnabled = !TextUtils.isEmpty(etPassphrase.getText())
                    && !TextUtils.isEmpty(etPassphraseRepeat.getText())
        }
    }
}