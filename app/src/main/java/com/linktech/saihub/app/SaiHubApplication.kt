package com.linktech.saihub.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.multidex.MultiDexApplication
import com.drake.brv.utils.BRV
import com.google.gson.Gson
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.linktech.saihub.manager.*
import com.linktech.saihub.util.LocalManageUtil
import com.tencent.mmkv.MMKV
import com.linktech.saihub.BR
import com.linktech.saihub.R
import com.linktech.saihub.app.SaihubNetUrl.BASE_WS_URL
import com.linktech.saihub.app.StringConstants.BTC
import com.linktech.saihub.app.StringConstants.NOTIFICATION_DATA
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.utils.WalletDbUtil
import com.linktech.saihub.entity.wallet.bean.socket.NotificationBean
import com.linktech.saihub.entity.wallet.bean.socket.TransferNotificationBean
import com.linktech.saihub.manager.ActivityManager
import com.linktech.saihub.ui.activity.MainActivity
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.NumberCountUtils
import com.linktech.saihub.util.system.AppFrontBackHelper
import com.zhangke.websocket.SimpleListener
import com.zhangke.websocket.WebSocketHandler
import com.zhangke.websocket.WebSocketSetting
import com.zhangke.websocket.response.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.json.JSONObject
import org.opencv.OpenCV
import java.nio.ByteBuffer
import java.util.*


/**
 * Created by tromo on 2021/8/25.
 */
open class SaiHubApplication : MultiDexApplication() {

