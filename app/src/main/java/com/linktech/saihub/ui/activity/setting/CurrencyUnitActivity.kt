package com.linktech.saihub.ui.activity.setting

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityCurrencyUnitBinding
import com.linktech.saihub.entity.event.MessageEvent
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.ui.adapter.setting.CurrencyUnitAdapter
import com.linktech.saihub.view.decoration.SimpleDividerItemDecoration
import com.qmuiteam.qmui.kotlin.onClick
import org.greenrobot.eventbus.EventBus

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_UNIT_ACTIVITY_PATH)
class CurrencyUnitActivity : BaseActivity() {

    private var binding: ActivityCurrencyUnitBinding? = null

    private var langListAdapter: CurrencyUnitAdapter? = null
    var selectPosition = 0
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_currency_unit)


        langListAdapter = CurrencyUnitAdapter(R.layout.item_list_setting_lang)
        binding?.rvLang?.layoutManager = LinearLayoutManager(mContext)

        binding?.rvLang?.layoutManager = LinearLayoutManager(mContext)
        binding?.rvLang?.addItemDecoration(SimpleDividerItemDecoration(mContext))
        binding?.rvLang?.itemAnimator = null
        binding?.rvLang?.adapter = langListAdapter


        var mLocals = RateAndLocalManager.RateKind.values()
        var mSelectLocal = RateAndLocalManager.getInstance(mContext).curRateKind
        mLocals?.forEachIndexed { index, localKind ->
            if (mSelectLocal?.code?.equals(localKind?.code) == true) {
                selectPosition = index
            }
        }
        langListAdapter?.selectPosition = selectPosition
        langListAdapter?.setNewInstance(mLocals.toMutableList())
        langListAdapter?.addChildClickViewIds(R.id.cl_base)
        langListAdapter?.setOnItemChildClickListener { adapter, view, position ->
            if (selectPosition == position) {
                return@setOnItemChildClickListener
            } else {
                selectPosition = position
                langListAdapter?.selectPosition = position
                langListAdapter?.notifyDataSetChanged()
            }
        }
        binding?.tvSave?.onClick(Constants.CLICK_INTERVAL) {
            try {
                val get = mLocals?.get(selectPosition)
                changeRate(get.code)
            } catch (e: Exception) {
            }
        }
    }

    private fun changeRate(rate: String) {
        RateAndLocalManager.getInstance(mContext).changeRateKind(rate)
        EventBus.getDefault().post(MessageEvent.getInstance(MessageEvent.MESSAGE_ID_CHANGE_CURRENCY_UNIT))
        finish()
    }
}