package com.linktech.saihub.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.app.StringConstants.BITCOIND_TX
import com.linktech.saihub.app.StringConstants.PAID_INVOICE
import com.linktech.saihub.app.StringConstants.USER_INVOICE_OUT
import com.linktech.saihub.app.StringConstants.USER_INVOICE_PAID
import com.linktech.saihub.app.StringConstants.USER_INVOICE_WAIT
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.databinding.FragmentMainWalletBinding
import com.linktech.saihub.databinding.ItemLnRecordBinding
import com.linktech.saihub.databinding.ItemTokenBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_WALLET_ADDRESS
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_DELETE_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_REFRESH_RECORD_LN_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_REFRESH_TOKEN_LN_WALLET
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.entity.lightning.PendingInvoiceEntity
import com.linktech.saihub.entity.lightning.TxEntity
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.manager.RateAndLocalManager.RateKind
import com.linktech.saihub.manager.RateAndLocalManager.getInstance
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.ui.dialog.SelectRechargeTypeDialog
import com.linktech.saihub.ui.dialog.SelectWalletTypeDialog
import com.linktech.saihub.ui.dialog.wallet.WalletDrawerDialog
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.appendAuth
import com.linktech.saihub.util.system.countDownCoroutines
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainWalletFragment : BaseFragment() {

    private var binding: FragmentMainWalletBinding? = null

    private var walletDrawerDialog: WalletDrawerDialog? = null

    private var tokenList: List<TokenInfoBean>? = mutableListOf()

    private var currentWallet: WalletBean? = null

    private var recordList: List<TxEntity>? = mutableListOf()

    //定时任务
    private var mCountdownJob: Job? = null

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainWalletBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun initViews() {
        getCurrentWallet()
        binding?.apply {
            btnDrawer.onClick(Constants.CLICK_INTERVAL) {
                walletDrawerDialog = WalletDrawerDialog()
                walletDrawerDialog?.showNow(childFragmentManager, "")
            }

            btnMore.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ACTIVITY_PATH)
                    .navigation()
            }

            btnCreate.onClick(Constants.CLICK_INTERVAL) {
                val sizeList =
                    arrayListOf(0, 0)
                val selectWalletTypeDialog = SelectWalletTypeDialog.newInstance(sizeList)
                selectWalletTypeDialog.showNow(childFragmentManager, "")
            }

            srlWallet.setOnRefreshListener {
                it.finishRefresh(2000)
                if (currentWallet?.existType == Constants.EXIST_LIGHTNING)
                    getLnData()
                else
                    getData()
            }

            tvAddress.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_TRANSACTION_RECEIVE_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                    .withString(StringConstants.COIN, StringConstants.BTC)
                    .navigation()
            }
        }
    }

    override fun addEvent() {
        lightningViewModel.mAccountData.vmObserver(this) {
            onAppSuccess = {
//                ToastUtils.shortToast(it.array?.get(0)?.address!!)
                ToastUtils.shortToast(it.login!!)
            }
        }
        walletAssetViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                if (it == null) {
                    binding?.llAddStub?.setVisible(true)
                    binding?.srlWallet?.setVisible(false)
                } else {
                    currentWallet = it
                    //上报地址
                    walletAssetViewModel.sendAddressToServer(currentWallet!!)
                    initWalletView()
                }

            }
        }
        walletAssetViewModel.mTokenListData.vmObserver(this) {
            onAppSuccess = {
                binding?.rvToken?.models = it
            }
        }
        walletAssetViewModel.mBalanceData.vmObserver(this) {
            onAppSuccess = {
                currentWallet?.id?.let { it1 -> walletAssetViewModel.getTokenList(it1) }
                currentWallet?.id?.let { it1 -> walletAssetViewModel.getBalanceData(it1, context) }
            }
        }
        walletAssetViewModel.mWalletBalanceData.vmObserver(this) {
            onAppSuccess = {
                binding?.tvBalance?.text = when (getInstance(context).curRateKind) {
                    RateKind.CNY -> "¥$it"
                    RateKind.USD -> "$$it"
                    RateKind.RUB -> "₽$it"
                }
            }
        }

        //搁浅逻辑
        walletAssetViewModel.mWalletListData.vmObserver(this) {
            onAppSuccess = {
                //如果钱包列表为空，重置密码或指纹登录标志
                if (it.isEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOCAL, "")
                        MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOGIN, false)
                        MMKVManager.getInstance().mmkv()
                            .encode(Constants.VERIFY_TOUCH_ID_LOGIN, false)
                    }

                }
            }
        }

        walletAssetViewModel.mCurrentWalletBasicData.vmObserver(this) {
            onAppSuccess = {
                if (it != null) {
                    currentWallet = it
                    if (it.existType == Constants.EXIST_LIGHTNING) {
                        binding?.apply {
                            tvLnWalletName.text = currentWallet?.name
                            if (tvUnit.text == getString(R.string.satoshis)) {
                                tvLnBalance.text = currentWallet?.lnSat
                            } else {
                                tvLnBalance.text = currentWallet?.lnBalance
                            }
                            tvLnConvert.text = when (getInstance(context).curRateKind) {
                                RateKind.CNY -> "¥${it.asset}"
                                RateKind.USD -> "$${it.assetUSD}"
                                RateKind.RUB -> "₽${it.assetRub}"
                            }
                        }
                    } else {
                        binding?.tvWalletName?.text = currentWallet?.name
                        binding?.tvAddress?.text = StringUtils.formatAddress(currentWallet?.address)
                        //切換地址後U的餘額要重新查詢
                        currentWallet?.let { walletBean ->
                            walletAssetViewModel.getUSDTBalance(walletBean)
                        }
                    }
                }
            }
        }

        lightningViewModel.mBalanceData.vmObserver(this) {
            onAppSuccess = {
                getCurrentWalletForBasic()
            }
        }

        lightningViewModel.mRecordListData.vmObserver(this) {
            onAppLoading = {
//                (activity as? BaseActivity)?.showLoading()
            }
            onAppSuccess = {
//                (activity as? BaseActivity)?.hideLoading()
                binding?.rvLnRecord?.models = it
            }
            onAppError = {
//                (activity as? BaseActivity)?.hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }

        //链上充值本地钱包判断是否有可转账钱包
        lightningViewModel.mWalletListData.vmObserver(this) {
            onAppSuccess = {
                if (it.isNotEmpty())
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_LN_WALLET_LIST_ACTIVITY_PATH)
                        .withString(StringConstants.ADDRESS, currentWallet?.address)
                        .navigation()
                else
                    ToastUtils.shortImageToast(getText(R.string.no_local_wallet))
            }
            onAppError = {

            }
        }
    }

    //初始化钱包布局 区分闪电钱包
    @SuppressLint("SetTextI18n")
    private fun initWalletView() {
        binding?.apply {
            srlWallet.setVisible(true)
            llAddStub.setVisible(false)
            if (currentWallet?.existType == Constants.EXIST_LIGHTNING) {
                initLNWallet()
            } else {
                initWallet()
            }
        }
    }

    private fun initWallet() {
        binding?.apply {
            clWallet.setVisible(true)
            clLnWallet.setVisible(false)
            tvWalletName.text = currentWallet?.name
            tvAddress.text = StringUtils.formatAddress(currentWallet?.address)
            tvBalance.text =
                when (getInstance(context).curRateKind) {
                    RateKind.CNY -> "¥${currentWallet?.asset}"
                    RateKind.USD -> "$${currentWallet?.assetUSD}"
                    RateKind.RUB -> "₽${currentWallet?.assetRub}"
                }
            rvToken.linear().setup {
                addType<TokenInfoBean>(R.layout.item_token)
                onBind {
                    setAnimation(AnimationType.SLIDE_LEFT)
                    val binding = getBinding<ItemTokenBinding>()
                    Glide.with(context).load(getModel<TokenInfoBean>().logo)
                        .into(binding.ivToken)
                    binding.tvAsset.text = when (getInstance(context).curRateKind) {
                        RateKind.CNY -> "≈¥${getModel<TokenInfoBean>().currency}"
                        RateKind.USD -> "≈$${getModel<TokenInfoBean>().currencyUsd}"
                        RateKind.RUB -> "≈₽${getModel<TokenInfoBean>().currencyRub}"
                    }
                }
                onClick(R.id.cl_root) {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_WALLET_TRANSACTION_RECORD_ACTIVITY_PATH)
                        .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                        .withSerializable(StringConstants.TOKEN_DATA, _data as? TokenInfoBean)
                        .navigation()
                }
            }.models = tokenList
            getData()
        }
    }

    private fun initLNWallet() {
        binding?.apply {
            clWallet.setVisible(false)
            clLnWallet.setVisible(true)
            tvLnWalletName.text = currentWallet?.name
            tvLnBalance.text = currentWallet?.lnBalance
            btnChainRecharge.setVisible(!TextUtils.isEmpty(currentWallet?.address))
            tvUnit.text = getString(R.string.btc)
            tvLnConvert.text = when (getInstance(context).curRateKind) {
                RateKind.CNY -> "¥${currentWallet?.asset}"
                RateKind.USD -> "$${currentWallet?.assetUSD}"
                RateKind.RUB -> "₽${currentWallet?.assetRub}"
            }
            //切换单位
            tvUnit.onClick {
                changeUnit()
            }
            tvLnBalance.onClick {
                changeUnit()
            }
            //钱包管理
            btnLnManage.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_SETTINGS_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                    .navigation()
            }
            //收款
            btnReceive.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_INVOICE_CREATE_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                    .navigation()
            }
            //交易
            btnSend.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_TRANSACTION_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                    .navigation()
            }
            //链上充值
            btnChainRecharge.onClick(Constants.CLICK_INTERVAL) {
                val selectRechargeTypeDialog = SelectRechargeTypeDialog()
                selectRechargeTypeDialog.itemChildEvent = {
                    if (it == 0) {
                        lightningViewModel.getTransferWallet()
                    } else {
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_WALLET_TRANSACTION_RECEIVE_ACTIVITY_PATH)
                            .withParcelable(StringConstants.WALLET_DATA, currentWallet)
                            .withString(StringConstants.COIN, StringConstants.BTC)
                            .navigation()
                    }
                }
                selectRechargeTypeDialog.showNow(parentFragmentManager, "")
            }
            rvLnRecord.linear().setup {
                addType<TxEntity>(R.layout.item_ln_record)
                onBind {
//                    val isBTCUnit = tvUnit.text == getString(R.string.btc)
                    val binding = getBinding<ItemLnRecordBinding>()
                    val model = getModel<TxEntity>()
                    binding.apply {
                        val value =
//                            if (isBTCUnit)
//                                NumberCountUtils.transformBTC(model.value.toString())
//                            else
                            model.value.toString()
                        //状态
                        when (model.type) {
                            //用户支付 付款
                            PAID_INVOICE -> {
                                llType.setBackgroundResource(R.drawable.shape_out)
                                ivType.setBackgroundResource(R.mipmap.icon_ln_out)
                                tvNum.setTextColor(resources.getColor(R.color.color_FFFF3750))
                                tvNum.text = "-$value"
                                tvNum.textSize = 18f
                            }
                            //链上交易
                            BITCOIND_TX -> {
                                llType.setBackgroundResource(R.drawable.shape_pendding)
                                ivType.setBackgroundResource(R.mipmap.icon_ln_on_chain)
                                tvNum.setTextColor(resources.getColor(R.color.color_FF026FED))
                                tvNum.text = "+$value"
                                tvNum.textSize = 18f
                            }
                            //用户创建 收款
                            USER_INVOICE_PAID -> {
                                llType.setBackgroundResource(R.drawable.shape_in)
                                ivType.setBackgroundResource(R.mipmap.icon_ln_in)
                                tvNum.setTextColor(resources.getColor(R.color.color_FF00C873))
                                tvNum.text = "+$value"
                                tvNum.textSize = 18f
                            }
                            //等待状态
                            USER_INVOICE_WAIT -> {
                                llType.setBackgroundResource(R.drawable.shape_in)
                                ivType.setBackgroundResource(R.mipmap.icon_ln_wait)
                                tvNum.setTextColor(resources.getColor(R.color.color_FF00C873))
                                tvNum.text = "$value"
                                tvNum.textSize = 18f
                            }
                            //已过期
                            USER_INVOICE_OUT -> {
                                llType.setBackgroundResource(R.drawable.shape_failed)
                                ivType.setBackgroundResource(R.mipmap.icon_ln_out_date)
                                tvNum.setTextColor(resources.getColor(R.color.color_FF686F7C))
                                tvNum.text = getString(R.string.out_of_date)
                                tvNum.textSize = 12f
                            }
                        }
                    }
                }
                onClick(R.id.cl_root) {
                    val txEntity = _data as? TxEntity
                    if (txEntity?.type == USER_INVOICE_WAIT)
                        ARouter.getInstance()
                            .build(ARouterUrl.WAL_LN_WALLET_INVOICE_DETAIL_ACTIVITY_PATH)
                            .withParcelable(
                                StringConstants.INVOICE_DATA,
                                PendingInvoiceEntity(
                                    txEntity.memo,
                                    txEntity.time,
                                    txEntity.value,
                                    txEntity.payReq,
                                    txEntity.expireTime
                                )
                            )
                            .navigation()
                }
            }.models = recordList
            getLnData()
        }
    }

    //切换单位
    private fun changeUnit() {
        binding?.apply {
            if (tvUnit.text == getString(R.string.btc)) {
                tvUnit.text = getString(R.string.satoshis)
                tvLnBalance.text = currentWallet?.lnSat
            } else {
                tvUnit.text = getString(R.string.btc)
                tvLnBalance.text = currentWallet?.lnBalance
            }
        }
    }

    private fun startTimerTask() {
        mCountdownJob?.cancel()
        mCountdownJob = countDownCoroutines(
            Constants.ASSET_REFRESH_INTERVAL,
            lifecycleScope,
            onTick = {
                //定时刷新资产
                LogUtils.e("定时刷新资产")
                //获取汇率
                walletAssetViewModel.getRate()
                if (currentWallet?.existType == Constants.EXIST_LIGHTNING) {
                    currentWallet?.host?.let { host ->
                        lightningViewModel.getBalance(
                            host,
                            currentWallet?.accessToken?.let { appendAuth(it) }!!,
                            currentWallet?.id!!
                        )
                    }
                } else {
                    currentWallet?.let { walletAssetViewModel.getBTCBalance(it) }
                    currentWallet?.let { walletAssetViewModel.getUSDTBalance(it) }
                }

            },
            onStart = {
                LogUtils.e("定时任务开始")
            },
            onFinish = {
                LogUtils.e("定时任务结束")
            })
    }

    //获取代币余额，代币列表信息 普通钱包
    private fun getData() {
        currentWallet?.id?.let { it1 -> walletAssetViewModel.getTokenList(it1) }
        currentWallet?.let { walletAssetViewModel.getBTCBalance(it) }
        currentWallet?.let { walletAssetViewModel.getUSDTBalance(it) }
        startTimerTask()
    }

    private fun getLnData() {
        currentWallet?.host?.let { host ->
            lightningViewModel.getBalance(
                host,
                currentWallet?.accessToken?.let { appendAuth(it) }!!,
                currentWallet?.id!!
            )
        }
        currentWallet?.host?.let { host ->
            currentWallet?.id?.let {
                lightningViewModel.getTransactionRecord(
                    it,
                    host,
                    currentWallet?.accessToken?.let { appendAuth(it) }!!
                )
            }
        }
        startTimerTask()
    }

    private fun getCurrentWallet() {
        walletAssetViewModel.getCurrentWallet(false)
    }

    private fun getCurrentWalletForBasic() {
        walletAssetViewModel.getCurrentWallet(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshWallet(event: MessageEvent) {
        when (event.id) {
            MessageEvent.MESSAGE_ID_CHANGE_SET_PASSPHRASE_CONVERT_WALLET -> {
                getCurrentWallet()
            }
            MessageEvent.MESSAGE_ID_CHANGE_CREATE_WALLET, MESSAGE_ID_CHANGE_WALLET, MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET -> {
                //创建钱包操作
                getCurrentWallet()
            }
            MESSAGE_ID_CHANGE_WALLET_ADDRESS -> {
                getCurrentWalletForBasic()
            }
            MESSAGE_ID_DELETE_WALLET -> {
                getCurrentWallet()
                //重新订阅socket
                EventBus.getDefault().postSticky(SocketEvent())
            }
            MessageEvent.MESSAGE_ID_CHANGE_CURRENCY_UNIT -> {
                //切换币种
                if (currentWallet?.existType == Constants.EXIST_LIGHTNING) {
                    getCurrentWalletForBasic()
                } else {
                    binding?.rvToken?.bindingAdapter?.notifyDataSetChanged()
                    currentWallet?.id?.let { it1 ->
                        walletAssetViewModel.getBalanceData(
                            it1,
                            context
                        )
                    }
                }
            }
            MESSAGE_ID_REFRESH_RECORD_LN_WALLET -> {
                getLnData()
            }
            MESSAGE_ID_REFRESH_TOKEN_LN_WALLET -> {
                if (currentWallet?.existType == Constants.EXIST_LIGHTNING)
                    lightningViewModel.refreshToken(
                        currentWallet?.id!!,
                        currentWallet?.host!!,
                        currentWallet?.refreshToken!!
                    )
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}