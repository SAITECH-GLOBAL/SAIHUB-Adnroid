package com.linktech.saihub.mvvm.model

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.linktech.saihub.R
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.entity.databinding.FeeEntity
import com.linktech.saihub.entity.wallet.bean.GasEntity
import com.linktech.saihub.entity.wallet.bean.TransactionRecordEntity
import com.linktech.saihub.entity.wallet.bean.UTXOEntity
import com.linktech.saihub.entity.wallet.bean.UnspentOutput
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.entity.wallet.bean.transaction.TransferVerifyBean
import com.linktech.saihub.entity.wallet.bean.transaction.UTXOMultiSigEntity
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.manager.wallet.ManagerBTCWallet
import com.linktech.saihub.manager.wallet.transfer.BitcoinTransaction
import com.linktech.saihub.mvvm.base.BaseViewModel
import com.linktech.saihub.mvvm.base.VmLiveData
import com.linktech.saihub.mvvm.base.VmState
import com.linktech.saihub.mvvm.base.launchVmRequest
import com.linktech.saihub.mvvm.repository.WalletRepository
import com.linktech.saihub.net.ex.ApiException
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.Pub
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.getTypeForAddress
import kotlinx.coroutines.*
import java.math.BigDecimal


class WalletTransactionViewModel : BaseViewModel() {

    private val walletRepository by lazy { WalletRepository() }

    val mTokenData by lazy { VmLiveData<TokenInfoBean>() }

    val mRecordData by lazy { VmLiveData<TransactionRecordEntity>() }

    val mGasPriceData by lazy { VmLiveData<GasEntity>() }

    val mGasListData by lazy { VmLiveData<List<FeeEntity>>() }

    val mUTXOData by lazy { VmLiveData<List<UTXOEntity>>() }

    val mSignHashData by lazy { VmLiveData<String>() }

    val mJsData by lazy { VmLiveData<String>() }

    val mTxHashData by lazy { VmLiveData<String>() }

    val mSubmitData by lazy { VmLiveData<TransferSendBean>() }

    val mMultiSigUrCodeData by lazy { VmLiveData<String>() }

    val mSingleSigUrCodeData by lazy { VmLiveData<String>() }

    val mMultiSigNumData by lazy { VmLiveData<String>() }

    val mMultiSigHexData by lazy { VmLiveData<String>() }

    val mSameTransactionJsData by lazy { VmLiveData<String>() }

    val mFeeJsData by lazy { VmLiveData<Pair<String, TransferSendBean>>() }

