package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.R
import com.linktech.saihub.databinding.DialogNomalListBinding
import com.linktech.saihub.databinding.ItemListDialogEditBinding
import com.linktech.saihub.ui.dialog.base.BaseFullBottomSheetFragment
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration

class SelectRechargeTypeDialog() : BaseFullBottomSheetFragment() {

    private var dataList: MutableList<String>? = null

    private var binding: DialogNomalListBinding? = null

    var listAdapter: BaseQuickAdapter<String, BaseViewHolder>? = null

    var itemChildEvent: (position: Int) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogNomalListBinding.inflate(inflater, container, false)
        setTopOffset(PixelUtils.dp2px(0f))
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataList = mutableListOf(getString(R.string.wallet_local), getString(R.string.wallet_other))
        initRecyView()
    }

    private fun initRecyView() {
        binding?.tvCenterTitle?.setVisible(true)
        binding?.tvCenterTitle?.text = getString(R.string.recharge_chain)
        binding?.recyEditList?.layoutManager = LinearLayoutManager(context)

        listAdapter = ListAdapter(R.layout.item_list_dialog_edit)

        binding?.recyEditList?.layoutManager = LinearLayoutManager(context)
        binding?.recyEditList?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.recyEditList?.itemAnimator = null

        listAdapter?.addChildClickViewIds(R.id.ll_content)
        listAdapter?.setOnItemChildClickListener { adapter, view, position ->
            itemChildEvent(position)
            dismiss()
        }

        binding?.recyEditList?.adapter = listAdapter

        listAdapter?.setNewInstance(dataList)

    }

    class ListAdapter(layoutResId: Int) :
        BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {

        override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
            DataBindingUtil.bind<ItemListDialogEditBinding>(viewHolder.itemView)
        }

        override fun convert(holder: BaseViewHolder, item: String) {
            val binding: ItemListDialogEditBinding? = DataBindingUtil.getBinding(holder.itemView)
            binding?.apply {
                tvContent.text = item
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}