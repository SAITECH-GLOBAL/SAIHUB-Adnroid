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
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseFragment
import com.linktech.saihub.databinding.FragmentMainPoolBinding
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.db.utils.PollDbUtil
import com.linktech.saihub.ui.adapter.PollListAdapter
import com.linktech.saihub.ui.dialog.ConfirmDialog
import com.linktech.saihub.ui.dialog.NomalListDialog
import com.linktech.saihub.util.CopyUtil
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import com.linktech.saihub.view.empty.PollEmptyView
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainPoolFragment : BaseFragment(), OnRefreshListener {

    private var binding: FragmentMainPoolBinding? = null
    var pollListAdapter: BaseQuickAdapter<PollBean, BaseViewHolder>? = null
    var emptyView: PollEmptyView? = null

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainPoolBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun initViews() {
        val rightTextView = binding?.topbar?.rightTextView
        val drawable = context?.resources?.getDrawable(R.mipmap.icon_add_blue)
        drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        rightTextView?.setCompoundDrawables(drawable, null, null, null)
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val pollList = PollDbUtil.getPollList()
        pollList?.reverse()
        pollListAdapter?.isUseEmpty = true
        pollListAdapter?.setNewInstance(pollList)
    }

    private fun initRecyclerView() {
        binding?.revPollList?.layoutManager = LinearLayoutManager(context)

        pollListAdapter = PollListAdapter(R.layout.item_list_poll)

        binding?.srlTrading?.setOnRefreshListener(this)
        binding?.srlTrading?.setEnableRefresh(false)

        binding?.revPollList?.layoutManager = LinearLayoutManager(context)
        binding?.revPollList?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.revPollList?.itemAnimator = null

        pollListAdapter?.addChildClickViewIds(R.id.iv_pool_set, R.id.cl_base)
        setChildClick()

        binding?.revPollList?.adapter = pollListAdapter
        emptyView = context?.let { PollEmptyView(it) }
        pollListAdapter?.setEmptyView(emptyView!!)
        emptyView?.setAddListener {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                .navigation()
        }
        pollListAdapter?.isUseEmpty = true
    }

    private fun setChildClick() {
        var dialogEditList = arrayListOf(
            getString(R.string.copy_url),
            getString(R.string.edit),
            getString(R.string.delete)
        )
        pollListAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val itemData = pollListAdapter?.data?.get(position)
            itemData ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.cl_base -> {
                    ARouter.getInstance().build(ARouterUrl.WAL_POOL_WEB_URL_ACTIVITY_PATH)
                        .withSerializable(StringConstants.POLL_DATA, itemData)
                        .withString(StringConstants.KEY_URL, itemData?.pollUrl)
                        .withString(StringConstants.TITLE, getString(R.string.poll_title))
                        .navigation()
                }
                R.id.iv_pool_set -> {
                    var dialog = NomalListDialog.newInstance(dialogEditList)
                    dialog?.itemChildEvent = { position ->
                        when (position) {
                            //Copy URL",
                            0 -> {
                                context?.let { it1 ->
                                    CopyUtil.copyCotentSuccess(
                                        it1,
                                        itemData?.pollUrl ?: ""
                                    )
                                }
                            }
                            // "Edit",
                            1 -> {
                                ARouter.getInstance()
                                    .build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                                    .withSerializable(StringConstants.POLL_DATA, itemData)
                                    .navigation()
                            }
                            //"Delete
                            2 -> {
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


    private fun deletePollItem(itemData: PollBean) {
        lifecycleScope?.launch {
            withContext(Dispatchers.IO) {
                PollDbUtil.deleteItem(itemData)
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
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                .navigation()
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }

}