package com.linktech.saihub.ui.activity.wallet.create

import android.graphics.Typeface
import android.text.InputType
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityCreateInvoiceBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.entity.lightning.DecodeInvoiceEntity
import com.linktech.saihub.entity.lightning.PendingInvoiceEntity
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.appendAuth
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

/**
 *  创建invoice
 * Created by tromo on 2022/6/22.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_INVOICE_CREATE_ACTIVITY_PATH)
class CreateInvoiceActivity : BaseActivity() {

    private var binding: ActivityCreateInvoiceBinding? = null
    private var payReq: String? = null

    override fun translucentStatusBar(): Boolean = true
    private var wvJs: WebView? = null

    private val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_invoice)
        wvJs = WebView(this)
        val webSettings = wvJs?.settings
        //允许使用JS
        webSettings?.javaScriptEnabled = true
        wvJs?.loadUrl("file:///android_asset/web/index.html")
        wvJs?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        binding?.apply {
            //输入框初始化
            etAmount.etSwitch?.inputType = InputType.TYPE_CLASS_NUMBER
            etAmount.etSwitch?.typeface =
                Typeface.createFromAsset(assets, "fonts/montserrat_regular.ttf")
            etMemo.etSwitch?.inputType = InputType.TYPE_CLASS_TEXT
            etAmount.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    checkButtonState()
                    changeUnitShow()
                    etAmount.etSwitch?.setSelection(s?.length!!)
                }
            })

            //切换折算单位
            btnUnit.onClick() {
                changeShow()
            }

            btnCreate.onClick(Constants.CLICK_INTERVAL) {
                lightningViewModel.addInvoice(
                    walletBean?.host!!,
                    appendAuth(walletBean?.accessToken!!),
                    getSatsNum()!!,
                    etMemo.getText()
                )
            }
        }
    }

    private fun changeShow() {
        binding?.apply {
            if (btnUnit.text == getString(R.string.btc)) {
                etAmount.etSwitch?.inputType = InputType.TYPE_CLASS_NUMBER
                btnUnit.text = getString(R.string.satoshis)
                tvAmount.text = etAmount.getText()
                tvUnit.text = getString(R.string.btc)
                etAmount.setContentText(NumberCountUtils.transformSatoshi(etAmount.getText()))
            } else {
                etAmount.etSwitch?.inputType = 8194
                btnUnit.text = getString(R.string.btc)
                tvAmount.text = etAmount.getText()
                tvUnit.text = getString(R.string.satoshis)
                etAmount.setContentText(NumberCountUtils.transformBTC(etAmount.getText()))
            }
        }

    }

    private fun getSatsNum(): String? {
        return if (binding?.btnUnit?.text == getString(R.string.satoshis))
            binding?.etAmount?.getText()
        else
            binding?.tvAmount?.text.toString()
    }

    //根据输入数量更新法币折合
    private fun changeUnitShow() {
        binding?.apply {
            tvAmount.text = if (btnUnit.text == getString(R.string.btc))
                NumberCountUtils.transformSatoshi(etAmount.getText())
            else
                NumberCountUtils.transformBTC(etAmount.getText())
            val satsNum = if (btnUnit.text == getString(R.string.satoshis))
                etAmount.getText()
            else
                tvAmount.text.toString()
            val rateEntity = CacheListManager.instance?.rateEntity
            tvConvert.text =
                when (RateAndLocalManager.getInstance(this@CreateInvoiceActivity).curRateKind) {
                    RateAndLocalManager.RateKind.CNY -> "¥${
                        NumberCountUtils.getLNConvert(
                            satsNum,
                            rateEntity?.btcCny
                        )
                    }"
                    RateAndLocalManager.RateKind.USD -> "$${
                        NumberCountUtils.getLNConvert(
                            satsNum,
                            rateEntity?.btcUsd
                        )
                    }"
                    RateAndLocalManager.RateKind.RUB -> "₽${
                        NumberCountUtils.getLNConvert(
                            satsNum,
                            rateEntity?.btcRub
                        )
                    }"
                }
        }
    }

    override fun onData() {
        super.onData()
        lightningViewModel.mInvoiceResultData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                payReq = it.payReq
                if (payReq != null) {
                    lightningViewModel.decodeInvoiceLocal(
                        /*walletBean?.host!!,
                        appendAuth(walletBean?.accessToken!!),*/ payReq!!
                    )
                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

        //decodeInvoice js观察者
        lightningViewModel.mDecodeLocalData.vmObserver(this) {
            onAppSuccess = {
                LogUtils.e("js-----$it")
                wvJs?.evaluateJavascript(it) { value ->
                    runCatching {
                        LogUtils.e(value)
                        val decodeInvoiceEntity =
                            Gson().fromJson(value, DecodeInvoiceEntity::class.java)
                        hideLoading()
                        decodeInvoiceEntity.payReq = payReq
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_LN_WALLET_INVOICE_DETAIL_ACTIVITY_PATH)
                            .withParcelable(
                                StringConstants.INVOICE_DATA,
                                PendingInvoiceEntity(
                                    decodeInvoiceEntity.description,
                                    decodeInvoiceEntity.timestamp?.toLong(),
                                    decodeInvoiceEntity.numSatoshis?.toLong(),
                                    decodeInvoiceEntity.payReq,
                                    decodeInvoiceEntity.expiry?.toInt()
                                )
                            )
                            .navigation()
                        EventBus.getDefault()
                            .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_REFRESH_RECORD_LN_WALLET))
                    }.onFailure { throwable ->
                        dismissSoftKeyboard()
                        hideLoading()
                        ToastUtils.shortToast(throwable.message!!)
                    }

                }
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

    }

    //检查按钮状态
    private fun checkButtonState() {
        binding?.apply {
            btnCreate.isEnabled = !TextUtils.isEmpty(etAmount.getText())
        }
    }

}