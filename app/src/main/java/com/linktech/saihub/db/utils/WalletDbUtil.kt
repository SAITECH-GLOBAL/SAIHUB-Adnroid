package com.linktech.saihub.db.utils

import android.text.TextUtils
import com.linktech.saihub.app.Constants
import com.linktech.saihub.greendao.WalletBeanDao
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.DbManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.util.LogUtils
import org.json.JSONObject

class WalletDbUtil {

    companion object {

        /**
         * 判断助记词是否存在
         *
         * @return
         */
        fun checkWalletMnemonic(mnemonic: String): Boolean {
            val walletBeans = WalletDaoUtils.walletDao.loadAll()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return false
            }
            val mnemonicList = walletBeans.map {
                AES.decrypt(it.mnemonic)
            }.filter {
                !TextUtils.isEmpty(it)
            }
            return mnemonicList.contains(mnemonic)
        }

        /**
         * 判断是否存在钱包私钥
         *
         * @return
         */
        fun checkWalletPrivateKey(privateKey: String): Boolean {
            val walletBeans = WalletDaoUtils.walletDao.loadAll()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return false
            }
            val privateKeyList = walletBeans.map {
                AES.decrypt(it.privateKey)
            }.filter {
                !TextUtils.isEmpty(it)
            }
            return privateKeyList.contains(privateKey)
        }

        /**
         * 判断是否存在钱包公钥
         *
         * @return
         */
        fun checkWalletPublicKey(publicKey: String): Boolean {
            val walletBeans = WalletDaoUtils.walletDao.loadAll()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return false
            }
            val publicKeyList = walletBeans.map {
                it.publicKey
            }.filter {
                !TextUtils.isEmpty(it)
            }
            return publicKeyList.contains(publicKey)
        }

        /**
         * 判断是否存在钱包主地址
         *
         * @return
         */
        fun checkWalletAddress(address: String): Boolean {
            val walletBeans = WalletDaoUtils.walletDao.loadAll()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return false
            }
            val addressList = walletBeans.map {
                it.address
            }.filter {
                !TextUtils.isEmpty(it)
            }
            return addressList.contains(address)
        }

        /**
         * 获取当前钱包余额信息
         *
         * @return
         */
        fun getWalletBalance(id: Long, kind: RateAndLocalManager.RateKind): String {
            val walletBeans =
                WalletDaoUtils.walletDao.queryBuilder().where(WalletBeanDao.Properties.Id.eq(id))
                    .list()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return "0.00"
            }
            val walletBean = walletBeans[0]
            return when (kind) {
                RateAndLocalManager.RateKind.CNY -> walletBean.asset
                RateAndLocalManager.RateKind.USD -> walletBean.assetUSD
                RateAndLocalManager.RateKind.RUB -> walletBean.assetRub
            }
        }


        /**
         * 获得所有钱包订阅数据
         */
        fun loadAllWalletSubAddressJSON(): String {
            val walletAll = DbManager.getInstance().daoSession.walletBeanDao?.loadAll()
                ?: return ""

            val jsonObject = JSONObject()
            jsonObject.put("ws_type", "transferList")
            jsonObject.put("sub", "1")
            jsonObject.put("coin", "BTC")
            val addressBuilder = StringBuilder()

            for ((position, item) in walletAll.withIndex()) {
                if (item.existType != Constants.EXIST_ADDRESS) {
                    val childAddressForTypeToServer =
                        ChildAddressDaoUtil.getChildAddressForTypeToServer(walletAll[position])
                    addressBuilder.append(if (position == 0) "" else ",")
                        .append(childAddressForTypeToServer)
                } else {
                    addressBuilder.append(if (position == 0) "" else ",")
                        .append(item.address)
                }
                LogUtils.i("address------addressBuilder.toString()")
            }
            jsonObject.put("address", addressBuilder.toString())

            return jsonObject.toString()
        }
    }

}