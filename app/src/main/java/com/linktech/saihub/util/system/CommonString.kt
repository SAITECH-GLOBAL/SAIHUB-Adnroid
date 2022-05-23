package com.linktech.saihub.util.system

import com.google.gson.Gson
import com.linktech.saihub.app.Constants
import com.linktech.saihub.entity.wallet.ExtPubKeyEntity
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc
import com.linktech.saihub.util.CommonUtil
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation

/**
 * Created by tromo on 2021/9/22.
 */

//生成随机四位英文字母
fun getRandomName(): String {
    val allowedChars = "abcdefghijklmnopqrstuvwxyz"
    return (1..4).map { allowedChars.random() }.joinToString("")
}

//输入内容是否为合法xpub、ypub或zpub
fun checkPublicKey(content: String): Boolean {
    runCatching {
        if (/*content.startsWith("xpub",true) ||*/ content.startsWith("ypub") || content.startsWith(
                "zpub")
        ) {
            var publicKey = content
            if (content.startsWith("ypub") || content.startsWith("Zpub") || content.startsWith("Ypub")) {
                publicKey = CommonUtil.ypubToXpub(CommonUtil.ypubToXpub(content))
            }
            val params: NetworkParameters = ParamsManagerBtc.getParams()
            val parentDK = DeterministicKey.deserializeB58(publicKey, params)
            val changeKey = HDKeyDerivation.deriveChildKey(parentDK, 0)
            val childDK = HDKeyDerivation.deriveChildKey(changeKey, 0)
        } else if (content.contains("ExtPubKey")) {
            val extPubKeyEntity =
                Gson().fromJson(content, ExtPubKeyEntity::class.java)
            return checkPublicKey(extPubKeyEntity.extPubKey!!)
        } else {
            return false
        }
    }.onSuccess {
        return true
    }.onFailure {
        return false
    }
    return true

}

fun getTypeForAddress(address: String): Int {
    return if (address.startsWith("3")) {
        Constants.CHILD_ADDRESS_NESTED
    } else {
        Constants.CHILD_ADDRESS_NATIVE
    }
}

fun getTypeForPub(address: String): Int {
    return if (address.startsWith("ypub")) {
        Constants.CHILD_ADDRESS_NESTED
    } else {
        Constants.CHILD_ADDRESS_NATIVE
    }
}

fun getBitcoinUrl(content: String?, isHash: Boolean): String {
    return if (isHash)
        Constants.BITCOIN_BROWSER_URL + "transaction/" + content
    else
        Constants.BITCOIN_BROWSER_URL + "address/" + content
}

fun getAgreementUrl(localKind: RateAndLocalManager.LocalKind): String {
    return when (localKind) {
        RateAndLocalManager.LocalKind.zh_HK -> "https://app.sai.tech/agreement/agreement_zh_HK.html"
        RateAndLocalManager.LocalKind.zh_CN -> "https://app.sai.tech/agreement/agreement_zh.html"
        RateAndLocalManager.LocalKind.en_US -> "https://app.sai.tech/agreement/agreement_en.html"
        RateAndLocalManager.LocalKind.ru_RU -> "https://app.sai.tech/agreement/agreement_ru.html"
    }
}

fun getTermServiceUrl(localKind: RateAndLocalManager.LocalKind): String {
    return when (localKind) {
        RateAndLocalManager.LocalKind.zh_HK -> "https://app.sai.tech/service/terms_zh_HK.html"
        RateAndLocalManager.LocalKind.zh_CN -> "https://app.sai.tech/service/terms_zh.html"
        RateAndLocalManager.LocalKind.en_US -> "https://app.sai.tech/service/terms_en.html"
        RateAndLocalManager.LocalKind.ru_RU -> "https://app.sai.tech/service/terms_ru.html"
    }
}

fun getColdWalletUrl(localKind: RateAndLocalManager.LocalKind): String {
    return when (localKind) {
        RateAndLocalManager.LocalKind.zh_HK -> "https://support.keyst.one/v/chinese/"
        RateAndLocalManager.LocalKind.zh_CN -> "https://support.keyst.one/v/chinese/"
        RateAndLocalManager.LocalKind.en_US -> "https://support.keyst.one/"
        RateAndLocalManager.LocalKind.ru_RU -> "https://support.keyst.one/"
    }
}

fun formatJsReturn(content: String): String {
    return content.substring(1, content.length - 1).replace("\\", "")
}

fun checkExt(extPub: String): Boolean {
    return extPub.startsWith("{")
}


