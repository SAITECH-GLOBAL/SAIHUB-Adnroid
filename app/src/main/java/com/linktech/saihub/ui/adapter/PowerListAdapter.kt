package com.linktech.saihub.ui.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.databinding.ItemListMainPowerBinding
import com.linktech.saihub.db.bean.PowerBean

class PowerListAdapter(layoutResId: Int) :
    BaseQuickAdapter<PowerBean, BaseViewHolder>(layoutResId) {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListMainPowerBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: PowerBean) {
        val binding: ItemListMainPowerBinding? = DataBindingUtil.getBinding(holder.itemView)

        binding?.tvPowerName?.text = item.name
        binding?.tvPowerNumber?.text = item.number
    }

    open class ViewHolder(var dataBinding: ViewDataBinding?) : com.chad.library.adapter.base.viewholder.BaseViewHolder(dataBinding?.root!!) {
    }

}