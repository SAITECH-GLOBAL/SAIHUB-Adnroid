package com.linktech.saihub.ui.activity.wallet.transaction

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityWalletListBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.TokenDaoUtil
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.LightningViewModel
import com.linktech.saihub.ui.adapter.wallet.WalletAdapter
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.empty.WalletAddEmptyView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 *  本地钱包链上转账选择列表
 * Created by tromo on 2022/6/23.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_LIST_ACTIVITY_PATH)
class WalletListActivity : BaseActivity() {

    private var binding: ActivityWalletListBinding? = null

    override fun translucentStatusBar(): Boolean = true

    private val walletAdapter by lazy {
        WalletAdapter(R.layout.item_wallet_drawer)
    }

    private val lightningViewModel by lazy {
        ViewModelProvider(this)[LightningViewModel::class.java]
    }

    private val address: String? by lazy {
        intent.getStringExtra(StringConstants.ADDRESS)
    }

    var walletList: List<WalletBean>? = null
    var emptyView: WalletAddEmptyView? = null

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_list)
        binding?.apply {
            rvWallet.layoutManager = LinearLayoutManager(this@WalletListActivity)
            rvWallet.adapter = walletAdapter
            emptyView = WalletAddEmptyView(this@WalletListActivity)
            walletAdapter.setEmptyView(emptyView!!)
            walletAdapter.isUseEmpty = false
            walletAdapter.addChildClickViewIds(R.id.cl_root)
            walletAdapter.setOnItemChildClickListener { adapter, view, position ->
                //切换钱包并跳转转账界面
                val walletBean = adapter.data[position] as? WalletBean
                lifecycleScope.launch(Dispatchers.IO) {
                    walletBean?.id?.let { WalletDaoUtils.updateCurrent(it) }
                    val tokenList =
                        walletBean?.id?.let { TokenDaoUtil.loadTokenDataForWalId(it, true) }
                    withContext(Dispatchers.Main) {
                        EventBus.getDefault()
                            .post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET))
                        ARouter.getInstance().build(ARouterUrl.WAL_WALLET_TRANSACTION_ACTIVITY_PATH)
                            .withParcelable(StringConstants.WALLET_DATA, walletBean)
                            .withSerializable(StringConstants.TOKEN_DATA, tokenList?.get(0))
                            .withString(StringConstants.ADDRESS, address)
                            .navigation()
                        finish()
                    }
                }


            }
        }
    }

    override fun onData() {
        super.onData()
        lightningViewModel.getTransferWallet()

        lightningViewModel.mWalletListData.vmObserver(this) {
            onAppLoading = {
                showLoading()
            }
            onAppSuccess = {
                hideLoading()
                walletAdapter.setNewInstance(it.toMutableList())
            }
            onAppError = {
                hideLoading()
                ToastUtils.shortToast(it.errorMsg)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}