package com.linktech.saihub.ui.activity.poll

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPoolListBinding
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.db.utils.PollDbUtil
import com.linktech.saihub.ui.adapter.PollListAdapter
import com.linktech.saihub.ui.dialog.NomalListDialog
import com.linktech.saihub.util.CopyUtil
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import com.linktech.saihub.view.empty.PollEmptyView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_POLL_LIST_ACTIVITY_PATH)
class PoolListActivity : BaseActivity() {

    private var binding: ActivityPoolListBinding? = null
    var pollListAdapter: BaseQuickAdapter<PollBean, BaseViewHolder>? = null
    var emptyView: PollEmptyView? = null

    var dialogEditList = arrayListOf<String>("Copy URL", "Edit", "Delete")

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pool_list)

        initRecyclerView()
        initEvent()
    }

    private fun initEvent() {
        binding?.topbar?.setRightOnClickListener {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                .navigation()
        }
    }


    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val pollList = PollDbUtil.getPollList()
        pollListAdapter?.isUseEmpty = true
        pollListAdapter?.setNewInstance(pollList)
    }


    private fun initRecyclerView() {
        pollListAdapter = PollListAdapter(R.layout.item_list_poll)

        binding?.recyPollList?.layoutManager = LinearLayoutManager(mContext)
        binding?.recyPollList?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.recyPollList?.itemAnimator = null

        pollListAdapter?.addChildClickViewIds(R.id.iv_pool_set, R.id.cl_base)
        setChildClick()

        binding?.recyPollList?.adapter = pollListAdapter

        emptyView = mContext?.let { PollEmptyView(it) }
        pollListAdapter?.setEmptyView(emptyView!!)
        emptyView?.setAddListener {
            ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                .navigation()
        }
        pollListAdapter?.isUseEmpty = false
    }

    private fun setChildClick() {
        pollListAdapter?.setOnItemChildClickListener { _, view, position ->
            val itemData = pollListAdapter?.data?.get(position)
            itemData ?: return@setOnItemChildClickListener
            when (view.id) {

                R.id.cl_base -> {
                    ARouter.getInstance().build(ARouterUrl.WAL_POOL_WEB_URL_ACTIVITY_PATH)
                        .withString(StringConstants.KEY_URL, itemData.pollUrl)
                        .navigation()
                }
                R.id.iv_pool_set -> {
                    var dialog = NomalListDialog.newInstance(dialogEditList)
                    dialog?.itemChildEvent = { position ->
                        when (position) {
                            //Copy URL",
                            0 -> {
                                mContext?.let { it1 -> CopyUtil.copyCotentSuccess(it1, itemData?.pollUrl ?: "") }
                            }
                            // "Edit",
                            1 -> {
                                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
                                    .withSerializable(StringConstants.POLL_DATA, itemData)
                                    .navigation()
                            }
                            //"Delete
                            2 -> {
                                deletePollItem(itemData)
                            }
                        }

                    }
                    dialog.show(supportFragmentManager, "")
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

}