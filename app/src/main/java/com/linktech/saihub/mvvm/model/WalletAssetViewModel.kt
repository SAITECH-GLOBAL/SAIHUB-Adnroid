package com.linktech.saihub.mvvm.model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.TYPE_IMPORT
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.TransferServerBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.TransferServerDaoUtils
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.db.utils.WalletDbUtil.Companion.getWalletBalance
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.BaseViewModel
import com.linktech.saihub.mvvm.base.VmLiveData
import com.linktech.saihub.mvvm.base.VmState
import com.linktech.saihub.mvvm.base.launchVmRequest
import com.linktech.saihub.mvvm.repository.WalletRepository
import com.linktech.saihub.net.ex.ApiException
import com.linktech.saihub.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal


class WalletAssetViewModel() : BaseViewModel() {

    private val walletRepository by lazy { WalletRepository() }


    val mWalletListData by lazy { VmLiveData<List<WalletBean>>() }
    val mWalletListObserverData by lazy { VmLiveData<List<WalletBean>>() }

    val mRateData by lazy { VmLiveData<Boolean>() }

    val mBalanceData by lazy { VmLiveData<Boolean>() }

    val mSendServerData by lazy { VmLiveData<Boolean>() }

    val mCurrentWalletData by lazy { VmLiveData<WalletBean?>() }

    val mCurrentWalletBasicData by lazy { VmLiveData<WalletBean?>() }

    val mTokenListData by lazy { VmLiveData<List<TokenInfoBean>>() }

    val mWalletBalanceData by lazy { VmLiveData<String>() }

    val mRecordData by lazy { VmLiveData<List<TransferServerBean>>() }

    val mInsertRecordData by lazy { VmLiveData<Boolean>() }

