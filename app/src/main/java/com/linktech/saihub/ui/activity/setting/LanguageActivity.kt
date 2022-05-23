package com.linktech.saihub.ui.activity.setting

import android.content.Intent
import androidx.core.view.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityLanguageBinding
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.manager.RateAndLocalManager
import com.linktech.saihub.ui.activity.MainActivity
import com.linktech.saihub.ui.adapter.setting.SettingLangAdapter
import com.linktech.saihub.util.LocalManageUtil
import com.linktech.saihub.view.decoration.SimpleDividerItemDecoration
import com.qmuiteam.qmui.kotlin.onClick

@Route(path = ARouterUrl.WAL_SAIHUB_WALLET_SETTING_LANG_ACTIVITY_PATH)
class LanguageActivity : BaseActivity() {

    private var binding: ActivityLanguageBinding? = null

    private var langListAdapter: SettingLangAdapter? = null
    var selectPosition = 0
    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language)


        langListAdapter = SettingLangAdapter(R.layout.item_list_setting_lang)
        binding?.rvLang?.layoutManager = LinearLayoutManager(mContext)

        binding?.rvLang?.layoutManager = LinearLayoutManager(mContext)
        binding?.rvLang?.addItemDecoration(SimpleDividerItemDecoration(mContext))
        binding?.rvLang?.itemAnimator = null
        binding?.rvLang?.adapter = langListAdapter


        val mLocals = RateAndLocalManager.LocalKind.values()
        val mSelectLocal = RateAndLocalManager.getInstance(mContext).curLocalLanguageKind
        mLocals.forEachIndexed { index, localKind ->
            if (mSelectLocal?.code?.equals(localKind.code) == true) {
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
                val get = mLocals[selectPosition]
                changeLange(get.code)
            } catch (e: Exception) {
            }
        }
    }

    private fun changeLange(lang: String) {
        RateAndLocalManager.getInstance(mContext).changeLocalLanguageKind(lang)
        MMKVManager.getInstance().mmkv().encode(
            Constants.CURRENT_LANGUAGE,
            RateAndLocalManager.getInstance(mContext).curLocalLanguageKind.code
        )
        LocalManageUtil.setApplicationLanguage(this)
        binding?.tvSave?.postDelayed(300) {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}