package com.linktech.saihub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.BuildConfig
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.databinding.FragmentMainPowerBinding
import com.linktech.saihub.db.bean.PowerBean
import com.linktech.saihub.db.utils.PowerDbUtil
import com.linktech.saihub.ui.adapter.PowerListAdapter
import com.linktech.saihub.ui.dialog.ConfirmDialog
import com.linktech.saihub.ui.dialog.NomalListDialog
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.empty.PowerEmptyView
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPowerFragment : BaseFragment(), OnRefreshListener {

    private var binding: FragmentMainPowerBinding? = null
    var powerListAdapter: BaseQuickAdapter<PowerBean, BaseViewHolder>? = null
    var emptyView: PowerEmptyView? = null

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPowerBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun initViews() {
        val rightTextView = binding?.topbar?.rightTextView
        val drawable = context?.resources?.getDrawable(R.mipmap.icon_add_blue)
        drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight);
        rightTextView?.setCompoundDrawables(drawable, null, null, null)
        initRecyclerView()
        refreshData()
        /*    if (BuildConfig.DEBUG) {
                var number = 50
                for (index in 0..number) {
                    val powerBean = PowerBean()
                    powerBean.name = "${index}"
                    powerBean.number = "${index}"
                    PowerDbUtil.savePollItem(powerBean)
                }
            }*/
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    //7050603707010178276 测试用账号
    private fun refreshData() {
        val pollList = PowerDbUtil.getPollList()
        pollList?.reverse()
        /* if (BuildConfig.DEBUG) {
             var bean=PowerBean()
             bean.mId=0
             bean.name="test"
             bean.number="7050603707010178276"
             pollList?.add(bean)
         }*/
        powerListAdapter?.isUseEmpty = true
        powerListAdapter?.setNewInstance(pollList)
    }

    private fun initRecyclerView() {
        binding?.revPowerList?.layoutManager = LinearLayoutManager(context)

        powerListAdapter = PowerListAdapter(R.layout.item_list_main_power)

        binding?.srlTrading?.setOnRefreshListener(this)
        binding?.srlTrading?.setEnableRefresh(false)

        binding?.revPowerList?.layoutManager = LinearLayoutManager(context)
        binding?.revPowerList?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.revPowerList?.itemAnimator = null


        setChildClick()

        binding?.revPowerList?.adapter = powerListAdapter
        emptyView = context?.let { PowerEmptyView(it) }
        powerListAdapter?.setEmptyView(emptyView!!)
        emptyView?.setAddListener {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POWER_ADD_ACTIVITY_PATH)
                .navigation()
        }
        powerListAdapter?.isUseEmpty = true
    }

    private fun setChildClick() {
        var dialogEditList = arrayListOf(getString(R.string.edit), getString(R.string.delete))
        powerListAdapter?.addChildClickViewIds(R.id.iv_power_set, R.id.cl_base)
        powerListAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val itemData = powerListAdapter?.data?.get(position)
            itemData ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.cl_base -> {
                    ARouter.getInstance()
                        .build(ARouterUrl.WAL_SAIHUB_WALLET_POWER_DETAIL_ACTIVITY_PATH)
                        .withSerializable(StringConstants.POWER_DATA, itemData)
                        .navigation()
                }
                R.id.iv_power_set -> {
                    var dialog = NomalListDialog.newInstance(dialogEditList)
                    dialog?.itemChildEvent = { position ->
                        when (position) {
                            // "Edit",
                            0 -> {
                                ARouter.getInstance()
                                    .build(ARouterUrl.WAL_SAIHUB_WALLET_POWER_ADD_ACTIVITY_PATH)
                                    .withSerializable(StringConstants.POWER_DATA, itemData)
                                    .navigation()
                            }
                            //"Delete
                            1 -> {
                                var dialog = ConfirmDialog()
                                dialog?.confirmEvent = {
                                    deletePollItem(itemData)
                                }
                                dialog?.show(childFragmentManager, "")
                            }
                        }

                    }
                    dialog.show(childFragmentManager, "")
                }
            }
        }
    }


    private fun deletePollItem(itemData: PowerBean) {
        lifecycleScope?.launch {
            withContext(Dispatchers.IO) {
                PowerDbUtil.deleteItem(itemData)
            }
            ToastUtils.shortRightImageToast(getString(R.string.delete_success))
            refreshData()
        }
    }

    override fun addEvent() {
        /* binding?.topbar?.setLeftOnClickListener {
             ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_LIST_ACTIVITY_PATH)
                 .navigation()
         }*/
        binding?.topbar?.setRightOnClickListener {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POWER_ADD_ACTIVITY_PATH)
                .navigation()
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }
}