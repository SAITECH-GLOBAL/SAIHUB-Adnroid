package com.linktech.saihub.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.KeyEvent
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.navigation.NavigationBarView
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.SOCKET_WS
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityMainBinding
import com.linktech.saihub.db.utils.WalletDbUtil
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.entity.event.SocketEvent
import com.linktech.saihub.entity.wallet.bean.socket.NotificationBean
import com.linktech.saihub.manager.ActivityManager
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletAssetViewModel
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.ui.adapter.FragmentAdapter
import com.linktech.saihub.ui.fragment.MainPoolFragment
import com.linktech.saihub.ui.fragment.MainPowerFragment
import com.linktech.saihub.ui.fragment.MainSettingFragment
import com.linktech.saihub.ui.fragment.MainWalletFragment
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.ToastUtils
import com.zhangke.websocket.WebSocketHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
class MainActivity : BaseActivity() {

    private var binding: ActivityMainBinding? = null
    private val items: MutableList<Pair<String, Fragment>> = ArrayList()
    private var mainFragmentPagerAdapter: FragmentAdapter? = null

    override fun getStatusBlackMode(): Boolean = true
    override fun translucentStatusBar(): Boolean = false

    private val walletAssetViewModel by lazy {
        ViewModelProvider(this)[WalletAssetViewModel::class.java]
    }

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initPagerAndFragment()
        //获取汇率
        walletAssetViewModel.getRate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LogUtils.d("----------")
        val notificationBean =
            intent?.getParcelableExtra<NotificationBean>(StringConstants.NOTIFICATION_DATA)
        if (notificationBean != null)
            walletViewModel.getWalletByAddress(
                notificationBean.chainAddress,
                if (notificationBean.chainCoin.contains(StringConstants.USDT, true))
                    StringConstants.USDT
                else
                    notificationBean.chainCoin.uppercase(Locale.getDefault())
            )
    }

    private fun initPagerAndFragment() {
        items.add(Pair("", MainWalletFragment()))
        items.add(Pair("", MainPoolFragment()))
        items.add(Pair("", MainPowerFragment()))
        items.add(Pair("", MainSettingFragment()))
        mainFragmentPagerAdapter = FragmentAdapter(supportFragmentManager, items)
        binding?.vpMain?.adapter = mainFragmentPagerAdapter
        binding?.vpMain?.offscreenPageLimit = 4
        binding?.vpMain?.isSaveEnabled = false


        binding?.navView?.itemIconTintList = null;
        binding?.navView?.setOnItemSelectedListener(object :
            NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.wallet_fragment -> {
                        binding?.vpMain?.setCurrentItem(0, false)
                        return true
                    }
                    R.id.pool_fragment -> {
                        binding?.vpMain?.setCurrentItem(1, false)
                        return true
                    }
                    R.id.power_fragment -> {
                        binding?.vpMain?.setCurrentItem(2, false)
                        return true
                    }
                    R.id.settings_fragment -> {
                        binding?.vpMain?.setCurrentItem(3, false)
                        return true
                    }
                }
                return true
            }
        })
    }

    override fun onData() {
        super.onData()
        walletViewModel.mPushIntentData.vmObserver(this) {
            onAppSuccess = {
                walletViewModel.updateSelectWallet(it.first)
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_WALLET_TRANSACTION_RECORD_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, it.first)
                    .withSerializable(StringConstants.TOKEN_DATA, it.second)
                    .navigation()
            }
        }
        walletViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                EventBus.getDefault()
                    .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun refreshSocketSub(socketEvent: SocketEvent) {
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val addressJson = WalletDbUtil.loadAllWalletSubAddressJSON()
                addressJson
            }.onSuccess {
                WebSocketHandler.getWebSocket(SOCKET_WS)?.send(it)
            }.onFailure {
            }
        }
    }

    /**
     * 再次点击退出
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val result: Boolean = handleKeyUp(keyCode)
        return if (!result) {
            super.onKeyUp(keyCode, event)
        } else {
            true
        }
    }

    private var mExit = false
    private val exitRevert = Runnable { mExit = false }
    private fun handleKeyUp(keyCode: Int): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return false
        }
        if (mExit) {
            binding?.vpMain?.removeCallbacks(exitRevert)
            ActivityManager.getInstance().finishActivityList()
            exitProcess(0)
        }
        mExit = true
        ActivityManager.getInstance().exitToHome()
        ToastUtils.shortToast(resources.getString(R.string.string_tips_exit))
        binding?.vpMain?.postDelayed(exitRevert, Constants.EXIT_WAIT_TIMEOUT)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}