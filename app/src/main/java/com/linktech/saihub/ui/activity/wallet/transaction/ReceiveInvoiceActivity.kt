package com.linktech.saihub.ui.activity.wallet.transaction

import `in`.xiandan.countdowntimer.CountDownTimerSupport
import `in`.xiandan.countdowntimer.OnCountDownTimerListener
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityReceiveInvoiceBinding
import com.linktech.saihub.entity.lightning.PendingInvoiceEntity
import com.linktech.saihub.qrcode.DuQRCodeEncoder
import com.linktech.saihub.ui.dialog.wallet.ShareDialog
import com.linktech.saihub.util.CommonUtil
import com.linktech.saihub.util.DateUtils
import com.linktech.saihub.util.PixelUtils
import com.linktech.saihub.util.StringUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 *  invoice详情
 * Created by tromo on 2022/6/22.
 */
@Route(path = ARouterUrl.WAL_LN_WALLET_INVOICE_DETAIL_ACTIVITY_PATH)
class ReceiveInvoiceActivity : BaseActivity() {

    private var binding: ActivityReceiveInvoiceBinding? = null

    private var receiveAddress: String? = null

    private var mTimer: CountDownTimerSupport? = null

    override fun translucentStatusBar(): Boolean = true

    private val pendingInvoiceEntity: PendingInvoiceEntity? by lazy {
        intent.getParcelableExtra(StringConstants.INVOICE_DATA)
    }

    override fun onInit() {
        super.onInit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receive_invoice)
        binding?.apply {
            receiveAddress = pendingInvoiceEntity?.payReq
            receiveAddress?.let { setAddressData(it) }

            tbReceive.setRightOnClickListener {
                ARouter.getInstance().build(ARouterUrl.WAL_SAIHUB_WALLET_MAIN_ACTIVITY_PATH)
                    .navigation()
            }

            if (TextUtils.isEmpty(pendingInvoiceEntity?.memo)) {
                clMemo.setVisible(false)
            } else {
                tvMemo.text = pendingInvoiceEntity?.memo
            }

            tvAmount.text = pendingInvoiceEntity?.value.toString()

            startTimer()

            llAddress.onClick(Constants.CLICK_INTERVAL_500) {
                CommonUtil.copyText(
                    this@ReceiveInvoiceActivity, receiveAddress,
                    getString(R.string.content_copy_success)
                )
            }

            btnShare.onClick(Constants.CLICK_INTERVAL) {
                val shareDialog = ShareDialog()
                val bundle = Bundle()
                bundle.putString(StringConstants.COIN, getString(R.string.ln_invoice))
                bundle.putString(StringConstants.ADDRESS, receiveAddress)
                shareDialog.arguments = bundle
                shareDialog.showNow(supportFragmentManager, "")
            }
        }
    }

    private fun startTimer() {
        val millisInFuture =
            (pendingInvoiceEntity?.timestamp!! + pendingInvoiceEntity?.expireTime!!) * 1000 - System.currentTimeMillis()
        //总时长 间隔时间
        mTimer = CountDownTimerSupport(millisInFuture, 1000)
        mTimer?.setOnCountDownTimerListener(object : OnCountDownTimerListener {
            override fun onTick(millisUntilFinished: Long) {
                // 倒计时间隔
                binding?.tvTime?.text =
                    DateUtils.getCountDownTimeFormat(millisUntilFinished)
            }

            override fun onFinish() {
                // 倒计时结束
                binding?.tvTime?.text = getString(R.string.out_of_date)
            }

            override fun onCancel() {
                // 倒计时手动停止
            }
        })
        mTimer?.start()
    }

    private fun setAddressData(address: String) {
        loadQrcode(address)
        binding?.tvAddress?.text = StringUtils.formatInvoice(address)
    }

    override fun onData() {
        super.onData()
//        binding?.tvAddress?.let { dealMiddleNoEffect(it) }
    }

    private fun dealMiddleNoEffect(tv: TextView, lines: Int = 1) {
        tv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //处理判断大于多少行时进行改变
                if (tv.viewTreeObserver.isAlive && tv.lineCount > lines) {
                    val lineEnd = tv.layout.getLineEnd(lines)
                    val allText = tv.text
                    val text = lineEnd.let {
                        val i = (it - 3) / 3
                        "${allText.substring(0, i)}...${
                            allText.substring(
                                allText.length - i + 3,
                                allText.length
                            )
                        }"
                    }
                    tv.text = text
                }
                if (tv.viewTreeObserver.isAlive) {
                    //回调过来之后直接移除监听
                    tv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

    }

    private fun loadQrcode(address: String) {
        lifecycleScope.launch {
            val downQrcode = withContext(Dispatchers.IO) {
                DuQRCodeEncoder.syncEncodeQRCode(
                    address,
                    PixelUtils.dp2px(193F),
                    mContext?.resources?.getColor(R.color.black)!!,
                    mContext?.resources?.getColor(R.color.white)!!,
                    null
                )
                //CodeUtils.createQRCode(addressKey, PixelUtils.dp2px(193F))
            }
            binding?.ivQrcode?.setImageBitmap(downQrcode)
            binding?.ivQrcode?.setPadding(0, 0, 0, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.stop()
    }
}