    //上报地址
    fun sendAddressToServer(walletBean: WalletBean) {
        if (!walletBean.isReportAddressToServer) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val childAddressForTypeToServerList =
                        ChildAddressDaoUtil.getChildAddressForTypeToServerList(walletBean)
                    walletRepository.sendAddressToServer(
                        childAddressForTypeToServerList.map { it.childAddress },
                        0
                    )
                }.onSuccess {
                    if (it.data?.result == true) {
                        walletBean.isReportAddressToServer = true
                        WalletDaoUtils.updateWallet(walletBean)
                        LogUtils.i("server success")
                    }
                }

            }
        }
    }

    //获取BTC及USDT汇率
    fun getRate() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                walletRepository.getRate()
            }.onSuccess {
                val rateEntity = it.data?.result
                if (rateEntity != null) {
                    CacheListManager.instance?.saveExchangeRate(rateEntity)
                    mRateData.postValue(VmState.Success(true))
                }

            }.onFailure {
                mRateData.postValue(VmState.Fail(it))
            }
        }
    }

    private var btcJob: Job? = null
    private var uJob: Job? = null

    //获取BTC余额
    fun getBTCBalance(walletBean: WalletBean) {
        btcJob?.cancel()
        btcJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                walletRepository.getBTCBalance(walletBean.addressToServer)
            }.onSuccess {
                val jsonObject = it.data?.result
                if (jsonObject?.isJsonNull == true) {
                    return@launch
                }
                var balance: BigDecimal = BigDecimal.ZERO
                if (walletBean.addressToServer.contains(",")) {
                    val addressList = walletBean.addressToServer.split(",")
                    for (item in addressList) {
                        balance += parseBalanceData(jsonObject, item)
                    }
                } else {
                    balance = parseBalanceData(jsonObject, walletBean.addressToServer)
                }
                saveBTCBalance(walletBean, balance)
            }.onFailure {
                mBalanceData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    private fun parseBalanceData(jsonObject: JsonObject?, address: String): BigDecimal {
        return if (jsonObject?.has(address) == true && jsonObject.get(address).asJsonObject.has("final_balance")) {
            jsonObject.get(address).asJsonObject.get("final_balance").asBigDecimal
        } else {
            BigDecimal.ZERO
        }
    }

    //存储BTC余额到数据库
    private fun saveBTCBalance(walletBean: WalletBean, balance: BigDecimal) {
        runCatching {
            TokenDaoUtil.updateBTCBalanceForWallet(
                walletBean.id,
                balance,
                CacheListManager.instance?.rateEntity
            )
        }.onSuccess {
            mBalanceData.postValue(VmState.Success(true))
        }.onFailure {
            mBalanceData.postValue(VmState.Error(ApiException(it)))
        }
    }

    //获取USDT余额
    fun getUSDTBalance(walletBean: WalletBean) {
        uJob?.cancel()
        uJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                walletRepository.getOMNIBalance(walletBean.address)
            }.onSuccess {
                val omniBalanceEntity = it.data?.result
                omniBalanceEntity?.balance?.let { it1 -> saveUSDTBalance(walletBean, it1) }
            }.onFailure {
                mBalanceData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //存储USDT余额到数据库
    private fun saveUSDTBalance(walletBean: WalletBean, balance: String) {
        runCatching {
            TokenDaoUtil.updateUSDTBalanceForWallet(
                walletBean.id,
                balance,
                CacheListManager.instance?.rateEntity
            )
        }.onSuccess {
            mBalanceData.postValue(VmState.Success(true))
        }.onFailure {
            mBalanceData.postValue(VmState.Error(ApiException(it)))
        }
    }

    //获取当前钱包信息
    fun getCurrentWallet(isBasic: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.getCurrent()
            }.onSuccess {
                if (isBasic)
                    mCurrentWalletBasicData.postValue(VmState.Success(it))
                else
                    mCurrentWalletData.postValue(VmState.Success(it))
            }.onFailure {
                if (isBasic)
                    mCurrentWalletBasicData.postValue(VmState.Error(ApiException(it)))
                else
                    mCurrentWalletData.postValue(VmState.Error(ApiException(it)))
            }
        }

    }

    //获取钱包代币信息
    fun getTokenList(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                TokenDaoUtil.loadTokenListForWalId(id)
            }.onSuccess {
                mTokenListData.postValue(VmState.Success(it))
            }.onFailure {
                mTokenListData.postValue(VmState.Error(ApiException(it)))
            }
        }

    }

    //获取钱包余额信息
    fun getBalanceData(id: Long, context: Context?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getWalletBalance(
                    id,
                    RateAndLocalManager.getInstance(context).curRateKind
                )
            }.onSuccess {
                mWalletBalanceData.postValue(VmState.Success(it))
            }.onFailure {
                mWalletBalanceData.postValue(VmState.Error(ApiException(it)))
            }
        }

    }

    //查询本地及网络转账记录
    fun getLocalTransactionRecord(walletBean: WalletBean, tokenInfoBean: TokenInfoBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mRecordData.postValue(VmState.Loading)
                val localList =
                    TransferServerDaoUtils.loadTransferPadding(walletBean, tokenInfoBean)

                //传入地址为钱包所有子地址+找零地址
                val address = when (walletBean.existType) {
                    Constants.EXIST_ADDRESS, Constants.EXIST_PRIVATE_KEY -> walletBean.address
                    else -> {
                        ChildAddressDaoUtil.getChildAddressForTypeToServer(walletBean)
                    }
                }
                val restRecord = walletRepository.getTransactionRecord(
                    address,
                    tokenInfoBean.tokenShort
                )

                val restList = restRecord.data?.result?.records?.toMutableList()
                //删除交易成功的数据
                val deleteHash = localList.filter { local ->
                    restList?.any { rest ->
                        local.hash == rest.hash
                    } == true
                }.map {
                    it.hash
                }
                TransferServerDaoUtils.delete(deleteHash)

                //删除后在查询一遍
                val pendingList =
                    TransferServerDaoUtils.loadTransferPadding(walletBean, tokenInfoBean)
                //拼接list
                val recordList = mutableListOf<TransferServerBean>()
                recordList.addAll(pendingList)
                restList?.let { recordList.addAll(it) }
                //按照时间倒序排序
                recordList.sortedWith(compareByDescending { it.timestamp })
            }.onSuccess {
                mRecordData.postValue(VmState.Success(it))
            }.onFailure {
                mRecordData.postValue(VmState.Error(ApiException(it)))
            }

        }
    }

    //存储本地pending交易记录
    fun saveLocalTransactionRecord(transferSendBean: TransferSendBean, hash: String, id: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val transferServerBean = TransferServerBean().apply {
                    this.hash = hash
                    this.walletId = id!!
                    this.timestamp = System.currentTimeMillis()
                    this.toAddress = transferSendBean.toAddress
                    this.amount = transferSendBean.moneyNumber
                    this.status = 3
                    this.type = 2
                    this.coin = transferSendBean.tokenName
                }
                TransferServerDaoUtils.insert(transferServerBean)
            }.onSuccess {
                mInsertRecordData.postValue(VmState.Success(true))
            }.onFailure {
                mInsertRecordData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //获取钱包列表
    fun getWalletList() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDaoUtils.loadAll()
            }.onSuccess {
                mWalletListData.postValue(VmState.Success(it))
            }.onFailure {
                mWalletListData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }
}