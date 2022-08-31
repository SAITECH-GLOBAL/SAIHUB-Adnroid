package com.linktech.saihub.mvvm.repository

import com.linktech.saihub.R
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.SaiHubApplication
import com.linktech.saihub.app.SaihubNetUrl
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.lightning.*
import com.linktech.saihub.mvvm.service.LightningService
import com.linktech.saihub.net.helper.RetrofitHelper2
import com.linktech.saihub.util.ToastUtils


class LightningRepository() : BaseRepository() {

    suspend fun createAccount(host: String): AccountEntity {
        val param = hashMapOf<String, Any>()
        param["partnerid"] = "saihub"
        param["accounttype"] = "common"
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .createAccount(host, param)
    }

    suspend fun getAccessToken(host: String, login: String, password: String): AccessTokenEntity {
        val param = hashMapOf<String, Any>()
        param["login"] = login
        param["password"] = password
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getAccessToken(host, param)
    }

    suspend fun refreshToken(host: String, refreshToken: String): AccessTokenEntity {
        val param = hashMapOf<String, Any>()
        param["refresh_token"] = refreshToken
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .refreshToken(host, param)
    }

    suspend fun getBTCAddress(host: String, authorization: String): BTCAddressEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getBTCAddress(host, authorization)
    }

    suspend fun addInvoice(
        host: String,
        authorization: String,
        amt: String,
        memo: String
    ): InvoiceEntity {
        val param = hashMapOf<String, Any>()
        param["amt"] = amt
        param["memo"] = memo
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .addInvoice(host, authorization, param)
    }

    suspend fun getInvoiceList(host: String, authorization: String): InvoiceListEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getInvoiceList(host, authorization, 100)
    }

    suspend fun getTxList(host: String, authorization: String): TxListEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getTxsList(host, authorization, 100)
    }

    suspend fun getBalance(host: String, authorization: String): LightningBalanceEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getBalance(host, authorization)
    }

    suspend fun decodeInvoice(
        host: String,
        authorization: String,
        invoice: String
    ): DecodeInvoiceEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .decodeInvoice(host, authorization, invoice)
    }

    suspend fun payInvoice(
        host: String,
        authorization: String,
        invoice: String,
        amount: String
    ): Any {
        val param = hashMapOf<String, Any>()
        param["invoice"] = invoice
        param["amount"] = amount
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .payInvoice(host, authorization, param)
    }

    suspend fun payInvoiceUrlType(
        host: String,
        authorization: String,
        invoice: String,
        amount: String
    ): Any {
        val param = hashMapOf<String, Any>()
        param["invoice"] = invoice
        param["amount"] = amount
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .payInvoiceUrlType(host, authorization, param)
    }

    suspend fun parseKeySend(
        host: String,
        authorization: String,
        user: String
    ): KeySendEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .parseKeySend(host, authorization, user)
    }

    suspend fun getKeySendCallback(
        url: String,
        authorization: String,
        amount: Long,
        nonce: Long,
        comment: String
    ): KeySendCallbackEntity {
        return RetrofitHelper2
            .getService(SaihubNetUrl.BASE_URL, LightningService::class.java)
            .getKeySendCallback(url, authorization, amount, nonce, comment)
    }

    fun addLightningWallet(
        host: String,
        login: String,
        password: String,
        accessToken: String,
        refreshToken: String,
        address: String,
        walletName: String
    ): WalletBean {
        val walletBean = WalletBean().apply {
            this.existType = Constants.EXIST_LIGHTNING
            this.isOpenTouchIdPay = false
            this.host = host
            this.login = login
            this.lnPassword = password
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            this.address = address
            this.addressToServer = address
            this.asset = "0"
            this.assetRub = "0"
            this.assetUSD = "0"
            this.isObserver = false
            this.isLNOpenPwdPay = false
            this.isLNOpenPwdPay = false
            this.createTime = System.currentTimeMillis()
            this.name = walletName
        }
        return walletBean
    }

    fun checkLNDHub(hub: String): Boolean {
        runCatching {
            if (hub.startsWith("lndhub://") && hub.contains("@")) {
                val split1 = hub.substring(9).split("@")
                val split2 = split1[0].split(":")
                return split2.size == 2 && split1.size == 2
            } else {
                return false
            }
        }.onFailure {
            return false
        }
        return true
    }

    fun getInfoFromLNDHub(hub: String): List<String> {
        //host login password
        val data = mutableListOf<String>()
        runCatching {
            val split1 = hub.substring(9).split("@")
            val split2 = split1[0].split(":")
            data.add(
                if (split1[1].endsWith("/"))
                    split1[1]
                else
                    "${split1[1]}/"
            )
            data.add(split2[0])
            data.add(split2[1])
            data
        }.onSuccess {
            return data
        }.onFailure {
            SaiHubApplication.getInstance()?.getString(R.string.import_error)
                ?.let { it1 -> ToastUtils.shortToast(it1) }
        }
        return data
    }

}