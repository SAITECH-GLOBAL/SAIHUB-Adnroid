package com.linktech.saihub.ui.activity.wallet.manager

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityManagerAddressBinding
import com.linktech.saihub.db.bean.WalletBean
import com.linktech.saihub.db.utils.ChildAddressDaoUtil
import com.linktech.saihub.ui.adapter.AddressManagerAdapter
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.view.decoration.SpacesVerticalItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_LIST_ACTIVITY_PATH)
class ManagerAddressActivity : BaseActivity() {

    var binding: ActivityManagerAddressBinding? = null
    var walletBean: WalletBean? = null
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_address)
        walletBean = intent.getParcelableExtra(StringConstants.WALLET_DATA)
        initRecyclerView()
        initData()
    }

    private var addressListAdapter: AddressManagerAdapter? = null
    private fun initRecyclerView() {
        addressListAdapter = AddressManagerAdapter(R.layout.item_list_address_list)

        binding?.recyAddress?.layoutManager = LinearLayoutManager(mContext)
        binding?.recyAddress?.addItemDecoration(SpacesVerticalItemDecoration(PixelUtils.dp2px(16F)))
        binding?.recyAddress?.itemAnimator = null

        addressListAdapter?.addChildClickViewIds(R.id.iv_export_privatekey, R.id.cl_base)
        addressListAdapter?.setOnItemChildClickListener { _, view, position ->
            val itemData = addressListAdapter?.data?.get(position)
            itemData ?: return@setOnItemChildClickListener
            if (view.id == R.id.iv_export_privatekey) {
                ARouter.getInstance()
                    .build(ARouterUrl.WAL_SAIHUB_WALLET_WALMANAGER_ADDRESS_EXPORT_PRIVATEKEY_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withSerializable(StringConstants.ADDRESS_CHILD_DATA, itemData)
                    .navigation()
            } else {
                ARouter.getInstance().build(ARouterUrl.WAL_WALLET_TRANSACTION_RECEIVE_ACTIVITY_PATH)
                    .withParcelable(StringConstants.WALLET_DATA, walletBean)
                    .withString(StringConstants.COIN, StringConstants.BTC)
                    .withString(StringConstants.ADDRESS_SHARE, itemData.childAddress)
                    .navigation()
            }


        }
        addressListAdapter?.isShowExport(walletBean?.existType == Constants.EXIST_MNEMONIC)
        binding?.recyAddress?.adapter = addressListAdapter
    }

    private fun initData() {
        lifecycleScope.launch {
            val addressList = withContext(Dispatchers.IO) {
                walletBean?.addressType?.let {
                    ChildAddressDaoUtil.getChildAddressForTypeNotChange(it, walletBean?.id)
                }
            }
            addressList ?: return@launch

            addressListAdapter?.setNewInstance(addressList)
        }

    }

}