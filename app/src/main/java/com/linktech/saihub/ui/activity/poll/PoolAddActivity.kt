package com.linktech.saihub.ui.activity.poll

import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityPoolAddBinding
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.db.utils.PollDbUtil
import com.linktech.saihub.db.utils.PowerDbUtil
import com.linktech.saihub.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_POLL_ADD_ACTIVITY_PATH)
class PoolAddActivity : BaseActivity() {

    private var binding: ActivityPoolAddBinding? = null

    var pollEditData: PollBean? = null
    var isEdit=false

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pool_add)

        pollEditData = intent.getSerializableExtra(StringConstants.POLL_DATA) as PollBean?


        binding?.etRemarkName?.addTextChangedListener {
            setBtEnable()
        }
        binding?.etUrl?.addTextChangedListener {
            setBtEnable()
        }


        if (pollEditData != null) {
            isEdit=true
            binding?.topbar?.setTitle(getString(R.string.edit_obsever_link))
            binding?.etRemarkName?.setText(pollEditData?.pollName)
            binding?.etUrl?.setText(pollEditData?.pollUrl)
        }

        binding?.tvSave?.onClick(Constants.CLICK_INTERVAL_500) {
            var pollBean = if (pollEditData == null) {
                PollBean()
            } else {
                pollEditData
            }
            pollBean?.pollName = binding?.etRemarkName?.text.toString()
            pollBean?.pollUrl = binding?.etUrl?.text.toString()
            lifecycleScope.launch {

                if (!isEdit) {
                    val powerNumber = withContext(Dispatchers.IO) {
                        PollDbUtil.havePowerCount()
                    }
                    if (powerNumber >= Constants.POLL_POWER_UP_NUMBER) {
                        ToastUtils.shortImageToast(getString(R.string.list_is_full))
                        return@launch
                    }
                }


                var insertId = withContext(Dispatchers.IO) {
                    pollBean?.let { it1 -> PollDbUtil.savePollItem(it1) }
                }
                if (insertId != null && insertId != -1L) {
                    finish()
                } else {
                    ToastUtils.shortImageToast(getString(R.string.poll_save_error))
                }
            }
        }
    }

    private fun setBtEnable() {
        binding?.tvSave?.isEnabled = binding?.etRemarkName?.text?.length!! > 0 && binding?.etUrl?.text?.length!! > 0
    }
}