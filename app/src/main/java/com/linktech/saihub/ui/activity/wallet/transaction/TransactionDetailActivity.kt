package com.linktech.saihub.ui.activity.wallet.transaction

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityTransactionDetailBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.manager.AES
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.model.WalletTransactionViewModel
import com.linktech.saihub.ui.activity.wallet.manager.VerifyPwdActivity
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.sys.WalTextWatch
import com.qmuiteam.qmui.kotlin.onClick
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 交易详情
 * Created by tromo on 2022/3/1.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_DETAIL_ACTIVITY_PATH)
class TransactionDetailActivity : BaseActivity() {

    private var binding: ActivityTransactionDetailBinding? = null

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    private val transferSendBean: TransferSendBean? by lazy {
        intent.getSerializableExtra(StringConstants.TRANSFER_SEND_DATA) as? TransferSendBean
    }

    private val tokenInfoBean: TokenInfoBean? by lazy {
        intent.getSerializableExtra(StringConstants.TOKEN_DATA) as? TokenInfoBean
    }

    private val txHex: String? by lazy {
        intent.getStringExtra(StringConstants.TRANSACTION_HEX)
    }

    private var walletBean: WalletBean? = null

    private val walletTransactionViewModel by lazy {
        ViewModelProvider(this)[WalletTransactionViewModel::class.java]
    }

    override fun translucentStatusBar(): Boolean = true

    var fingerprintIdentify: FingerprintIdentify? = null

    private var isPassword = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onInit() {
        super.onInit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_detail)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)

        binding?.apply {

            tvAddress.text = transferSendBean?.toAddress
            tvCoin.text = transferSendBean?.tokenName
            tvNum.text = transferSendBean?.moneyNumber
            tvConvert.text = transferSendBean?.convert
            tvFee.text =
                "${getString(R.string.fee_desc)}${transferSendBean?.gas}${StringConstants.BTC}(${transferSendBean?.gasConvert})"

            etWalletPassword.etSwitch?.addTextChangedListener(object : WalTextWatch() {
                override fun onTextChanged(s: String?) {
                    btnSend.isEnabled = !TextUtils.isEmpty(s)
                }

            })

            initUI()

            //切换支付方式
            tvType.onClick() {
                if (walletBean?.isOpenTouchIdPay == false) {
                    //未开通跳转 开通流程
                    if (fingerprintIdentify?.isFingerprintEnable != true) {
                        ToastUtils.shortToast(getString(R.string.open_touchid_hint))
                        return@onClick
                    }

                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_VERIFY_PWD_ACTIVITY_PATH)
                        .withInt(StringConstants.VERIFY_TYPE, VerifyPwdActivity.VERIFY_TOUCHID)
                        .withParcelable(StringConstants.WALLET_DATA, walletBean)
                        .navigation()
                } else {
                    setVerifyType()
                }
            }

            btnSend.onClick(Constants.CLICK_INTERVAL) {
                //验证密码
                if (etWalletPassword.getText() == AES.decrypt(walletBean?.password)) {
                    txHex?.let { it1 -> walletTransactionViewModel.sendTransaction(it1) }
                } else {
                    etWalletPassword.setErrorTip(getString(R.string.password_error_try_again))
                }
            }

            ivFingerprint.onClick(Constants.CLICK_INTERVAL) {
                startFinger()
            }

            btnNext.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WALLET_TRANSACTION_RECORD_ACTIVITY_PATH)
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withSerializable(StringConstants.TOKEN_DATA, tokenInfoBean)
                    .navigation(this@TransactionDetailActivity)
            }
        }
    }

    override fun onData() {
        super.onData()
        walletTransactionViewModel.mTxHashData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                transferSendBean?.let { it1 ->
                    walletAssetViewModel.saveLocalTransactionRecord(
                        it1,
                        it, walletBean?.id
                    )
                }
            }
            onAppError = {
                hideLoading()
                it.errorMsg.let { it1 -> ToastUtils.shortImageToast(it1) }
            }
        }

        walletAssetViewModel.mInsertRecordData.vmObserver(this) {
            onAppSuccess = {
                hideLoading()
                ToastUtils.shortRightImageToast(getString(R.string.send_success_tip))
                binding?.llSuccess?.setVisible(true)
                binding?.gpCheck?.setVisible(false)
                binding?.ivFingerprint?.setVisible(false)
                binding?.tvType?.setVisible(false)
            }
            onAppError = {
                hideLoading()
                it.errorMsg.let { it1 -> ToastUtils.shortImageToast(it1) }
            }
        }

        walletAssetViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                walletBean = it
                isPassword = walletBean?.isOpenTouchIdPay == false
                setVerifyType()
            }
        }

    }

    private fun setVerifyType() {
        binding?.apply {
            if (isPassword) {
                gpCheck.setVisible(true)
                ivFingerprint.setVisible(false)
                etWalletPassword.etSwitch?.setText("")
                tvType.text = getString(R.string.touch_pay)
                fingerprintIdentify?.cancelIdentify()
            } else {
                gpCheck.setVisible(false)
                ivFingerprint.setVisible(true)
                tvType.text = getString(R.string.pwd_pay)
                startFinger()
            }
            isPassword = !isPassword
        }
    }

    private fun initUI() {
        initFingerPrint()
        binding?.apply {

            if (walletBean?.isOpenTouchIdPay == false) {
                gpCheck.setVisible(true)
                ivFingerprint.setVisible(false)
                tvType.text = getString(R.string.touch_pay)
                fingerprintIdentify?.cancelIdentify()
                isPassword = false
            } else {
                gpCheck.setVisible(false)
                ivFingerprint.setVisible(true)
                tvType.text = getString(R.string.pwd_pay)
                startFinger()
            }
        }
    }

    private fun initFingerPrint() {
        fingerprintIdentify = FingerprintIdentify(mContext)
        fingerprintIdentify?.setSupportAndroidL(true)
        fingerprintIdentify?.init()
    }

    private var verifyErrorNumber = 0
    private fun startFinger() {
        fingerprintIdentify?.startIdentify(Constants.MAX_AVAILABLE_TIMES,
            object : BaseFingerprint.IdentifyListener {
                override fun onSucceed() {
                    txHex?.let { it1 -> walletTransactionViewModel.sendTransaction(it1) }
                    fingerprintIdentify?.cancelIdentify()
                }

                override fun onNotMatch(availableTimes: Int) {
                    ToastUtils.shortImageToast(getString(R.string.fingerprint_verification_failed))
                }

                override fun onFailed(isDeviceLocked: Boolean) {
                    verifyErrorNumber++
                    ToastUtils.shortImageToast(getString(R.string.fingerprint_verification_failed))
                }

                override fun onStartFailedByDeviceLocked() {
                    ToastUtils.shortImageToast(getString(R.string.device_locked))
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshWallet(event: MessageEvent) {
        when (event.id) {
            MessageEvent.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET -> {
                //创建钱包操作
                walletAssetViewModel.getCurrentWallet(false)
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}