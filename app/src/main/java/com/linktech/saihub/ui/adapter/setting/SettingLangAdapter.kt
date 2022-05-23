package com.linktech.saihub.ui.adapter.setting

import android.graphics.Typeface
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.R
import com.linktech.saihub.databinding.ItemListSettingLangBinding
import com.linktech.saihub.manager.RateAndLocalManager

class SettingLangAdapter(layoutResId: Int) :
    BaseQuickAdapter<RateAndLocalManager.LocalKind, BaseViewHolder>(layoutResId) {

    var selectPosition = 0
    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<ItemListSettingLangBinding>(viewHolder.itemView)
    }


    override fun convert(holder: BaseViewHolder, item: RateAndLocalManager.LocalKind) {
        val binding: ItemListSettingLangBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.tvLang?.text = item?.name

        if (holder.layoutPosition == selectPosition) {
            if (selectPosition == 1 || selectPosition == 2) {
                binding?.tvLang?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            } else {
                try {
                    binding?.tvLang?.setTypeface(
                        Typeface.createFromAsset(
                            context.assets,
                            "fonts/montserrat_medium.ttf"
                        )
                    )
                } catch (e: Exception) {
                }
            }
            context.resources?.getColor(R.color.color_FF005F6F)
                ?.let { binding?.tvLang?.setTextColor(it) }
            binding?.ivSelect?.setImageResource(R.mipmap.icon_cb_lang_select)

        } else {
            binding?.tvLang?.typeface =
                Typeface.createFromAsset(context.assets, "fonts/montserrat_regular.ttf")
            context.resources?.getColor(R.color.black)?.let { binding?.tvLang?.setTextColor(it) }
            binding?.ivSelect?.setImageDrawable(null);
        }
    }


}