package com.linktech.saihub.app

import android.os.Environment
import java.io.File

object Constants {
    /**
     * uuid
     */
    const val UUID = "app_uuid"

    const val CURRENT_LANGUAGE = "current_language"

    const val ASSET_REFRESH_INTERVAL = 60000L
    const val CLICK_INTERVAL_500 = 500L
    const val CLICK_INTERVAL = 1000L

    /**
     * 密码验证间隔时间     (设置-开启关闭指纹验证)
     */
    const val VERIFY_PWD_TIME = 1000 * 60

    const val SOCKET_WS = "socket_ws"

    /**
     * 验证phrase 助记词的方式
     */
    const val PHRASE_TYPE_CREATE_WALLET = 0 //创建钱包验证助记词
    const val PHRASE_TYPE_BACKUP_WALLET = 1 //备份钱包验证助记词


    const val CHILD_ADDRESS_SIZE = 20 //生成子地址数量


    const val TYPE_CREATE = 1
    const val TYPE_IMPORT = 2

    /**
     * 当前是否开启密码验证-登录
     */
    const val VERIFY_PWD_LOGIN = "verify_pwd_login"

    /**
     * 当前是否开启指纹验证-登录
     */
    const val VERIFY_TOUCH_ID_LOGIN = "verify_touch_id_login"


    /**
     * 开启验证码以后 存储本地的密码
     */
    const val VERIFY_PWD_LOCAL: String = "verify_pwd_local"

    /**
     * 指纹验证最多次数
     */
    const val MAX_AVAILABLE_TIMES = 3


    /**
     * btc最小交易数量
     */
    const val BTC_MIN_NUMBER = 0.000006

    /**
     * 是否创建或导入过钱包
     */
    const val IS_HAVE_WALLET = "is_have_wallet"

    /**
     * 是否第一次运行
     */
    const val IS_NOT_FIRST = "is_not_first"

    /**
     * 是否第一次运行
     */
    const val IS_JUMP_PWD = "is_jump_pwd"

    /**
     * app 安装时间
     */
    const val APP_CREATE_TIME = "app_create_time"

    /**
     * 语言
     */
    const val LANGUAGE = "language"
    const val Android = 1

    /**
     * 地址本上线
     */
    const val ADDRESS_UP_NUMBER = 20

    /**
     * 矿池 电力热力
     */
    const val POLL_POWER_UP_NUMBER = 50

    /**
     * 是否有通知消息(在收到转账通知和系统通知消息时候  更改为true)  我的界面右上角的通知铃铛显示
     */
    const val IS_HAVA_NOTIFICATION_UNREAD = "is_hava_notification_unread"

    /**
     * 当前的余额是否显示
     */
    const val MONEY_VISIBLE = "money_visible"

    /**
     * 当前是否免密
     */
    const val AVOID_PASSWORD = "avoidpassword"

    /**
     * 已读id
     */
    const val READ_NOTIFICATION_ID = "read_notification_id"
    const val READ_NOTIFICATION_TIME = "read_notification_time"

    /**
     * 已读交易通知id
     */
    const val READ_TRANSFER_NOTIFICATION_ID = "read_transfer_notification_id"
    const val READ_TRANSFER_NOTIFICATION_TIME = "read_transfer_notification_time"


    /**
     * 当前的币种
     */
    const val CURRENCY = "currency"
    const val CURRENCY_RMB = 0
    const val CURRENCY_USD = 1
    const val WALLET_IMPORT_TYPE = "wallet_import_type"
    const val WAL_IMPORT_KEYSTORE = 1
    const val WAL_IMPORT_MNEMOINIC = 2
    const val WAL_IMPORT_OBSERVER = 3
    const val WAL_IMPORT_PRIVATEKEY = 4
    const val APIKEY = "P5CN36B8PEXUBVS1VDY5RW7VZX7CE9EZGQ"
    //    public static final String APIKEY = "SU3BF2F31WQAYR6M82285FXZUQ427M8FMY";

    /**
     * 支付方式  //0是密码  1是指纹
     */
    const val PAY_TYPE_PWD = 0
    const val PAY_TYPE_FINGERPRINT = 1