    //获取钱包余额信息
    fun getSingleBalanceData(id: Long, isBTC: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                TokenDaoUtil.loadTokenDataForWalId(id, isBTC)
            }.onSuccess {
                if (it.isNotEmpty())
                    mTokenData.postValue(VmState.Success(it[0]))
            }.onFailure {
                mTokenData.postValue(VmState.Error(ApiException(it)))
            }
        }

    }

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        LogUtils.e("exceptionHandler:${throwable}")
        mGasListData.postValue(VmState.Error(ApiException(throwable)))
    }

    //获取gas price 及 UTXO
    fun getGasPrice(walletBean: WalletBean, tokenName: String) {
        viewModelScope.launch(exceptionHandler) {
            mGasListData.postValue(VmState.Loading)
            runCatching {
                withContext(Dispatchers.IO) {
                    val asyncGas = async {
                        walletRepository.getGasPrice()
                    }
                    val asyncUTXO = async {
//                    walletRepository.getUTXO(address)
                        val addressStr =
                            ChildAddressDaoUtil.getChildAddressStrTransferForType(
                                walletBean.childAddressType,
                                tokenName,
                                walletBean
                            )

                        walletRepository.getUTXOFromBlockChain(addressStr)
                    }
                    Pair(asyncGas.await().data?.result, asyncUTXO.await().unspentOutputs)
                }
            }.onSuccess {
                getGasLimit(it.first, it.second)
            }.onFailure {
                mGasListData.postValue(VmState.Error(ApiException(it)))
            }
        }
    }

    //获取gas limit  bytes
    private fun getGasLimit(gasEntity: GasEntity?, utxoList: List<UnspentOutput>?) {
        runCatching {
            var moneyUTXONumber = 0
            if (utxoList != null) {
                for (outputsBean in utxoList) {
                    if (outputsBean.value!! > 0) {
                        moneyUTXONumber++
                    }
                }
            } else {
                moneyUTXONumber = 0
            }
//            bytes = 78 + 该地址类型所有UTXO数量 * 102
            val btcBytes: Long = (78 + moneyUTXONumber * 102).toLong()
            if (btcBytes == 0L) {
                return
            }
            val gasPriceList = mutableListOf(
                gasEntity?.fastGasPrice?.toLong(),
                gasEntity?.proposeGasPrice?.toLong(),
                gasEntity?.safeGasPrice?.toLong()
            )
            val typeList =
                mutableListOf(
                    getString(R.string.fast),
                    getString(R.string.avg),
                    getString(R.string.slow)
                )
            setFeeList(btcBytes, gasPriceList, typeList)
        }
    }

    //返回gas费用集合
    private fun setFeeList(
        btcBytes: Long,
        gasPriceList: MutableList<Long?>,
        typeList: MutableList<String?>
    ) {
        runCatching {
            val feeList = mutableListOf<FeeEntity>()
            val rateEntity = CacheListManager.instance?.rateEntity
            for ((index, item) in typeList.withIndex()) {
                feeList.add(FeeEntity().apply {

                    val numStr = NumberCountUtils.getNumberScaleByPow(
                        (btcBytes * gasPriceList[index]!!).toString(),
                        8,
                        8
                    )
                    this.gasPrice = gasPriceList[index]
                    this.num = "$numStr ${StringConstants.BTC}"

                    this.type = item
                    this.isSelect = index == 1
                    when (RateAndLocalManager.getInstance(SaiHubApplication.getInstance()).curRateKind) {
                        RateAndLocalManager.RateKind.CNY -> {
                            this.convert = NumberCountUtils.getConvert(numStr, rateEntity?.btcCny)
                            this.convertType = RateAndLocalManager.RateKind.CNY.symbol
                        }
                        RateAndLocalManager.RateKind.USD -> {
                            this.convert = NumberCountUtils.getConvert(numStr, rateEntity?.btcUsd)
                            this.convertType = RateAndLocalManager.RateKind.USD.symbol
                        }
                        RateAndLocalManager.RateKind.RUB -> {
                            this.convert = NumberCountUtils.getConvert(numStr, rateEntity?.btcRub)
                            this.convertType = RateAndLocalManager.RateKind.RUB.symbol
                        }
                    }

                })
            }
            feeList
        }.onSuccess {
            mGasListData.postValue(VmState.Success(it))
        }.onFailure {
            mGasListData.postValue(VmState.Error(ApiException(it)))
        }
    }

    //提交交易 step1：生成交易对象
    fun submitTransaction(
        address: String,
        amount: String,
        tokenBean: TokenInfoBean?,
        walletBean: WalletBean?,
        gasPrice: Long?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mSubmitData.postValue(VmState.Loading)
                //地址格式错误
                if (!ManagerBTCWallet.getInstance().validAddressTransfer(address)) {
                    mSubmitData.postValue(VmState.Error(ApiException(getString(R.string.addres_format_error))))
                    return@launch
                }
                //如果是omni转账不支持隔离见证原生
                if (tokenBean?.tokenShort == StringConstants.USDT && getTypeForAddress(address) == Constants.CHILD_ADDRESS_NATIVE) {
                    mSubmitData.postValue(VmState.Error(ApiException(getString(R.string.omni_transfer_tip))))
                    return@launch
                }
                //余额不足
                val addressStr = walletBean?.childAddressType?.let {
                    ChildAddressDaoUtil.getChildAddressStrTransferForType(
                        it,
                        tokenBean?.tokenShort,
                        walletBean
                    )
                }
                val utxoFromBlockChain =
                    addressStr?.let { walletRepository.getUTXOFromBlockChain(it) }
                if (utxoFromBlockChain?.unspentOutputs?.isEmpty() == true) {
                    mSubmitData.postValue(VmState.Error(ApiException(getString(R.string.balance_not_sufficient))))
                    return@launch
                }
                walletRepository.buildTransferSendBean(
                    address,
                    amount,
                    tokenBean,
                    walletBean,
                    gasPrice,
                    utxoFromBlockChain?.unspentOutputs
                )
            }.onSuccess {
                verifyTransferInput(it)
            }.onFailure {
                mSubmitData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //提交交易 step2：验证交易对象合法性
    private fun verifyTransferInput(transferVerifyBean: TransferVerifyBean) {
        runCatching {
            if (transferVerifyBean.balanceDouble <= 0) {
                getString(R.string.balance_not_sufficient)?.let {
                    mSubmitData.postValue(VmState.Error(ApiException((it))))
                }
                return
            }
            if (Pub.GetDouble(transferVerifyBean.moneyNumberStr) > transferVerifyBean.balanceDouble) {
                getString(R.string.balance_not_sufficient)?.let {
                    mSubmitData.postValue(VmState.Error(ApiException((it))))
                }
                return
            }
            /* if (Pub.GetDouble(transferVerifyBean.moneyNumberStr) < Constants.BTC_MIN_NUMBER) {
                 getString(R.string.btc_min_number)?.let {
                     mSubmitData.postValue(VmState.Error(ApiException((it))))
                 }
 //            ToastUtils.shortToast(getString(R.string.btc_min_number))
                 return
             }*/
            var feePair: Pair<String?, String?>? = null
            //验证BTC的fee
            feePair = checkBTCGasGasLimit(transferVerifyBean)
            if (feePair == null) {
                return
            }
            val feeNumber: Double = getFeeNumber(feePair)
            //BTC OMNI 没有合约地址
            if (TextUtils.isEmpty(transferVerifyBean.contractaddress) && !transferVerifyBean.tokenName.equals(
                    StringConstants.USDT
                )
            ) {
                //BTC判断余额是否充足 调用本地js(多签、单签、bc1)，走js判断
                if (transferVerifyBean.walletBean?.existType == Constants.EXIST_MULTI_SIG
                    || transferVerifyBean.walletBean?.existType == Constants.EXIST_PUBLIC_KEY
                    || transferVerifyBean.walletBean?.addressType == Constants.CHILD_ADDRESS_NATIVE
                ) {
                    calculateFee(transferVerifyBean.getTransferSendBean(feePair))
                    return
                } else {
                    val bigDecimal = BigDecimal(
                        Pub.sub(
                            transferVerifyBean.balanceDouble,
                            Pub.GetDouble(transferVerifyBean.moneyNumberStr)
                        )
                    )
                    val bigDecimal1 = BigDecimal(feeNumber)

                    if (bigDecimal < bigDecimal1) {
                        getString(R.string.balance_not_sufficient)?.let {
                            mSubmitData.postValue(VmState.Error(ApiException((it))))
                        }
                        return
                    }
                }

            } else {
                val addressUtxo = transferVerifyBean.utxoList.filter {
                    transferVerifyBean.walletBean?.addressType?.let { it1 -> it.getAddress(it1) } == transferVerifyBean.walletBean?.address
                }
                var balanceAddress = BigDecimal.ZERO
                for (item in addressUtxo) {
                    balanceAddress += BigDecimal(
                        NumberCountUtils.getNumberScaleByPow(
                            item.value.toString(),
                            8,
                            8
                        )
                    )
                }
                if (addressUtxo.isEmpty() || balanceAddress < BigDecimal(feeNumber)) {
                    getString(R.string.insufficient_btc_balance)?.let {
                        mSubmitData.postValue(VmState.Error(ApiException((it))))
                    }
                    return
                }
            }
            transferVerifyBean.getTransferSendBean(feePair)
        }.onSuccess {
            mSubmitData.postValue(VmState.Success(it))
        }.onFailure {
            mSubmitData.postValue(VmState.Error(ApiException(it.message)))
        }

    }

    //提交交易 step3：签名BTC交易
    fun signTransaction(transferBean: TransferSendBean, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val bitcoinTransaction = BitcoinTransaction()
                if (transferBean.tokenName == StringConstants.BTC) {
                    bitcoinTransaction.signSegWitTransaction(transferBean, walletBean)
                } else {
                    bitcoinTransaction.signUsdtSegWitTransaction(transferBean, walletBean)
                }
            }.onSuccess {
                LogUtils.e("transactionHash= \n  $it")
                mSignHashData.postValue(VmState.Success(it))
            }.onFailure {
                mSignHashData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //提交交易 step2.5：调用js 判断fee是否充足
    private fun calculateFee(transferBean: TransferSendBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mSingleSigUrCodeData.postValue(VmState.Loading)
                //utxos, address, value, feeRate
                //获取utxo hex
                transferBean.utxoHexList = transferBean.utxoList.map {
                    async {
                        UTXOMultiSigEntity().apply {
                            this.address = it.getAddress(Constants.CHILD_ADDRESS_NATIVE)
                            this.txid = it.txHashBigEndian
                            this.txId = it.txHashBigEndian
                            this.confirmations = it.confirmations
                            this.txhex = it.txHashBigEndian?.let { it1 ->
                                walletRepository.getUTXOHex(it1)
                            }
                            this.vout = it.txOutputN
                            this.wif = false
                            this.value = it.value
                        }
                    }
                }.awaitAll()
                val address = transferBean.toAddress
                val value = NumberCountUtils.getCong(transferBean.moneyNumber, 8)
                val feeRate = transferBean.gasPrice.toDouble()
                val jsData =
                    "javascript:calculateFee(${Gson().toJson(transferBean.utxoHexList)},'${address}',${value},${feeRate})"
                Pair(jsData, transferBean)
            }.onSuccess {
                LogUtils.e("transactionHash= \n  $it")
                mFeeJsData.postValue(VmState.Success(it))
            }.onFailure {
                mFeeJsData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //提交交易 step3：签名BTC交易  本地js
    fun signTransactionJs(transferBean: TransferSendBean, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mJsData.postValue(VmState.Loading)
                //mnemonic, utxos, address, value, changeAddress, feeRate, isBech32
                //获取utxo hex
                val mnemonic =
                    if (walletBean.existType == Constants.EXIST_MNEMONIC) AES.decrypt(walletBean.mnemonic)
                    else AES.decrypt(walletBean.privateKey)
                val passPhrase = AES.decrypt(walletBean.passphrase)
                val utxoHexList = transferBean.utxoHexList
                val address = transferBean.toAddress
                val value = NumberCountUtils.getCong(transferBean.moneyNumber, 8)
                val changeAddress = ChildAddressDaoUtil.getChangeAddress(walletBean)
                val feeRate = transferBean.gasPrice.toDouble()
                val isBech32 = true
                val jsData =
                    if (walletBean.existType == Constants.EXIST_MNEMONIC)
                        if (TextUtils.isEmpty(passPhrase))
                            "javascript:createHDTransaction('${mnemonic}',${
                                Gson().toJson(
                                    utxoHexList
                                )
                            },'${address}',${value},'${changeAddress}',${feeRate},${isBech32})"
                        else
                            "javascript:createHDTransaction('${mnemonic}',${
                                Gson().toJson(
                                    utxoHexList
                                )
                            },'${address}',${value},'${changeAddress}',${feeRate},${isBech32},'${passPhrase}')"
                    else
                        "javascript:createSingleTransaction('${mnemonic}',${
                            Gson().toJson(utxoHexList)
                        },'${address}',${value},'${changeAddress}',${feeRate},${isBech32})"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mJsData.postValue(VmState.Success(it))
            }.onFailure {
                mJsData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //提交交易 step3：签名BTC交易  本地js  多签交易
    fun signMultiSigJs(transferBean: TransferSendBean, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mMultiSigUrCodeData.postValue(VmState.Loading)
                //utxos, zpubs,isNative,requireM,address,value,fee,changeAddress
                //获取utxo hex
                val output = ManagerBTCWallet.getInstance()
                    .parseMultiSigInput(walletBean.multiSigData)

                val utxoHexList = transferBean.utxoHexList
                val requireM = (output[0] as? String)?.split(",")?.get(0)
                val address = transferBean.toAddress
                val value = NumberCountUtils.getCong(transferBean.moneyNumber, 8)
                val changeAddress = ChildAddressDaoUtil.getChangeAddress(walletBean)
                val feeRate = transferBean.gasPrice.toDouble()
                val isNative = walletBean.addressType == Constants.CHILD_ADDRESS_NATIVE
                val isAndroid = true
                val jsData =
                    "javascript:createMultisigTransaction(${Gson().toJson(utxoHexList)},${
                        Gson().toJson(output[1])
                    },${isNative},${requireM},'${address}',${value},${feeRate},'${changeAddress}',${isAndroid})"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mMultiSigUrCodeData.postValue(VmState.Success(it))
            }.onFailure {
                mMultiSigUrCodeData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //提交交易 step3：签名BTC交易  本地js  单签交易
    fun signSingleSigJs(transferBean: TransferSendBean, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mSingleSigUrCodeData.postValue(VmState.Loading)
                //utxos, zpub,address,value,fee,changeAddress,isAndroid
                //获取utxo hex
                val utxoHexList = transferBean.utxoHexList
                val zpub = walletBean.publicKeyExt
                val address = transferBean.toAddress
                val value = NumberCountUtils.getCong(transferBean.moneyNumber, 8)
                val changeAddress = ChildAddressDaoUtil.getChangeAddress(walletBean)
                val feeRate = transferBean.gasPrice.toDouble()
                val isAndroid = true
                val jsData =
                    "javascript:createWatchOnlyTransaction(${Gson().toJson(utxoHexList)},${zpub},'${address}',${value},${feeRate},'${changeAddress}',${isAndroid})"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mSingleSigUrCodeData.postValue(VmState.Success(it))
            }.onFailure {
                mSingleSigUrCodeData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //发送广播
    fun sendTransaction(hash: String) {
        launchVmRequest({ walletRepository.sendTransaction(hash) }, mTxHashData)
    }

    private fun getFeeNumber(feePair: Pair<String, String>): Double {
        val bigDecimalGad = NumberCountUtils.mulD(feePair.second, feePair.first)
        return if (bigDecimalGad == null) {
            0.0
        } else {
            val bigFree: BigDecimal =
                bigDecimalGad.divide(BigDecimal(10).pow(8), 8, BigDecimal.ROUND_DOWN)
                    .stripTrailingZeros()
            bigFree.toDouble()
        }
    }

    /**
     * 验证BTC gas 和Gaslimit
     *
     * @param transferVerifyBean
     * @return
     */
    private fun checkBTCGasGasLimit(transferVerifyBean: TransferVerifyBean): Pair<String, String> {
        //推荐类型
        val satbStr: String = java.lang.String.valueOf(transferVerifyBean.gasPriceIntForSeek)
        val bytesStr: String = java.lang.String.valueOf(transferVerifyBean.gasLimitOrBytes)
        return Pair(satbStr, bytesStr)
    }

    //获取多签交易有几个签名
    fun getTransactionSignedNum(psbt: String, walletBean: WalletBean) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mMultiSigNumData.postValue(VmState.Loading)
                //psbt, zpubs,isNative,requireM
                //获取utxo hex
                val output = ManagerBTCWallet.getInstance()
                    .parseMultiSigInput(walletBean.multiSigData)

                val requireM = (output[0] as? String)?.split(",")?.get(0)
                val isNative = walletBean.addressType == Constants.CHILD_ADDRESS_NATIVE
                val jsData =
                    "javascript:getTransactionSignedNum('${psbt}',${Gson().toJson(output[1])},${isNative},${requireM})"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mMultiSigNumData.postValue(VmState.Success(it))
            }.onFailure {
                mMultiSigNumData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //获取多签交易base64获取 签名hex
    fun psbtBase64ToHex(psbt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mMultiSigHexData.postValue(VmState.Loading)
                // hex
                val jsData = "javascript:psbtBase64ToHex('${psbt}')"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mMultiSigHexData.postValue(VmState.Success(it))
            }.onFailure {
                mMultiSigHexData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //获取单签交易base64获取 签名hex
    fun singlePsbtToHex(psbt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mMultiSigHexData.postValue(VmState.Loading)
                // hex
                val jsData = "javascript:singlePsbtToHex('${psbt}')"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mMultiSigHexData.postValue(VmState.Success(it))
            }.onFailure {
                mMultiSigHexData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }

    //判断两个psbt是否为同一笔交易
    fun isSameTransaction(psbt: String, psbtSource: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mSameTransactionJsData.postValue(VmState.Loading)
                val psbtArray = arrayOf(psbt, psbtSource)
                // hex
                val jsData = "javascript:isSameTransaction(${Gson().toJson(psbtArray)})"
                jsData
            }.onSuccess {
                LogUtils.e("mJsData= \n  $it")
                mSameTransactionJsData.postValue(VmState.Success(it))
            }.onFailure {
                mSameTransactionJsData.postValue(VmState.Error(ApiException(it.message)))
            }
        }
    }
}