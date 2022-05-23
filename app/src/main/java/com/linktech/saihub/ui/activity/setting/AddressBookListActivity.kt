package com.linktech.saihub.ui.activity.setting

import android.content.Intent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.Constants.ADDRESS_BOOK_EDIT
import com.linktech.saihub.app.Constants.ADDRESS_BOOK_SELECT
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityAddressBookBinding
import com.linktech.saihub.db.bean.PowerBean
import com.linktech.saihub.db.bean.TokenInfoBean
import com.linktech.saihub.db.bean.WalletAddressBean
import com.linktech.saihub.db.utils.AddressDaoUtil
import com.linktech.saihub.db.utils.PowerDbUtil
import com.linktech.saihub.ui.activity.wallet.transaction.TransactionActivity
import com.linktech.saihub.ui.adapter.setting.AddressBookAdapter
import com.linktech.saihub.ui.dialog.ConfirmDialog
import com.linktech.saihub.ui.dialog.NomalListDialog
import com.linktech.saihub.util.CopyUtil
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import com.linktech.saihub.view.empty.AddressBookEmptyView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ACTIVITY_PATH)
class AddressBookListActivity : BaseActivity() {

    private var binding: ActivityAddressBookBinding? = null

    var bookAdapter: AddressBookAdapter? = null
    var emptyView: AddressBookEmptyView? = null

    private val type: Int by lazy {
        intent.getIntExtra(StringConstants.LOAD_TYPE, ADDRESS_BOOK_EDIT)
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_address_book)

        val rightTextView = binding?.topbar?.rightTextView
        val drawable = resources?.getDrawable(R.mipmap.icon_add_blue)
        drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        rightTextView?.setCompoundDrawables(drawable, null, null, null)

        initRvView()

        binding?.topbar?.setRightOnClickListener {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ADD_ACTIVITY_PATH)
                .navigation()
        }
    }


    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val addressList = AddressDaoUtil.loadAll()
        addressList.reverse()
        bookAdapter?.isUseEmpty = true
        bookAdapter?.setNewInstance(addressList)
    }


    private fun initRvView() {
        bookAdapter = AddressBookAdapter(R.layout.item_list_address_book)

        binding?.rvAddressBook?.layoutManager = LinearLayoutManager(mContext)
        binding?.rvAddressBook?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.rvAddressBook?.itemAnimator = null
        binding?.rvAddressBook?.adapter = bookAdapter

        emptyView = mContext?.let { AddressBookEmptyView(it) }
        bookAdapter?.setEmptyView(emptyView!!)
        bookAdapter?.isUseEmpty = false

        emptyView?.setAddListener {
            ARouter.getInstance()
                .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ADD_ACTIVITY_PATH)
                .navigation()
        }

        bookAdapter?.addChildClickViewIds(R.id.cl_base)
        val dialogEditList = arrayListOf(
            getString(R.string.copy_address),
            getString(R.string.edit),
            getString(R.string.delete)
        )
        bookAdapter?.setOnItemChildClickListener { _, _, position ->
            val itemData = bookAdapter?.data?.get(position)
            itemData ?: return@setOnItemChildClickListener
            if (type == ADDRESS_BOOK_SELECT) {
                val intent =
                    Intent().putExtra(StringConstants.ADDRESS, itemData.address)
                setResult(TransactionActivity.RESULT_CODE, intent)
                finish()
            } else {
                val dialog = NomalListDialog.newInstance(dialogEditList)
                dialog.itemChildEvent = { position ->
                    when (position) {
                        //Copy Address",
                        0 -> {
                            mContext?.let { it1 ->
                                CopyUtil.copyCotentSuccess(
                                    it1,
                                    itemData?.address ?: ""
                                )
                            }
                        }
                        // "Edit",
                        1 -> {
                            ARouter.getInstance()
                                .build(ARouterUrl.WAL_SAIHUB_WALLET_SETTING_ADDRESS_BOOK_ADD_ACTIVITY_PATH)
                                .withSerializable(StringConstants.ADDRESS_BOOK_DATA, itemData)
                                .navigation()
                        }
                        // "Delete",
                        2 -> {
                            val dialog = ConfirmDialog()
                            dialog.confirmEvent = {
                                deletePollItem(itemData)
                            }
                            dialog.show(supportFragmentManager, "")

                        }
                    }

                }
                dialog.show(supportFragmentManager, "")
            }

        }
    }

    private fun deletePollItem(itemData: WalletAddressBean) {
        lifecycleScope?.launch {
            withContext(Dispatchers.IO) {
                AddressDaoUtil.delete(itemData)
            }
            ToastUtils.shortRightImageToast(getString(R.string.delete_success))
            refreshData()
        }
    }

}