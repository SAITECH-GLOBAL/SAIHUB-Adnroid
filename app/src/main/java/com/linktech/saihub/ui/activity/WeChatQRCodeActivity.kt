package com.linktech.saihub.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.king.mlkit.vision.camera.AnalyzeResult
import com.king.mlkit.vision.camera.CameraScan
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity
import com.linktech.saihub.R
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.manager.wallet.btc.Base58Check
import com.linktech.saihub.util.ToastUtils
import com.linktech.saihub.util.screenshot.BitmapUtils
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick
import com.sparrowwallet.hummingbird.BC32
import com.sparrowwallet.hummingbird.Bytewords
import com.sparrowwallet.hummingbird.ResultType
import com.sparrowwallet.hummingbird.URDecoder
import com.sparrowwallet.hummingbird.registry.CryptoAccount
import com.sparrowwallet.hummingbird.registry.RegistryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Created by tromo on 2022/3/10.
 */
class WeChatQRCodeActivity : WeChatCameraScanActivity() {

    companion object {
        const val TAG = "WeChatQRCodeActivity"

        const val TYPE_IMPORT = 1

        const val REQUEST_CODE_ALBUM = 0x1515
    }

    private val decoder: URDecoder? by lazy {
        URDecoder()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_zxing_lite
    }

    override fun initUI() {
        super.initUI()
        val type = intent.getIntExtra(StringConstants.LOAD_TYPE, 0)
        val llAlbum = findViewById<LinearLayout>(R.id.ll_album)
        llAlbum.setVisible(type == 1)

        llAlbum.onClick(Constants.CLICK_INTERVAL) {
            openAlbum()
        }
    }

    private fun openAlbum() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = "android.intent.action.GET_CONTENT"
        intent.addCategory("android.intent.category.OPENABLE")
        startActivityForResult(intent, REQUEST_CODE_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ALBUM -> {
                    analyzeImage(data?.data!!)
                }
            }
        }
    }

    private fun analyzeImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val bitmap = BitmapUtils.decodeUri(this@WeChatQRCodeActivity, uri, 400, 400)
                WeChatQRCodeDetector.detectAndDecode(bitmap)
            }.onSuccess {
                val intent = Intent()
                intent.putExtra(CameraScan.SCAN_RESULT, if (it.isNotEmpty()) it[0] else "")
                setResult(RESULT_OK, intent)
                finish()
            }.onFailure {
                val intent = Intent()
                intent.putExtra(CameraScan.SCAN_RESULT, "")
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }


    override fun onScanResultCallback(result: AnalyzeResult<List<String>>) {
        if (result.result.isNotEmpty()) {
            //停止分析
            cameraScan.setAnalyzeImage(false)
            var content: String
            runCatching {
                if (result.result[0].uppercase(Locale.ROOT).startsWith("UR")) {
                    if (decoder?.result == null) {
                        for (item in result.result) {
                            decoder?.receivePart(item)
                        }
                        cameraScan.setAnalyzeImage(true)
                    } else {
                        val urResult: URDecoder.Result = decoder!!.result
                        when (urResult.type) {
                            ResultType.SUCCESS -> {
                                val intent = Intent()

                                content = when (urResult.ur.registryType) {
                                    RegistryType.BYTES -> {
                                        intent.putExtra(StringConstants.RESULT_TYPE,
                                            RegistryType.BYTES.type)
                                        val bytes = urResult.ur.toBytes()
                                        String(bytes, StandardCharsets.UTF_8)
                                    }
                                    RegistryType.CRYPTO_PSBT -> {
                                        intent.putExtra(StringConstants.RESULT_TYPE,
                                            RegistryType.CRYPTO_PSBT.type)
                                        urResult.ur.toString()
//                                        String(bytes, StandardCharsets.UTF_8)
                                        //                                    val bytes = urResult.ur.toBytes()
                                        //                                    String(bytes, StandardCharsets.UTF_8)
//                                        val psbt = (urResult.ur.decodeFromRegistry() as? CryptoPSBT)?.psbt!!
//                                        String(psbt, StandardCharsets.UTF_8)
                                    }
                                    RegistryType.CRYPTO_ACCOUNT -> {
                                        intent.putExtra(StringConstants.RESULT_TYPE,
                                            RegistryType.CRYPTO_ACCOUNT.type)
                                        urResult.ur.toString()
                                    }
                                    else -> {
                                        result.result[0]
                                    }
                                }

                                intent.putExtra(CameraScan.SCAN_RESULT, content)
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                            else -> {}
                        }
                    }
                } else {
                    content = result.result[0]
                    val intent = Intent()
                    intent.putExtra(CameraScan.SCAN_RESULT, content)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }.onFailure {

            }

        }
    }

}