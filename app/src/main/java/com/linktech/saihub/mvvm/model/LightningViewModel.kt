package com.linktech.saihub.mvvm.model

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.linktech.saihub.R
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.db.utils.WalletDbUtil
import com.linktech.saihub.entity.lightning.*
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.mvvm.base.BaseViewModel
import com.linktech.saihub.mvvm.base.VmLiveData
import com.linktech.saihub.mvvm.base.VmState
import com.linktech.saihub.mvvm.base.launchNewVmRequest
import com.linktech.saihub.mvvm.repository.LightningRepository
import com.linktech.saihub.net.ex.ApiException
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.appendAuth
import com.linktech.saihub.util.system.formatDomain
import com.linktech.saihub.util.system.keySendToDomain
import kotlinx.coroutines.*
import java.math.BigDecimal

//Lightning
class LightningViewModel() : BaseViewModel() {

    private val lightningRepository by lazy { LightningRepository() }

    val mAccountData by lazy { VmLiveData<WalletBean>() }
    val mTokenData by lazy { VmLiveData<AccessTokenEntity>() }
    val mRefreshTokenData by lazy { VmLiveData<AccessTokenEntity>() }
    val mAddressData by lazy { VmLiveData<BTCAddressEntity>() }
    val mInvoiceResultData by lazy { VmLiveData<InvoiceEntity>() }
    val mRecordListData by lazy { VmLiveData<List<TxEntity>>() }
    val mBalanceData by lazy { VmLiveData<Boolean>() }
    val mDecodeData by lazy { VmLiveData<DecodeInvoiceEntity>() }
    val mPayResultData by lazy { VmLiveData<Any>() }
    val mKeySendResultData by lazy { VmLiveData<KeySendEntity>() }
    val mKeySendCallbackData by lazy { VmLiveData<KeySendCallbackEntity>() }
    val mPwdResultData by lazy { VmLiveData<Boolean>() }
    val mNameResultData by lazy { VmLiveData<WalletBean>() }
    val mTouchIdResultData by lazy { VmLiveData<Boolean>() }
    val mExportResultData by lazy { VmLiveData<String>() }
    val mWalletListData by lazy { VmLiveData<List<WalletBean>>() }
    val mInputTypeData by lazy { VmLiveData<String>() }
    val mDecodeLocalData by lazy { VmLiveData<String>() }

    fun createAccount(host: String, walletName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mAccountData.postValue(VmState.Loading)
                val accountEntity = lightningRepository.createAccount(formatDomain(host))
                accountEntity
            }.onSuccess {
                if (!TextUtils.isEmpty(it.login) && !TextUtils.isEmpty(it.password))
                    getLNToken(host, it.login!!, it.password!!, walletName)
                else
                    mAccountData.postValue(VmState.Error(ApiException("login pwd null")))
            }.onFailure {
                mAccountData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    fun getAccessToken(host: String, login: String, password: String) {
        launchNewVmRequest(
            { lightningRepository.getAccessToken(host, login, password) },
            mTokenData
        )
    }

    fun getBTCAddress(host: String, authorization: String) {
        launchNewVmRequest({ lightningRepository.getBTCAddress(host, authorization) }, mAddressData)
    }

    //刷新token
    fun refreshToken(id: Long, host: String, refreshToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val accountEntity = lightningRepository.refreshToken(host, refreshToken)
                accountEntity
            }.onSuccess {
                WalletDbUtil.updateLNToken(id, it.accessToken, it.refreshToken)
            }.onFailure {
                mRefreshTokenData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    fun addInvoice(
        host: String,
        authorization: String,
        amt: String,
        memo: String
    ) {
        launchNewVmRequest(
            { lightningRepository.addInvoice(host, authorization, amt, memo) },
            mInvoiceResultData
        )
    }

    fun getBalance(
        host: String,
        authorization: String,
        id: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val balance = lightningRepository.getBalance(host, authorization)
                if (balance.bTC?.availableBalance != null)
                    CacheListManager.instance?.rateEntity?.let {
                        WalletDbUtil.updateLNConvert(
                            id, balance.bTC.availableBalance,
                            it
                        )
                    }
                true
            }.onSuccess {
                mBalanceData.postValue(VmState.Success(it))
            }
        }
    }

    fun decodeInvoice(host: String, authorization: String, invoice: String) {
        launchNewVmRequest(
            { lightningRepository.decodeInvoice(host, authorization, invoice) },
            mDecodeData
        )
    }

