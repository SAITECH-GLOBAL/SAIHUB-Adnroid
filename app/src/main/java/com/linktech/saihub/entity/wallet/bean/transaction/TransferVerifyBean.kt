package com.linktech.saihub.entity.wallet.bean.transaction

import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.wallet.bean.UnspentOutput
import com.linktech.saihub.entity.wallet.bean.transaction.TransferSendBean
import com.linktech.saihub.manager.CacheListManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.util.NumberCountUtils

/**
 * 验证交易的bean
 */
class TransferVerifyBean {
    var selectGasType = 0
    var gasPriceEtInput //gasPrice
            : String? = null
    var gasMaxFeePerEtInput //maxFeePerGas
            : String? = null
    var gasMaxPriorityFeePerEtInput //maxPriorityFeePerGas
            : String? = null
    var gasLimitEtInput //gasLimit
            : String? = null
    var moneyNumberStr //数量
            : String? = null
    var toAddressStr //交易地址
            : String? = null
    var contractaddress //代币合约地址
            : String? = null
    var balanceDouble //余额
            = 0.0
    var checked //是否开启高级设置
            = false
    var maxPriorityFeePerGas //maxPriorityFeePerGas
            = 0.0
    var maxFeePerGas //maxFeePerGas
            = 0.0
    var gasPriceIntForSeek //gasPriceIntForSeek
            = 0.0
    var tokenName //当前的主币种
            : String? = null
    var walletBean //钱包数据
            : WalletBean? = null
    var token_places //代币小数位
            = 0
    var gasLimitOrBytes //utxo有value的数量
            : Long = 0
    var remarkStr //trx 备注
            : String? = null
    var tokenType //tokenType
            = 0
    var utxoList: List<UnspentOutput> = mutableListOf()


    fun getTransferSendBean(feePair: Pair<String?, String?>?): TransferSendBean {
        val rateEntity = CacheListManager.instance?.rateEntity
        val transferBean = TransferSendBean()
        transferBean.moneyNumber = moneyNumberStr
        transferBean.tokenName = tokenName
        transferBean.toAddress = toAddressStr
        transferBean.utxoList = utxoList
        transferBean.address = walletBean!!.getAddress()
        transferBean.contractAddress = contractaddress
        transferBean.convert =
            when (RateAndLocalManager.getInstance(SaiHubApplication.getInstance()).curRateKind) {
                RateAndLocalManager.RateKind.CNY -> "${RateAndLocalManager.RateKind.CNY.symbol}${
                    NumberCountUtils.getConvert(moneyNumberStr,
                        if (tokenName == StringConstants.BTC) rateEntity?.btcCny else rateEntity?.usdtCny)
                }"
                RateAndLocalManager.RateKind.USD -> "${RateAndLocalManager.RateKind.USD.symbol}${
                    NumberCountUtils.getConvert(moneyNumberStr,
                        if (tokenName == StringConstants.BTC) rateEntity?.btcUsd else rateEntity?.usdtUsd)
                }"
                RateAndLocalManager.RateKind.RUB -> "${RateAndLocalManager.RateKind.RUB.symbol}${
                    NumberCountUtils.getConvert(moneyNumberStr,
                        if (tokenName == StringConstants.BTC) rateEntity?.btcRub else rateEntity?.usdtRub)
                }"
            }
        if (feePair != null) {
            transferBean.gasLimit = feePair.second
            transferBean.gasPrice = feePair.first
            transferBean.gas = NumberCountUtils.getConvert(transferBean.gasLimit,
                transferBean.gasPrice, 8, 8)
            transferBean.gasConvert =
                when (RateAndLocalManager.getInstance(SaiHubApplication.getInstance()).curRateKind) {
                    RateAndLocalManager.RateKind.CNY -> "${RateAndLocalManager.RateKind.CNY.symbol}${
                        NumberCountUtils.getConvert(transferBean.gas, rateEntity?.btcCny)
                    }"
                    RateAndLocalManager.RateKind.USD -> "${RateAndLocalManager.RateKind.USD.symbol}${
                        NumberCountUtils.getConvert(transferBean.gas, rateEntity?.btcUsd)
                    }"
                    RateAndLocalManager.RateKind.RUB -> "${RateAndLocalManager.RateKind.RUB.symbol}${
                        NumberCountUtils.getConvert(transferBean.gas, rateEntity?.btcRub)
                    }"
                }
        }
        return transferBean
    }
}