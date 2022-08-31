package com.linktech.saihub.ui.activity.wallet.create

import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.SaihubNetUrl
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityAddLightningBinding
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.getRandomName
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus


/**
 *  创建闪电钱包
 * Created by tromo on 2022/6/21.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_ADD_ACTIVITY_PATH)
class AddLightningActivity : BaseActivity() {

    private var binding: ActivityAddLightningBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_lightning)
        binding?.apply {
            etWalletName.etSwitch?.inputType = InputType.TYPE_CLASS_TEXT
            etWalletName.etSwitch?.filters = arrayOf<InputFilter>(LengthFilter(20))
            etWalletName.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            etHost.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                }
            })

            //生成随机钱包名称
            etWalletName.setContentText("Lightning-${getRandomName()}")
            etHost.setContentText(SaihubNetUrl.DEFAULT_LIGHTNING_URL)

            tvImport.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_IMPORT_ACTIVITY_PATH)
                    .navigation()
            }


            btnCreate.onClick(Constants.CLICK_INTERVAL) {
                lightningViewModel.createAccount(etHost.getText(), etWalletName.getText())
            }
        }
    }

    override fun onData() {
        super.onData()
        lightningViewModel.mAccountData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                EventBus.getDefault().postSticky(SocketEvent())
                EventBus.getDefault()
                    .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET))
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortImageToast(getString(R.string.create_host_error))
            }
        }

    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnCreate.isEnabled = !TextUtils.isEmpty(etHost.getText())
                    && !TextUtils.isEmpty(etWalletName.getText())
        }
    }

}