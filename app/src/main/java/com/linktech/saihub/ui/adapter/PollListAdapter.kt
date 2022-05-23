package com.linktech.saihub.ui.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.databinding.ItemListPollBinding
import com.linktech.saihub.db.bean.PollBean

class PollListAdapter(layoutResId: Int) :
    BaseQuickAdapter<PollBean, BaseViewHolder>(layoutResId) {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListPollBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: PollBean) {
        val binding: ItemListPollBinding? = DataBindingUtil.getBinding(holder.itemView)

        binding?.tvPollName?.text = item.pollName
        if (item?.pollUrl.length > 10) {
            binding?.tvPollAddress?.text = "...${item.pollUrl.slice(IntRange(item?.pollUrl.length - 10, item?.pollUrl.length - 1))}"
        } else {
            binding?.tvPollAddress?.text = item.pollUrl
        }
    }
    open class ViewHolder(var dataBinding: ViewDataBinding?) : com.chad.library.adapter.base.viewholder.BaseViewHolder(dataBinding?.root!!) {
    }

}