    companion object {

        private var instance: SaiHubApplication? = null

        fun getInstance(): SaiHubApplication? {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ARouterManager.init(this)
        DbManager.init(this)
        //初始化MMKV
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(callbacks)
        AppConfigManager.getInstance()?.init(this)
        BRV.modelId = BR.m
        //初始化OpenCV
        OpenCV.initAsync(this)
        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(this)
        //预加载x5内核
//        initX5WebView()
        initSocketCommission()
        val helper = AppFrontBackHelper()
        helper.register(this, object : AppFrontBackHelper.OnAppStatusListener {
            override fun onFront() {
                //应用切到前台处理
                LogUtils.e("前台了")
                if (WebSocketHandler.getWebSocket(Constants.SOCKET_WS) != null && !WebSocketHandler.getWebSocket(
                        Constants.SOCKET_WS).isConnect
                ) {
                    WebSocketHandler.getWebSocket(Constants.SOCKET_WS).reconnect()
                }
            }

            override fun onBack() {
                //应用切到后台处理
                LogUtils.e("后台了")
            }
        })

    }


    private fun initSocketCommission() {
        GlobalScope.launch(Dispatchers.IO) {
            //初始化websocket
            val setting = WebSocketSetting()
            //连接地址，必填，例如 wss://localhost:8080
            val webSocketUrl = BASE_WS_URL
            setting.connectUrl =
                if (TextUtils.isEmpty(webSocketUrl)) "wss://h5.binana.vip/invoker/ws/" else webSocketUrl
            //设置连接超时时间
            setting.connectTimeout = 10 * 1000
            //设置心跳间隔时间
            setting.connectionLostTimeout = 10
            //设置断开后的重连次数，可以设置的很大，不会有什么性能上的影响
            setting.reconnectFrequency = 40
            //设置消息分发器，接收到数据后先进入该类中处理，处理完再发送到下游
//        setting.setResponseProcessDispatcher(AppResponseDispatcher())
            //接收到数据后是否放入子线程处理，只有设置了 ResponseProcessDispatcher 才有意义
//        setting.setProcessDataOnBackground(true)
            //网络状态发生变化后是否重连，
            //需要调用 WebSocketHandler.registerNetworkChangedReceiver(context) 方法注册网络监听广播
            WebSocketHandler.registerNetworkChangedReceiver(getInstance())
            setting.setReconnectWithNetworkChanged(true)

            //通过 init 方法初始化默认的 WebSocketManager 对象
            val manager = WebSocketHandler.initGeneralWebSocket(Constants.SOCKET_WS, setting)
            manager.start()
            manager.addListener(object : SimpleListener() {
                override fun onConnected() {
                    sendHeatMessage(false)
                    subAddressToServer()
                    LogUtils.i("socket--commossion-----onConnected")
                }

                override fun onConnectFailed(e: Throwable?) {
                    LogUtils.i("socket--Commission----onConnectFailed")
                }

                override fun onDisconnect() {
                    LogUtils.i("socket--Commission----onDisconnect")
                }

                override fun onSendDataError(errorResponse: ErrorResponse?) {
                    LogUtils.i("socket--Commission---onSendDataError")
                }

                override fun <T : Any?> onMessage(message: String?, data: T) {
                    LogUtils.i("socket--Commission----onMessage$message")
                    runCatching {
                        val jsonObject = JSONObject(message)
                        if (jsonObject.has("pong")) {
                            sendHeatMessage(true)
                        }
                        //订阅地址成功
                        if (jsonObject.has("data")) {
                            val tranPostSocketBean =
                                Gson().fromJson(message, TransferNotificationBean::class.java)
                            createNotificationContent(tranPostSocketBean)
                        }
                    }.onFailure {
                    }

                }

                override fun <T : Any?> onMessage(bytes: ByteBuffer?, data: T) {
                }

                override fun onPing(framedata: Framedata?) {
//                    LogUtils.i("socket--Commission----onPing" + framedata)
                }

                override fun onPong(framedata: Framedata?) {
//                    LogUtils.i("socket--Commission----onPong" + framedata)
                }

            })
        }
    }

    /**
     * 创建通知
     */
    private var id = 0
    fun createNotificationContent(bean: TransferNotificationBean?) {
        if (bean == null) {
            return
        }
        //转账数量
        val amountStr =
            NumberCountUtils.roundTwoZEROBackStr(
                bean.data.amount,
                if (bean.data.coin == BTC) 8 else 6
            )

        //转账币种   代币需要查询代币名称    需要调用接口处理币种名称
        var coinStr = ""
        val tokenInfoData: TokenInfoBean? = null
        coinStr = bean.data.coin
        LogUtils.d("SocketClient2---- ")


        //转行类型  转入 转出  收款  转账 1转入 2转出
        val transferInputOrOut =
            if (bean.data.type == 1) getInstance()?.getString(R.string.receive) else getInstance()?.getString(
                R.string.transfer
            )
        LogUtils.d("SocketClient3-----")
        //转账状态  成功或者失败
        val transferState =
            if (bean.data.status == 1) getInstance()?.getString(R.string.success) else getInstance()?.getString(
                R.string.failed
            )

        val titleStr = "${coinStr}:${amountStr} $transferInputOrOut $transferState"

        val contentStr = if (bean.data.type == 2)
            if (bean.data.coin == BTC)
                getInstance()?.getString(R.string.send_address) + " " +
                        (if (!TextUtils.isEmpty(bean.data.fromAddress)) bean.data.fromAddress else bean.data.contractAddress)
            else
                getInstance()?.getString(R.string.receive_address) + " " +
                        (if (!TextUtils.isEmpty(bean.data.toAddress)) bean.data.toAddress else bean.data.contractAddress)
        else
            if (bean.data.coin == BTC)
                getInstance()?.getString(R.string.receive_address) + " " +
                        (if (!TextUtils.isEmpty(bean.data.toAddress)) bean.data.toAddress else bean.data.contractAddress)
            else
                getInstance()?.getString(R.string.send_address) + " " +
                        (if (!TextUtils.isEmpty(bean.data.fromAddress)) bean.data.fromAddress else bean.data.contractAddress)
        LogUtils.d("SocketClient4-----")


        crateNotification(
            titleStr,
            contentStr,
            bean.data.coin,
            if (bean.data.type == 1) bean.data.toAddress else bean.data.fromAddress,
            tokenInfoData
        )
    }

    private fun crateNotification(
        titleStr: String,
        contentStr: String,
        chainCoin: String,
        walletAddress: String,
        tokenInfoData: TokenInfoBean?
    ) {
        id++

        val linkChainApplication = getInstance() ?: return

        val notificationManager =
            linkChainApplication.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        LogUtils.d("SocketClient-----chainCoin==${chainCoin} ")
        LogUtils.d("SocketClient-----walletAddress==${walletAddress} ")// 17:59:57.554

        //判断 钱包是否删除
        val intent = Intent(linkChainApplication, MainActivity::class.java)
//        intent.action = "notification.intent.action.click"
//        var data = NotifiationEntity(chainCoin, walletAddress, tokenInfoData)
        val notificationBean = NotificationBean()
        notificationBean.chainCoin = chainCoin
        notificationBean.chainAddress = walletAddress
        notificationBean.tokenInfoData = tokenInfoData
        intent.putExtra(NOTIFICATION_DATA, notificationBean)
        val pendingIntent = PendingIntent.getActivity(
            linkChainApplication,
            UUID.randomUUID().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        LogUtils.d("SocketClient-----PendingIntent==${walletAddress} ")// 17:59:57.554
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel =
                NotificationChannel("transfer", "transfer", NotificationManager.IMPORTANCE_HIGH)
            LogUtils.e(mChannel.toString())

            notificationManager.createNotificationChannel(mChannel)
            Notification.Builder(linkChainApplication)
                .setChannelId("transfer")
                .setContentTitle(titleStr)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentText(contentStr)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        linkChainApplication.resources,
                        R.mipmap.app_icon
                    )
                )
                .setSmallIcon(R.mipmap.app_icon).build()
        } else {
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(linkChainApplication)
                    .setContentTitle(titleStr)
                    .setContentText(contentStr)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setChannelId("transfer")
            notificationBuilder.build()
        }
        LogUtils.e("创建通知")
        try {
            if (id > 8) {
                notificationManager.cancel(id - 8)
            }
            notificationManager.notify(id, notification)
        } catch (e: Exception) {
        }
    }

    private fun sendHeatMessage(isDelayed: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val jsonObject = JSONObject()
                jsonObject.put("ws_type", "ping")
                if (isDelayed) {
                    WebSocketHandler.getWebSocket(Constants.SOCKET_WS)
                        ?.sendDelayed(jsonObject.toString())
                } else {
                    WebSocketHandler.getWebSocket(Constants.SOCKET_WS)?.send(jsonObject.toString())
                }
            }.onFailure {
            }
        }
    }

    /**
     * 连接成功订阅地址 订阅所有钱包地址
     */
    fun subAddressToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val addressJson = WalletDbUtil.loadAllWalletSubAddressJSON()
                addressJson
            }.onSuccess {
                WebSocketHandler.getWebSocket(Constants.SOCKET_WS)?.send(it)
            }.onFailure {
            }
        }
    }
//
//
//    open fun initX5WebView() {
//        GlobalScope.launch(Dispatchers.IO) {
//            if (!QbSdk.isTbsCoreInited()) {
//                // 设置X5初始化完成的回调接口
//                QbSdk.preInit(applicationContext, null)
//            }
//            //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
//            val cb: PreInitCallback = object : PreInitCallback {
//                override fun onViewInitFinished(arg0: Boolean) {
//                    if (arg0) {
//                        LogUtils.d("ArticleSystem", "X5 内核加载成功")
//                    } else {
//                        LogUtils.d("ArticleSystem", "X5 内核加载失败")
//                    }
//                }
//
//                override fun onCoreInitFinished() {}
//            }
//            //x5内核初始化接口
//            QbSdk.initX5Environment(applicationContext, cb)
//        }
//    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocalManageUtil.attachBaseContext(base, ""))
    }

    private var callbacks: ActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            ActivityManager.getInstance().addActivity(activity)
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            ActivityManager.getInstance().currentActivity = activity
        }

        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            ActivityManager.getInstance().removeActivity(activity)
        }
    }


}