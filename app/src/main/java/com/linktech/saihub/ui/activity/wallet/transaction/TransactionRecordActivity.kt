package com.linktech.saihub.ui.activity.wallet.transaction

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityTransactionRecordBinding
import com.linktech.saihub.databinding.ItemRecordBinding
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.TransferServerBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.model.WalletTransactionViewModel
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.checkExt
import com.linktech.saihub.util.system.countDownCoroutines
import com.linktech.saihub.util.system.getBitcoinUrl
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Job

/**
 * 转账记录
 * Created by tromo on 2022/2/25.
 */
@Route(path = ARouterUrl.WAL_WALLET_TRANSACTION_RECORD_ACTIVITY_PATH)
class TransactionRecordActivity : BaseActivity() {

    private var binding: ActivityTransactionRecordBinding? = null

    override fun getStatusBlackMode(): Boolean = false

    override fun translucentStatusBar(): Boolean = true

    private var recordList: List<TransferServerBean>? = mutableListOf()

    private val walletBean: WalletBean? by lazy {
        intent.getParcelableExtra(StringConstants.WALLET_DATA)
    }

    private val tokenBean: TokenInfoBean? by lazy {
        intent.getSerializableExtra(StringConstants.TOKEN_DATA) as? TokenInfoBean
    }

