package com.linktech.saihub.ui.adapter.vp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.BRV
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.linktech.saihub.BR
import com.linktech.saihub.R
import com.linktech.saihub.entity.databinding.Phrase


/**
 * Created by tromo on 2022/2/23.
 */
class RvAdapter(var data: List<List<Phrase>>) :
    RecyclerView.Adapter<RvAdapter.ViewPagerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RvAdapter.ViewPagerViewHolder {
        return ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RvAdapter.ViewPagerViewHolder, position: Int) {
        holder.rvItem.linear().setup {
            addType<Phrase>(R.layout.item_phrase)
        }.models = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // 1.建1个内部类
    class ViewPagerViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var rvItem: RecyclerView = itemView.findViewById(R.id.rv_item)

    }
}