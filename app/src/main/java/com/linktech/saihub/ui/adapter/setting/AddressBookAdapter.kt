package com.linktech.saihub.ui.adapter.setting

import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.databinding.ItemListAddressBookBinding
import com.linktech.saihub.db.bean.WalletAddressBean
import com.linktech.saihub.util.StringUtils

class AddressBookAdapter(layoutResId: Int) :
    BaseQuickAdapter<WalletAddressBean, BaseViewHolder>(layoutResId) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListAddressBookBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: WalletAddressBean) {
        val binding: ItemListAddressBookBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.tvAddressName?.text = item.addressName
        binding?.tvAddress?.text = StringUtils.formatAddress(item.address)

    }


}