package com.linktech.saihub.ui.adapter.setting

import android.graphics.Typeface
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.R
import com.linktech.saihub.databinding.ItemListSettingLangBinding
import com.linktech.saihub.manager.RateAndLocalManager

class CurrencyUnitAdapter(layoutResId: Int) :
    BaseQuickAdapter<RateAndLocalManager.RateKind, BaseViewHolder>(layoutResId) {

    var selectPosition = 0
    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListSettingLangBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: RateAndLocalManager.RateKind) {
        val binding: ItemListSettingLangBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.tvLang?.text = item?.name

        if (holder?.layoutPosition == selectPosition) {
            context?.resources?.getColor(R.color.color_FF005F6F)?.let { binding?.tvLang?.setTextColor(it) }
            binding?.ivSelect?.setImageResource(R.mipmap.icon_cb_lang_select)
            try {
                binding?.tvLang?.setTypeface(Typeface.createFromAsset(context.assets, "fonts/montserrat_medium.ttf"));
            } catch (e: Exception) {
            }
        } else {
            binding?.tvLang?.typeface = null
            context?.resources?.getColor(R.color.black)?.let { binding?.tvLang?.setTextColor(it) }
            binding?.ivSelect?.setImageDrawable(null);
        }
    }


}