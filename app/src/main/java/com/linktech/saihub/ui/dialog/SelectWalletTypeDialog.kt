package com.linktech.saihub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.databinding.DialogNomalListBinding
import com.linktech.saihub.databinding.ItemListDialogEditBinding
import com.linktech.saihub.ui.dialog.base.BaseFullBottomSheetFragment
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import java.util.ArrayList

private const val ARG_PARAM1 = "param1"

class SelectWalletTypeDialog() : BaseFullBottomSheetFragment() {

    private var sizeList: ArrayList<Int>? = null

    private var dataList: MutableList<String>? = null

    private var binding: DialogNomalListBinding? = null

    var listAdapter: BaseQuickAdapter<String, BaseViewHolder>? = null

    var itemChildEvent: (position: Int) -> Unit = {}

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<Int>) =
            SelectWalletTypeDialog().apply {
                arguments = Bundle().apply {
                    putIntegerArrayList(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        arguments?.let {
            sizeList = it.getIntegerArrayList(ARG_PARAM1)
        }
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
        dataList =
            mutableListOf(getString(R.string.wallet_on_chain), getString(R.string.wallet_type_ln))
        initRecyView()
    }

    private fun initRecyView() {
        binding?.tvTitle?.setVisible(true)
        binding?.recyEditList?.layoutManager = LinearLayoutManager(context)

        listAdapter = ListAdapter(R.layout.item_list_dialog_edit)

        binding?.recyEditList?.layoutManager = LinearLayoutManager(context)
        binding?.recyEditList?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.recyEditList?.itemAnimator = null

        listAdapter?.addChildClickViewIds(R.id.ll_content)
        listAdapter?.setOnItemChildClickListener { adapter, view, position ->
            if (position == 0)
                addWallet()
            else
                addLnWallet()
//            itemChildEvent(position)
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
                val drawable = if (holder.adapterPosition == 0)
                    context.resources?.getDrawable(R.mipmap.type_chain)
                else
                    context.resources?.getDrawable(R.mipmap.type_ln)
                drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                tvContent.setCompoundDrawables(drawable, null, null, null)
            }
        }
    }

    private fun addWallet() {
        if (sizeList?.get(0) ?: 0 >= 10) {
            ToastUtils.shortImageToast(getString(R.string.wallet_num_tip))
        } else {
            ARouter.getInstance().build(ARouterUrl.WAL_WALLET_ADD_ACTIVITY_PATH)
                .navigation()
            dismiss()
        }

    }

    fun addLnWallet() {
        if (sizeList?.get(1) ?: 0 >= 10) {
            ToastUtils.shortImageToast(getString(R.string.wallet_num_tip))
        } else {
            ARouter.getInstance().build(ARouterUrl.WAL_LN_WALLET_ADD_ACTIVITY_PATH)
                .navigation()
            dismiss()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}