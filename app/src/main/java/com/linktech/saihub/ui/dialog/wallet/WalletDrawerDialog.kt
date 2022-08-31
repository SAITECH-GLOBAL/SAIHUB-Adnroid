package com.linktech.saihub.ui.dialog.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.dialog.BaseDrawerDialogFragment
import com.linktech.saihub.databinding.DialogWalletDrawerBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.mvvm.base.vmObserver
import com.linktech.saihub.mvvm.model.WalletViewModel
import com.linktech.saihub.ui.adapter.wallet.WalletAdapter
import com.linktech.saihub.ui.dialog.SelectWalletTypeDialog
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.empty.WalletAddEmptyView
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus


/**
 * 钱包列表抽屉
 */
class WalletDrawerDialog : BaseDrawerDialogFragment(), WalletAddEmptyView.AddListener {


    private var binding: DialogWalletDrawerBinding? = null
    private var onDisMissListener: OnDisMissListener? = null
    private var onClickListener: OnClickListener? = null
    private var nameJob: Job? = null

    private val walletAdapter by lazy {
        WalletAdapter(R.layout.item_wallet_drawer)
    }

    private val walletObAdapter by lazy {
        WalletAdapter(R.layout.item_wallet_drawer)
    }

    private val walletLnAdapter by lazy {
        WalletAdapter(R.layout.item_wallet_drawer)
    }

    var emptyView: WalletAddEmptyView? = null
    var emptyView1: WalletAddEmptyView? = null
    var emptyView2: WalletAddEmptyView? = null

    var walletList: List<WalletBean>? = null
    var walletObList: List<WalletBean>? = null
    var walletLnList: List<WalletBean>? = null

    private val walletViewModel by lazy {
        ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogWalletDrawerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        init()
        setData()
    }

    private fun setData() {
        loadWalletData()

        walletViewModel.mWalletListData.vmObserver(this) {
            onAppSuccess = {
                walletList = it
                walletAdapter.isUseEmpty = true
                walletAdapter.setNewInstance(it.toMutableList())
            }

        }

        walletViewModel.mWalletListObserverData.vmObserver(this) {
            onAppSuccess = {
                walletObList = it
                walletObAdapter.isUseEmpty = true
                walletObAdapter.setNewInstance(it.toMutableList())
            }
        }

        walletViewModel.mWalletListLnData.vmObserver(this) {
            onAppSuccess = {
                walletLnList = it
                walletLnAdapter.isUseEmpty = true
                walletLnAdapter.setNewInstance(it.toMutableList())
            }
        }
        //TODO 滑动
//        binding?.cslAsset?.smoothScrollToChild()

        walletViewModel.mCurrentWalletData.vmObserver(this) {
            onAppSuccess = {
                EventBus.getDefault()
                    .postSticky(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_WALLET))
                dismiss()
            }
        }

    }

    private fun init() {
        binding?.apply {
            btnAddWallet.onClick(Constants.CLICK_INTERVAL) {
                val sizeList =
                    arrayListOf(
                        (walletObList?.size?.let { walletList?.size?.plus(it) }) ?: 0,
                        walletLnList?.size!!
                    )
                val selectWalletTypeDialog = SelectWalletTypeDialog.newInstance(sizeList)
                selectWalletTypeDialog.showNow(parentFragmentManager, "")
                dismiss()
            }
            rvWalletList.layoutManager = LinearLayoutManager(context)
            rvWalletObList.layoutManager = LinearLayoutManager(context)
            rvWalletLnList.layoutManager = LinearLayoutManager(context)
            rvWalletList.adapter = walletAdapter
            rvWalletObList.adapter = walletObAdapter
            rvWalletLnList.adapter = walletLnAdapter


            emptyView = context?.let { WalletAddEmptyView(it) }
            emptyView1 = context?.let { WalletAddEmptyView(it) }
            emptyView2 = context?.let { WalletAddEmptyView(it) }
            walletAdapter.setEmptyView(emptyView!!)
            walletObAdapter.setEmptyView(emptyView1!!)
            walletLnAdapter.setEmptyView(emptyView2!!)
            walletAdapter.isUseEmpty = false
            walletObAdapter.isUseEmpty = false
            walletLnAdapter.isUseEmpty = false

            emptyView?.setAddListener(this@WalletDrawerDialog)
            emptyView1?.setAddListener(this@WalletDrawerDialog)
            emptyView2?.setAddListener {
                ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_ADD_ACTIVITY_PATH)
                    .navigation()
                dismiss()
            }

            walletAdapter.setOnItemClickListener { adapter, _, position ->
                (adapter.data[position] as? WalletBean)?.let { walletViewModel.updateSelectWallet(it) }
            }
            walletObAdapter.setOnItemClickListener { adapter, _, position ->
                (adapter.data[position] as? WalletBean)?.let { walletViewModel.updateSelectWallet(it) }
            }
            walletLnAdapter.setOnItemClickListener { adapter, _, position ->
                (adapter.data[position] as? WalletBean)?.let { walletViewModel.updateSelectWallet(it) }
            }
        }
    }

    override fun addWallet() {
        if ((walletObList?.size?.let { walletList?.size?.plus(it) }) ?: 0 >= 10) {
            ToastUtils.shortImageToast(getString(R.string.wallet_num_tip))
        } else {
            ARouter.getInstance().build(ARouterUrl.WAL_WALLET_ADD_ACTIVITY_PATH)
                .navigation()
            dismiss()
        }

    }


    private fun loadWalletData() {
        walletViewModel.getWalletList()
        walletViewModel.getObserverWalletList()
        walletViewModel.getLnWalletList()
    }


    interface OnDisMissListener {
        fun onDismiss()
    }

    interface OnClickListener {
        fun onConfirmClick(isCheck: Boolean)
    }

    fun setOnDisMissListener(onDisMissListener: OnDisMissListener) {
        this.onDisMissListener = onDisMissListener
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }


    override fun onDestroy() {
        super.onDestroy()
        nameJob?.cancel()
        onDisMissListener?.onDismiss()
    }

}