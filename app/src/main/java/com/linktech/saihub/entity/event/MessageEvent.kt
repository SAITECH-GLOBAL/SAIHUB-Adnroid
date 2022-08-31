package com.linktech.saihub.entity.event

import com.linktech.saihub.db.bean.WalletBean

class MessageEvent() {


    var id: Int? = null

    var walletBean: WalletBean? = null


    constructor(id: Int) : this() {
        this.id = id
    }

    constructor(id: Int, walletData: WalletBean) : this() {
        this.id = id
        this.walletBean = walletData
    }

    companion object {
        /**
         * 切换币种单位 CNY USD
         */
        const val MESSAGE_ID_CHANGE_CURRENCY_UNIT = 1

        /**
         * 删除钱包
         */
        const val MESSAGE_ID_DELETE_WALLET = 2

        /**
         * 修改钱包密码
         */
        const val MESSAGE_ID_CHANGE_PWD_WALLET = 3

        /**
         * 开启关闭  touchid pay
         */
        const val MESSAGE_ID_CHANGE_TOUCHID_PAY_WALLET = 4

        /**
         * 设置passphrase 覆盖钱包
         */
        const val MESSAGE_ID_CHANGE_SET_PASSPHRASE_CONVERT_WALLET = 5

        /**
         * 创建钱包
         */
        const val MESSAGE_ID_CHANGE_CREATE_WALLET = 6

        /**
         * 管理钱包-通过私钥或者助记词 重置密码
         */
        const val MESSAGE_ID_CHANGE_RESET_PWD_WALLET = 7

        /**
         * 切换钱包
         */
        const val MESSAGE_ID_CHANGE_WALLET = 8

        /**
         * 更換錢包主地址
         */
        const val MESSAGE_ID_CHANGE_WALLET_ADDRESS = 9

        /**
         * 开启关闭闪电钱包支付密码
         */
        const val MESSAGE_ID_CHANGE_PAY_PWD_LN_WALLET = 10
        /**
         * 刷新闪电钱包交易记录
         */
        const val MESSAGE_ID_REFRESH_RECORD_LN_WALLET = 11
        /**
         * 刷新闪电钱包token
         */
        const val MESSAGE_ID_REFRESH_TOKEN_LN_WALLET = 12

        fun getInstance(id: Int): MessageEvent? {
            return MessageEvent(id)
        }

        fun getInstance(id: Int, walletData: WalletBean): MessageEvent? {
            return MessageEvent(id, walletData)
        }
    }

}