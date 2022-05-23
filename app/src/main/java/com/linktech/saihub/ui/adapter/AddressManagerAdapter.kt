package com.linktech.saihub.ui.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.databinding.ItemListAddressListBinding
import com.linktech.saihub.databinding.ItemListPollBinding
import com.linktech.saihub.db.bean.ChildAddressBean
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.system.setVisible

class AddressManagerAdapter(layoutResId: Int) :
    BaseQuickAdapter<ChildAddressBean, BaseViewHolder>(layoutResId) {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListAddressListBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: ChildAddressBean) {
        val binding: ItemListAddressListBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.apply {
            item.run {
                tvNumber.text = "${childNumber + 1}"
                tvAddress.text = StringUtils.formatAddress(childAddress)
                ivExportPrivatekey.setVisible(isShowExport)
            }
        }
    }

    var isShowExport = true
    fun isShowExport(b: Boolean) {
        isShowExport = b
    }

}