    /**
     * 选择的地址类型
     */
    //    public static final String CHILD_ADDRESS_TYPE = "child_address_type";
    const val CHILD_ADDRESS_NATIVE = 0 //隔离原生地址
    const val CHILD_ADDRESS_NESTED = 1 //隔离兼容地址
    const val CHILD_ADDRESS_NORMAL = 2 //普通地址
    const val CHILD_ADDRESS_CHANGE = -1 //找零
    const val CHILD_ADDRESS_CHANGE_NUMBER_KEY = "child_change_address_number" //记录找零地址number
    const val CHILD_ADDRESS_CHANGE_NUMBER = 0 //找零地址的当前number1

    const val WAL_TOKEN_TYPE_NOT = -1 //非代币
    const val WAL_TOKEN_TYPE_BTC_OMNI = 9


    const val VALIDATE_MNEMONIC = 0 //助记词类型
    const val VALIDATE_PRIVATE_KEY = 1 //私钥类型
    const val VALIDATE_PUBLIC_KEY = 2 //拓展公钥类型
    const val VALIDATE_ADDRESS = 3 //普通地址类型
    const val VALIDATE_MULTI_SIG = 4 //多签类型


    const val EXIST_MNEMONIC = 1//助记词类型
    const val EXIST_PRIVATE_KEY = 2//私钥类型
    const val EXIST_PUBLIC_KEY = 3 //拓展公钥类型
    const val EXIST_ADDRESS = 4 //普通地址类型
    const val EXIST_MULTI_SIG = 5 //多签类型
    const val EXIST_LIGHTNING = 6 //闪电网络类型

    /**
     * 非找零地址
     */
    const val CHILD_ADDRESS_NOT_CHANGE_TYPE = 0

    /**
     * 找零地址
     */
    const val CHILD_ADDRESS_CHANGE_TYPE = 1

    /**
     * 地址本类型 1编辑   2选择
     */
    const val ADDRESS_BOOK_EDIT = 1
    const val ADDRESS_BOOK_SELECT = 2


    const val BITCOIN_BROWSER_URL = "https://explorer.btc.com/btc/"


    /**
     * 钱包主界面备份
     */
    const val WAL_MAIN_BACKUP = 1

    /**
     * 查询不同地址  本币地址  代币地址
     */
    const val ADDRESS_TYPE = "address_type"
    const val ADDRESS_TYPE_MAIN = 1 //主币
    const val ADDRESS_TYPE_CONTRACT = 2 //代币
    const val CONTRACTADDRESS = "contractaddress"

    /**
     * 转账的类型 //   0所有 1转出  2转入    3交易失败(转出) 4转账中(转出)
     */
    const val TRAN_TYPE_ALL = 0
    const val TRAN_TYPE_TO = 1
    const val TRAN_TYPE_FROM = 2
    const val TRAN_TYPE_FAIL = 3
    const val TRAN_TYPE_ING = 4

    /**
     * 转账通知是否开启
     */
    const val TRANSFER_NOTIFICATION = "transfer_notification"
    const val TRANSFER_NOTIFY_OPEN = 0
    const val TRANSFER_NOTIFY_CLOSE = 1

    /**
     * 闪兑
     */
    const val QUICK_EXCHANGE_ETH = 1

    /**
     * 交易
     */
    const val TOKEN_DATA = "token_data"

    //app更新下载名称
    const val DOWNLOAD_FILE_NAME = "saihub.apk"
    const val DOWNLOAD_FOLDER_NAME = "saihubDownload"

    //apk更新下载目录名称
    val UPDATE_FOLDER_NAME = Environment.getExternalStorageDirectory().toString() +
            File.separator + DOWNLOAD_FOLDER_NAME

    //分享图片目录名称
    val SHARE_FOLDER_NAME =
        Environment.getExternalStorageDirectory().toString() + File.separator + "saihub"
    const val EXIT_WAIT_TIMEOUT: Long = 3000 //连续两次点击返回退出APP

    const val Level_0 = 0x99
    const val Level_1 = 0x100
    const val Level_2 = 0x101
    const val Level_3 = 0x102

    /**
     * 扫码码
     */
    const val REQUEST_CODE_SCAN = 0x1002

    /**
     * filprovider
     */
    const val FILEPROVIDER_AUTHORITIES = "com.linktech.saihub.provider"

    const val WEBSITE_URL = "https://sai.tech"

}