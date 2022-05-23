package com.linktech.saihub.ui.activity.login

import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityResetBinding
import com.linktech.saihub.db.utils.WalletDaoUtils
import com.linktech.saihub.manager.AES
import com.linktech.saihub.manager.MMKVManager
import com.linktech.saihub.ui.dialog.NomalSuccessFailDialog
import com.linktech.saihub.util.MD5Utils
import com.linktech.saihub.util.system.setVisible
import com.linktech.saihub.view.ed.OnInputListener
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * Created by tromo on 2022/2/18.
 */
@Route(path = ARouterUrl.WAL_WALLET_RESET_ACTIVITY_PATH)
class ResetActivity : BaseActivity() {

    private var binding: ActivityResetBinding? = null

    override fun translucentStatusBar(): Boolean = true

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset)
        binding?.apply {

            tvCancel.onClick(Constants.CLICK_INTERVAL) {
                finish()
            }
            binding?.etPassword?.addTextChangedListener {
                binding?.tvErrorHint?.setVisible(false)
            }
            binding?.etPasswordRepeat?.addTextChangedListener {
                binding?.tvErrorHint?.setVisible(false)
            }

            binding?.etPassword?.setOnInputListener(object : OnInputListener() {
                override fun onInputFinished(content: String?) {
                    setBtEnable()
                }
            })
            binding?.etPasswordRepeat?.setOnInputListener(object : OnInputListener() {
                override fun onInputFinished(content: String?) {
                    setBtEnable()
                }
            })
            binding?.etWalletPassword?.etSwitch?.addTextChangedListener {
                binding?.etWalletPassword?.resetStatus()
                setBtEnable()
            }
            btnConfirm.onClick(Constants.CLICK_INTERVAL) {
                resetPwd(binding?.etWalletPassword?.etSwitch?.text.toString())

            }


        }
    }

    private fun setBtEnable() {
        binding?.btnConfirm?.isEnabled = binding?.etPassword?.text?.length == 6 &&
                binding?.etPasswordRepeat?.text?.length == 6 &&
                binding?.etWalletPassword?.etSwitch?.text.toString().trim().isNotEmpty()
    }

    private fun resetPwd(pwdInputStr: String) {
        lifecycleScope.launch {
            val isWalletPwd = withContext(Dispatchers.IO) {
                val loadAll = WalletDaoUtils.loadAll()
                val encryptStr = AES.encrypt(pwdInputStr)
                var temp = false
                loadAll.forEach { walletItem ->
                    if (encryptStr.equals(walletItem.password)) {
                        temp = true
                    }
                }
                temp
            }

            if (!isWalletPwd) {
                binding?.etWalletPassword?.setErrorTip(getString(R.string.wallet_pwd_error))
                return@launch
            }
            val pwdStr1 = binding?.etPassword?.text?.toString()
            val pwdStr2 = binding?.etPasswordRepeat?.text?.toString()
            if (pwdStr1.equals(pwdStr2).not()) {
                binding?.tvErrorHint?.setVisible(true)
                return@launch
            }

            MMKVManager.getInstance().mmkv().encode(Constants.VERIFY_PWD_LOCAL, MD5Utils.digest(pwdStr1))

            val dialog = NomalSuccessFailDialog(R.mipmap.icon_dialog_uccess, getString(R.string.complete), getString(R.string.ok))
            dialog.confirmEvent = {
                finish()
            }
            dialog.show(supportFragmentManager, "")
        }
    }

    override fun onData() {
        super.onData()
    }
}