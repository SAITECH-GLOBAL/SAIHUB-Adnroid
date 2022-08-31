package com.linktech.saihub.db.utils

import android.text.TextUtils
import com.linktech.saihub.app.Constants
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.lightning.AccessTokenEntity
import com.linktech.saihub.entity.wallet.bean.RateEntity
import com.linktech.saihub.greendao.WalletBeanDao
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.DbManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.system.checkExt
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

        /**
         * 修改钱包名称 （only ln）
         *
         * @param walletId
         * @param name
         */
        fun updateWalletName(walletId: Long, name: String?): WalletBean {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            if (wallet.existType == Constants.EXIST_LIGHTNING) {
                wallet.name = name
                WalletDaoUtils.walletDao.update(wallet)
            }
            return wallet
        }

        /**
         * 更新闪电钱包token信息
         *
         * @param walletId
         * @param accessToken
         * @param refreshToken
         */
        fun updateLNToken(walletId: Long, accessToken: String?, refreshToken: String?) {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            if (wallet.existType == Constants.EXIST_LIGHTNING) {
                wallet.accessToken = accessToken
                wallet.refreshToken = refreshToken
                WalletDaoUtils.walletDao.update(wallet)
            }
        }

        /**
         * 更新闪电钱包address信息 不确定address会不会发生改变
         *
         * @param walletId
         * @param address
         */
        fun updateLNAddress(walletId: Long, address: String?) {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            if (wallet.existType == Constants.EXIST_LIGHTNING) {
                wallet.setAddress(address)
                WalletDaoUtils.walletDao.update(wallet)
            }
        }

        /**
         * 修改钱包主地址
         *
         * @param walletId
         * @param address
         */
        fun updateWalletAddress(walletId: Long, address: String?) {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            wallet.setAddress(address)
            WalletDaoUtils.walletDao.update(wallet)
        }

        /**
         * 更新闪电钱余额及法币折合
         * @param walletId
         * @param rateEntity
         */
        fun updateLNConvert(walletId: Long, balance: Long?, rateEntity: RateEntity) {
            val list = WalletDaoUtils.walletDao.queryBuilder()
                .where(WalletBeanDao.Properties.Id.eq(walletId)).list()
            val walletBean = list[0]
            walletBean.lnBalance = NumberCountUtils.transformBTC(balance.toString())
            walletBean.lnSat = balance.toString()
            val currency = NumberCountUtils.getLNConvert(walletBean.lnSat, rateEntity.btcCny)
            val currencyUsd = NumberCountUtils.getLNConvert(walletBean.lnSat, rateEntity.btcUsd)
            val currencyRub = NumberCountUtils.getLNConvert(walletBean.lnSat, rateEntity.btcRub)
            walletBean.asset = currency
            walletBean.assetRub = currencyRub
            walletBean.assetUSD = currencyUsd
            WalletDaoUtils.walletDao.update(walletBean)
        }

        /**
         * 更新闪电钱包密码
         *
         * @param walletId
         * @param pwd
         */
        fun updateWalletPwd(walletId: Long, pwd: String?) {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            if (wallet.existType == Constants.EXIST_LIGHTNING) {
                wallet.password = AES.encrypt(pwd)
                wallet.isLNOpenPwdPay = true
                WalletDaoUtils.walletDao.update(wallet)
            }
        }

        fun updateWalletTouchPay(walletId: Long, isOpen: Boolean) {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            if (wallet.existType == Constants.EXIST_LIGHTNING) {
                wallet.isOpenTouchIdPay = isOpen
                WalletDaoUtils.walletDao.update(wallet)
            }
        }

        fun getExportContent(walletId: Long): String {
            val wallet = WalletDaoUtils.walletDao.load(walletId)
            return "lndhub://${wallet.login}:${wallet.lnPassword}@${
                wallet.host.substring(
                    0,
                    wallet.host.length - 1
                )
            }"
        }

        //获取可转账本地钱包
        fun getTransferWallet(): List<WalletBean> {
            val list = WalletDaoUtils.walletDao.queryBuilder().where(
                WalletBeanDao.Properties.ExistType.notEq(
                    Constants.EXIST_LIGHTNING
                ), WalletBeanDao.Properties.ExistType.notEq(
                    Constants.EXIST_ADDRESS
                ), WalletBeanDao.Properties.ExistType.notEq(
                    Constants.EXIST_PUBLIC_KEY
                )
            )
                .orderDesc(WalletBeanDao.Properties.CreateTime).list()
            val publicKeyWalletList = WalletDaoUtils.walletDao.queryBuilder().where(
                WalletBeanDao.Properties.ExistType.eq(
                    Constants.EXIST_PUBLIC_KEY
                )
            ).orderDesc(WalletBeanDao.Properties.CreateTime).list()
            if (publicKeyWalletList.isNotEmpty()) {
                list.addAll(publicKeyWalletList.filter {
                    checkExt(it.publicKeyExt)
                })
            }
            return list
        }

        /**
         * 判断是否存在重复的闪电钱包
         *
         * @return
         */
        fun checkSameLNWallet(login: String, password: String, host: String): Boolean {
            val walletBeans = WalletDaoUtils.walletDao.loadAll()
            if (walletBeans == null || walletBeans.isEmpty()) {
                return false
            }
            val filter = walletBeans.filter {
                it.lnPassword == password && it.login == login && it.host == host
            }
            return filter.isNotEmpty()
        }
    }

}