    fun payInvoice(host: String, authorization: String, invoice: String, amount: String) {
        launchNewVmRequest(
            { lightningRepository.payInvoice(host, authorization, invoice, amount) },
            mPayResultData
        )
    }

    //解析转账输入内容
    fun analyzeInput(host: String, authorization: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (content.contains("@") && !content.startsWith("@")) {
                    //keysend
                    val split = content.split("@")
                    mInputTypeData.postValue(VmState.Success(StringConstants.INPUT_TYPE_KEYSEND))
                    parseKeySend(keySendToDomain(split[1]), authorization, split[0])
                } else {
                    //invoice
                    mInputTypeData.postValue(VmState.Success(StringConstants.INPUT_TYPE_INVOICE))
                    decodeInvoiceLocal(/*host, authorization,*/ content)
                }
            }.onFailure {
                mInputTypeData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    private fun parseKeySend(host: String, authorization: String, user: String) {
        launchNewVmRequest(
            { lightningRepository.parseKeySend(host, authorization, user) },
            mKeySendResultData
        )
    }

    fun getKeySendCallback(
        host: String,
        url: String,
        authorization: String,
        amount: Long,
        nonce: Long,
        comment: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mPayResultData.postValue(VmState.Loading)
                //callback获取pr
                val keySendCallback = lightningRepository.getKeySendCallback(
                    url,
                    authorization,
                    amount,
                    nonce,
                    comment
                )
                if (keySendCallback.status == "ERROR") {
                    mPayResultData.postValue(VmState.Error(ApiException(keySendCallback.reason)))
                    return@launch
                }
                //pr解析invoice
//                val decodeInvoiceEntity = keySendCallback.pr?.let {
//                    lightningRepository.decodeInvoice(
//                        host, authorization,
//                        it
//                    )
//                }
                //支付invoice
                keySendCallback.pr?.let {
                    lightningRepository.payInvoiceUrlType(
                        host, authorization,
                        it, amount.toString()
                    )
                }
            }.onSuccess {
                mPayResultData.postValue(VmState.Success(it!!))
            }.onFailure {
                mPayResultData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //导入
    fun importLnHub(hub: String, walletName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                var infoList: List<String>? = null
                mAccountData.postValue(VmState.Loading)
                val checkLNDHub = lightningRepository.checkLNDHub(hub)
                if (checkLNDHub) {
                    infoList = lightningRepository.getInfoFromLNDHub(hub)
                } else {
                    mAccountData.postValue(VmState.Error(ApiException("import error")))
                    return@launch
                }
                infoList
            }.onSuccess {
                if (it.isNotEmpty() && it.size == 3)
                    getLNToken(it[0], it[1], it[2], walletName)
            }.onFailure {
                mAccountData.postValue(VmState.Error(ApiException("import error")))
            }
        }
    }

