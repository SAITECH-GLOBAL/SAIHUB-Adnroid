package com.linktech.saihub.ui.adapter.wallet

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.databinding.ItemListAddressListBinding
import com.linktech.saihub.databinding.ItemListPollBinding
import com.linktech.saihub.databinding.ItemWalletDrawerBinding
import com.linktech.saihub.db.bean.ChildAddressBean
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.system.setVisible

class WalletAdapter(layoutResId: Int) :
    BaseQuickAdapter<WalletBean, BaseViewHolder>(layoutResId) {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemWalletDrawerBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: WalletBean) {
        val binding: ItemWalletDrawerBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.apply {
            ivSel.setVisible(item.isCurrent)
            tvName.text = item.name
            tvAddress.text = StringUtils.formatAddress(item.address)
        }
    }

}