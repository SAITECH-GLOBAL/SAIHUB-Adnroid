package com.linktech.saihub.mvvm.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.SaihubNetUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.wallet.bean.*
import com.linktech.saihub.entity.wallet.bean.transaction.TransferVerifyBean
import com.linktech.saihub.manager.wallet.btc.FeeUtil
import com.linktech.saihub.mvvm.service.WalletService
import com.linktech.saihub.net.BaseResp
import com.linktech.saihub.net.helper.RetrofitHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class WalletRepository() : BaseRepository() {

    //上报地址
    suspend fun sendAddressToServer(addressList: List<String>, type: Int): BaseResp<Boolean> {
        val list = addressList.map {
            AddEntity().apply {
                this.address = it
                this.coin = StringConstants.BTC
                this.type = type
            }
        }
        val toJson = Gson().toJson(list)
        val param = hashMapOf<String, Any>()
        param["json"] = toJson
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .sendAddressToServer(param)
    }

    //获取BTC及USDT汇率
    suspend fun getRate(): BaseResp<RateEntity> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getRate()
    }

    //获取BTC余额
    suspend fun getBTCBalance(address: String): BaseResp<JsonObject> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getBTCBalance(address)
    }

    //获取OMNI USDT余额
    suspend fun getOMNIBalance(address: String): BaseResp<OMNIBalanceEntity> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getOMNIBalance(address, 31)
    }

    //获取BTC\OMNI USDT转账记录
    suspend fun getTransactionRecord(
        address: String,
        tokenShort: String
    ): BaseResp<TransactionRecordEntity> {
        return if (tokenShort == StringConstants.BTC) RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getBTCRecord(address, 1, 30)
        else RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getOMNIRecord(address, 31, 1, 30)
    }

    //获取gas price
    suspend fun getGasPrice(): BaseResp<GasEntity> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getGasPrice()
    }

    //批量获取UTXO
    suspend fun getUTXO(address: String): BaseResp<List<UTXOEntity>> {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getUTXO(address)
    }

    //批量获取UTXO BlockChain
    suspend fun getUTXOFromBlockChain(address: String): UTXODataBean {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getUTXOFromBlockChain(address)
    }

    //构建交易认证对象
    fun buildTransferSendBean(
        address: String,
        amount: String,
        tokenInfoBean: TokenInfoBean?,
        walletBean: WalletBean?,
        gasPrice: Long?,
        unspentOutputs: List<UnspentOutput>?
    ): TransferVerifyBean {
        return TransferVerifyBean().apply {
            this.moneyNumberStr = amount
            this.toAddressStr = address
            this.tokenName = tokenInfoBean?.tokenShort
            this.gasPriceIntForSeek = gasPrice?.toDouble()!!
            this.gasLimitOrBytes =
                FeeUtil.getFeeSegWit(amount, unspentOutputs, tokenInfoBean?.tokenShort)
            this.walletBean = walletBean
            this.balanceDouble = tokenInfoBean?.tokenBalance?.toDouble()!!
            this.token_places = tokenInfoBean.places
            if (unspentOutputs != null) {
                this.utxoList = unspentOutputs
            }
        }
    }

    //发送广播-BTC
    suspend fun sendTransaction(hash: String): BaseResp<String> {
        val param = hashMapOf<String, Any>()
        param["transaction"] = hash
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .sendTransaction(param)
    }

    //获取UTXO tx_hex
    suspend fun getUTXOHex(hash: String): String {
        return RetrofitHelper
            .getService(SaihubNetUrl.BASE_URL, WalletService::class.java)
            .getUTXOHex(hash)
    }

}