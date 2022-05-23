package com.linktech.saihub.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.databinding.FragmentMainWalletBinding
import com.linktech.saihub.databinding.ItemTokenBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_WALLET
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_CHANGE_WALLET_ADDRESS
import com.linktech.saihub.entity.event.MessageEvent.Companion.MESSAGE_ID_DELETE_WALLET
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.manager.RateAndLocalManager.RateKind
import com.linktech.saihub.manager.RateAndLocalManager.getInstance
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.repository.WalletRepository
import com.linktech.saihub.ui.dialog.wallet.WalletDrawerDialog
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.StringUtils
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

    //定时任务
    private var mCountdownJob: Job? = null

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
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
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_ADD_ACTIVITY_PATH)
                    .navigation()
            }

            srlWallet.setOnRefreshListener {
                it.finishRefresh(2000)
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
                    binding?.tvAddress?.text = StringUtils.formatAddress(currentWallet?.address)
                    //切換地址後U的餘額要重新查詢
                    currentWallet?.let { walletBean ->
                        walletAssetViewModel.getUSDTBalance(walletBean)
                    }
                }
            }
        }
    }

    //初始化钱包布局
    private fun initWalletView() {
        binding?.apply {
            srlWallet.setVisible(true)
            llAddStub.setVisible(false)
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
                currentWallet?.let { walletAssetViewModel.getBTCBalance(it) }
                currentWallet?.let { walletAssetViewModel.getUSDTBalance(it) }
            },
            onStart = {
                LogUtils.e("定时任务开始")
            },
            onFinish = {
                LogUtils.e("定时任务结束")
            })
    }

    //获取代币余额，代币列表信息
    private fun getData() {
        currentWallet?.id?.let { it1 -> walletAssetViewModel.getTokenList(it1) }
        currentWallet?.let { walletAssetViewModel.getBTCBalance(it) }
        currentWallet?.let { walletAssetViewModel.getUSDTBalance(it) }
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
                binding?.rvToken?.bindingAdapter?.notifyDataSetChanged()
                currentWallet?.id?.let { it1 -> walletAssetViewModel.getBalanceData(it1, context) }
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}