    private val walletTransactionViewModel by lazy {
        ViewModelProvider(this)[WalletTransactionViewModel::class.java]
    }

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    //定时任务
    private var mCountdownJob: Job? = null

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_record)
        binding?.apply {

            tbRecord.setTitle(tokenBean?.tokenShort)

            tvBalance.text = tokenBean?.tokenBalance

            tvConvert.text =
                when (RateAndLocalManager.getInstance(this@TransactionRecordActivity).curRateKind) {
                    RateAndLocalManager.RateKind.CNY -> "≈¥${tokenBean?.currency}"
                    RateAndLocalManager.RateKind.USD -> "≈$${tokenBean?.currencyUsd}"
                    RateAndLocalManager.RateKind.RUB -> "≈₽${tokenBean?.currencyRub}"
                }

            if (tokenBean?.tokenShort == StringConstants.USDT) {
                //omni判断原生隔离见证不支持转账操作  仅支持私钥或助记词钱包
                if (walletBean?.existType == Constants.EXIST_PRIVATE_KEY || walletBean?.existType == Constants.EXIST_MNEMONIC) {
                    btnSend.setVisible(walletBean?.addressType == Constants.CHILD_ADDRESS_NESTED)
                } else {
                    btnSend.setVisible(false)
                }
            } else {
                btnSend.setVisible(walletBean?.existType != Constants.EXIST_ADDRESS)
                //公钥转账 判断导入内容
                if (walletBean?.existType == Constants.EXIST_PUBLIC_KEY)
                    btnSend.setVisible(walletBean?.publicKeyExt?.let { checkExt(it) } == true)
            }

            btnSend.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_TRANSACTION_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withSerializable(StringConstants.TOKEN_DATA, tokenBean)
                    .navigation()
            }

            btnReceive.onClick(Constants.CLICK_INTERVAL) {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_TRANSACTION_RECEIVE_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withString(StringConstants.COIN, tokenBean?.tokenShort)
                    .navigation()
            }

            srlRecord.setOnRefreshListener {
                it.finishRefresh(2000)
                walletBean?.let {
                    tokenBean?.let { it1 ->
                        walletAssetViewModel.getLocalTransactionRecord(
                            it,
                            it1
                        )
                    }
                }
            }

            rvRecord.linear().setup {
                addType<TransferServerBean>(R.layout.item_record)
                addType<String>(R.layout.layout_list_more)
                onBind {
                    when (itemViewType) {
                        R.layout.item_record -> {
                            val binding = getBinding<ItemRecordBinding>()
                            val model = getModel<TransferServerBean>()
                            binding.tvAddress.text =
                                StringUtils.formatAddress(
                                    if (model.type == 1)
                                        model.fromAddress
                                    else
                                        model.toAddress?.split(',')?.get(0)
                                )
                            //状态 0未知 1成功 2失败 3转账中
                            when (model.status) {
                                1 -> {
                                    //1 in 2 out
                                    if (model.type == 1) {
                                        binding.llType.setBackgroundResource(R.drawable.shape_in)
                                        binding.ivType.setBackgroundResource(R.mipmap.icon_in)
                                        binding.tvNum.setTextColor(resources.getColor(R.color.color_FF00C873))
                                    } else {
                                        binding.llType.setBackgroundResource(R.drawable.shape_out)
                                        binding.ivType.setBackgroundResource(R.mipmap.icon_out)
                                        binding.tvNum.setTextColor(resources.getColor(R.color.color_FFFF3750))
                                    }
                                }
                                2 -> {
                                    binding.llType.setBackgroundResource(R.drawable.shape_failed)
                                    binding.ivType.setBackgroundResource(R.mipmap.icon_failed)
                                    binding.tvNum.setTextColor(resources.getColor(R.color.color_FF686F7C))
                                }
                                3 -> {
                                    binding.llType.setBackgroundResource(R.drawable.shape_pendding)
                                    binding.ivType.setBackgroundResource(R.mipmap.icon_pendding)
                                    binding.tvNum.setTextColor(resources.getColor(R.color.color_FF026FED))
                                }
                            }
                        }
                    }
                }
                onClick(R.id.cl_root) {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
                        .withString(
                            StringConstants.KEY_URL,
                            getBitcoinUrl((_data as? TransferServerBean)?.hash, true)
                        )
                        .navigation()
                }
                //点击more 进入区块浏览器
                onClick(R.id.tv_more) {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
                        .withString(
                            StringConstants.KEY_URL,
                            getBitcoinUrl(walletBean?.address, false)
                        )
                        .navigation()
                }
            }.models = recordList
            rvRecord.bindingAdapter.addFooter("123")

            tbRecordType.configTabLayoutConfig {
                onSelectIndexChange = { _, selectIndexList, _, _ ->
                    setDataByType(selectIndexList.first())
                }
            }
        }

    }

    override fun onData() {
        super.onData()
        startTimerTask()
        walletBean?.let {
            tokenBean?.let { it1 ->
                walletAssetViewModel.getLocalTransactionRecord(
                    it,
                    it1
                )
            }
        }

        walletAssetViewModel.mBalanceData.vmObserver(this) {
            onAppSuccess = {
                walletBean?.id?.let { it1 ->
                    walletTransactionViewModel.getSingleBalanceData(
                        it1,
                        tokenBean?.tokenShort == StringConstants.BTC
                    )
                }
            }
        }

        walletTransactionViewModel.mTokenData.vmObserver(this) {
            onAppSuccess = {
                binding?.apply {
                    tvBalance.text = it.tokenBalance
                    tvConvert.text =
                        when (RateAndLocalManager.getInstance(this@TransactionRecordActivity).curRateKind) {
                            RateAndLocalManager.RateKind.CNY -> "≈¥${it.currency}"
                            RateAndLocalManager.RateKind.USD -> "≈$${it.currencyUsd}"
                            RateAndLocalManager.RateKind.RUB -> "≈₽${it.currencyRub}"
                        }
                }

            }
        }

        walletAssetViewModel.mRecordData.vmObserver(this) {
            onAppLoading = {
                if (binding?.srlRecord?.isRefreshing == false)
                    showLoading()
            }
            onAppSuccess = {
                hideLoading()
                recordList = it
                binding?.tbRecordType?.currentItemIndex?.let { it1 -> setDataByType(it1) }
            }
            onAppError = {
                hideLoading()
                it.errorMsg?.let { it1 -> ToastUtils.shortToast(it1) }
            }
        }

    }

    private fun setDataByType(index: Int) {
        binding?.apply {
            when (index) {
                0 -> rvRecord.models = recordList
                1 -> rvRecord.models = recordList?.filter {
                    it.status == 1 && it.type == 2
                }
                2 -> rvRecord.models = recordList?.filter {
                    it.status == 1 && it.type == 1
                }
                3 -> rvRecord.models = recordList?.filter {
                    it.status == 2
                }
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
                walletBean?.let {
                    if (tokenBean?.tokenShort == StringConstants.BTC)
                        walletAssetViewModel.getBTCBalance(it)
                    else
                        walletAssetViewModel.getUSDTBalance(it)
                }
            },
            onStart = {
                LogUtils.e("定时任务开始")
            },
            onFinish = {
                LogUtils.e("定时任务结束")
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountdownJob?.cancel()
    }
}