    //创建钱包第二步获取token 即导入钱包第一步
    private fun getLNToken(
        host: String,
        login: String,
        password: String,
        walletName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                //存在重复钱包
                if (WalletDbUtil.checkSameLNWallet(login, password, host)) {
                    mAccountData.postValue(VmState.Error(ApiException(getString(R.string.same_wallet_tip))))
                    return@launch
                }
                val accessToken = lightningRepository.getAccessToken(host, login, password)
                accessToken
            }.onSuccess {
                if (!TextUtils.isEmpty(it.accessToken) && !TextUtils.isEmpty(it.refreshToken))
                    getAddressAndInsertWallet(
                        host,
                        login,
                        password,
                        it.accessToken!!,
                        it.refreshToken!!,
                        walletName
                    )
                else
                    mAccountData.postValue(VmState.Error(ApiException(getString(R.string.import_error))))
            }.onFailure {
                mAccountData.postValue(VmState.Error(ApiException(getString(R.string.import_error))))
            }
        }
    }

    //获取address后完成创建或导入操作
    private fun getAddressAndInsertWallet(
        host: String,
        login: String,
        password: String,
        accessToken: String,
        refreshToken: String,
        walletName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val walletBean: WalletBean?
                val address = lightningRepository.getBTCAddress(host, appendAuth(accessToken))
                walletBean = lightningRepository.addLightningWallet(
                    host,
                    login,
                    password,
                    accessToken,
                    refreshToken,
                    //没有地址存空
                    if (address.array?.isNotEmpty() == true && !TextUtils.isEmpty(address.array[0].address))
                        address.array[0].address!!
                    else
                        "",
                    walletName
                )
                val aLong = WalletDaoUtils.insertNewWallet(walletBean)
                walletBean.id = aLong
                walletBean
            }.onSuccess {
                mAccountData.postValue(VmState.Success(it))
            }.onFailure {
                mAccountData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        LogUtils.e("exceptionHandler:${throwable}")
        mRecordListData.postValue(VmState.Error(ApiException(throwable)))
    }

    //获取闪电网络交易记录 时间倒序排序
    fun getTransactionRecord(id: Long, host: String, authorization: String) {
        viewModelScope.launch(exceptionHandler) {
            mRecordListData.postValue(VmState.Loading)
            runCatching {
                withContext(Dispatchers.IO) {
                    //先取缓存
                    val recordCacheList = CacheListManager.instance?.getLnListCacheById(id)
                    if (recordCacheList?.isNotEmpty() == true)
                        mRecordListData.postValue(VmState.Success(recordCacheList))
                    val recordList = mutableListOf<TxEntity>()
                    val asyncInvoice = async {
                        lightningRepository.getInvoiceList(host, authorization)
                    }
                    val asyncTX = async {
                        lightningRepository.getTxList(host, authorization)
                    }
                    val invoiceListEntity = asyncInvoice.await()
                    //转换invoice列表
                    val mapInvoice = invoiceListEntity.array?.map {
                        TxEntity().apply {
                            this.value = it.amt?.toLong()
                            this.isPaid = it.ispaid ?: false
                            this.isOutDate =
                                it.timestamp?.plus(it.expireTime!!)!! < (System.currentTimeMillis() / 1000)
                            this.memo = it.description
                            this.payReq = it.payReq
                            this.type = when {
                                this.isPaid == true -> StringConstants.USER_INVOICE_PAID
                                this.isOutDate == true -> StringConstants.USER_INVOICE_OUT
                                else -> StringConstants.USER_INVOICE_WAIT
                            }
                            this.time = it.timestamp.toLong()
                            this.expireTime = it.expireTime
                        }
                    }
                    val txListEntity = asyncTX.await()
                    val txList = txListEntity.array?.map {
                        if (it.time == null)
                            it.time = if (it.timestamp is String) {
                                it.timestamp.toLong()
                            } else {
                                if (it.timestamp is Double)
                                    BigDecimal(it.timestamp).toLong()
                                else
                                    it.timestamp as Long
                            }
                        if (it.value == null)
                            it.value = NumberCountUtils.transformSatoshi(it.amount.toPlainString())
                                .toLong()
                        it
                    }?.toMutableList()
                    recordList.addAll(mapInvoice!!)
                    recordList.addAll(txList!!)
                    if (recordList.isNotEmpty()) {
                        val sortedByDescending = recordList.sortedByDescending { it.time }
                        sortedByDescending
                    } else {
                        recordList
                    }
                }
            }.onSuccess {
                //存缓存
                CacheListManager.instance?.saveLnListCacheByType(it, id)
                mRecordListData.postValue(VmState.Success(it.toMutableList()))
            }.onFailure {
                mRecordListData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //闪电钱包更新钱包密码
    fun setWalletPwd(id: Long, pwd: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDbUtil.updateWalletPwd(id, pwd)
            }.onSuccess {
                mPwdResultData.postValue(VmState.Success(true))
            }.onFailure {
                mPwdResultData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //闪电钱包更新钱包名称
    fun setWalletName(id: Long, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDbUtil.updateWalletName(id, name)
            }.onSuccess {
                mNameResultData.postValue(VmState.Success(it))
            }.onFailure {
                mNameResultData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //闪电钱包关闭指纹支付方
    fun closeWalletTouchId(id: Long, isOpen: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDbUtil.updateWalletTouchPay(id, isOpen)
            }.onSuccess {
                mTouchIdResultData.postValue(VmState.Success(true))
            }.onFailure {
                mTouchIdResultData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //获取导出内容
    fun getExportContent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WalletDbUtil.getExportContent(id)
            }.onSuccess {
                mExportResultData.postValue(VmState.Success(it))
            }.onFailure {
                mExportResultData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //获取导出内容
    fun getTransferWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            mWalletListData.postValue(VmState.Loading)
            runCatching {
                WalletDbUtil.getTransferWallet()
            }.onSuccess {
                mWalletListData.postValue(VmState.Success(it))
            }.onFailure {
                mWalletListData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //decode  invoice 本地方法
    fun decodeInvoiceLocal(invoice: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mDecodeLocalData.postValue(VmState.Loading)
                // hex
                val jsData = "javascript:decodeInvoice('${invoice}')"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mDecodeLocalData.postValue(VmState.Success(it))
            }.onFailure {
                mDecodeLocalData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }
}