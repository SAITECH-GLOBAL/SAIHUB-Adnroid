package com.linktech.saihub.ui.activity

import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.webkit.*
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
import com.linktech.saihub.R
import com.linktech.saihub.app.ARouterUrl
import com.linktech.saihub.app.Constants
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.base.BaseActivity
import com.linktech.saihub.databinding.ActivityWebBinding
import com.linktech.saihub.util.LogUtils
import com.qmuiteam.qmui.kotlin.onClick

@Route(path = ARouterUrl.WAL_WEB_URL_ACTIVITY_PATH)
class WebActivity : BaseActivity() {

    private var binding: ActivityWebBinding? = null
    private var mBnetworkError = false
    private var mUrl: String? = null
    private var mTitleStr: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        isWhite = false
        super.onCreate(savedInstanceState)
    }

    override fun onInit() {
        super.onInit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web)

        binding?.lvLoading?.imageAssetsFolder = "images/"
        binding?.lvLoading?.playAnimation()

        mUrl = intent.getStringExtra(StringConstants.KEY_URL)
        mTitleStr = intent.getStringExtra(StringConstants.TITLE)


        initWeb()

        binding?.topbar?.setTitle(mTitleStr)
        binding?.topbar?.setLeftText(getString(R.string.close))
        binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)?.visibility = View.GONE

        binding?.topbar?.setLeftOnClickListener {
            if (binding?.webFilechooser?.canGoBack() == true) binding?.webFilechooser?.goBack() else finish()
        }
        binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)
            ?.onClick(Constants.CLICK_INTERVAL) {
                finish()
            }
        binding?.llNetworkError?.onClick(Constants.CLICK_INTERVAL) {
            binding?.llNetworkError?.visibility = View.GONE
            binding?.webFilechooser?.loadUrl(mUrl.toString())
        }
    }

    private fun initWeb() {

        binding?.progressBar1?.max = 100
        binding?.progressBar1?.progressDrawable =
            this.resources.getDrawable(R.drawable.color_progressbar)



        binding?.webFilechooser?.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                LogUtils.e("WebActivity---onPageFinished")
                val pageSize: Int = view.copyBackForwardList().getCurrentIndex()
                if (pageSize > 0) {
                    binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)?.visibility =
                        View.VISIBLE
                } else {
                    binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)?.visibility =
                        View.GONE
                }
                binding?.progressBar1?.setVisibility(View.GONE)
                super.onPageFinished(view, url)
                if (!mBnetworkError) {
                    binding?.llNetworkError?.setVisibility(View.GONE)
                } else {
                    Handler().postDelayed({
//                        binding?.llNetworkError?.setVisibility(View.VISIBLE)
                    }, 500)
                }

            }

            override fun onReceivedError(
                p0: WebView?,
                p1: WebResourceRequest?,
                p2: WebResourceError?
            ) {
                super.onReceivedError(p0, p1, p2)
                LogUtils.e("WebActivity---onReceivedError")
//                mBnetworkError = true
//                binding?.llNetworkError?.visibility = View.VISIBLE
//                binding?.webFilechooser?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//                binding?.webFilechooser?.setVisibility(View.VISIBLE);
            }

            override fun onReceivedSslError(
                webView: WebView?,
                sslErrorHandler: SslErrorHandler?,
                sslError: SslError?
            ) {
                super.onReceivedSslError(webView, sslErrorHandler, sslError)
                if (sslError?.getPrimaryError() == android.net.http.SslError.SSL_INVALID) {// 校验过程遇到了bug
                    //这里直接忽略ssl证书的检测出错问题，选择继续执行页面
                    sslErrorHandler?.proceed();
                } else {
                    //不是证书问题时候则停止执行加载页面
                    sslErrorHandler?.cancel();
                }

            }

        })

        binding?.webFilechooser?.webChromeClient = object : WebChromeClient() {
            override fun onJsConfirm(
                arg0: WebView, arg1: String, arg2: String,
                arg3: JsResult
            ): Boolean {
                return super.onJsConfirm(arg0, arg1, arg2, arg3)
            }

            override fun onProgressChanged(p0: WebView?, p1: Int) {
                super.onProgressChanged(p0, p1)
                LogUtils.e("WebActivity---onProgressChanged")
                binding?.progressBar1?.progress = p1
                if (p1 >= 100) {
                    binding?.progressBar1?.visibility = View.GONE
                    binding?.lvLoading?.visibility = View.GONE
                    binding?.lvLoading?.pauseAnimation()
                }
            }

            override fun onReceivedTitle(p0: WebView?, p1: String?) {
                super.onReceivedTitle(p0, p1)
                LogUtils.e("WebActivity---onReceivedTitle")
                if (mTitleStr == null) {
                    binding?.topbar?.setTitle(p1)
                }
            }

        }

        val webSetting: WebSettings? = binding?.webFilechooser?.getSettings()
        webSetting?.allowFileAccess = true
        webSetting?.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSetting?.setSupportZoom(true)
        webSetting?.builtInZoomControls = true
        webSetting?.useWideViewPort = true
        webSetting?.setSupportMultipleWindows(false)
        webSetting?.setAppCacheEnabled(true)
        webSetting?.domStorageEnabled = true
        webSetting?.javaScriptEnabled = true
        webSetting?.setGeolocationEnabled(true)
        webSetting?.setAppCacheMaxSize(Long.MAX_VALUE)
        webSetting?.setAppCachePath(getDir("appcache", 0).path)
        webSetting?.databasePath = getDir("databases", 0).path
        webSetting?.setGeolocationDatabasePath(getDir("geolocation", 0).path)
        webSetting?.pluginState = WebSettings.PluginState.ON_DEMAND
        val time = System.currentTimeMillis()
        if (mUrl == null) {
            binding?.webFilechooser?.loadUrl(mUrl.toString())
        } else {
            binding?.webFilechooser?.loadUrl(mUrl.toString())
        }
        CookieSyncManager.createInstance(this)
        CookieSyncManager.getInstance().sync()
    }


    override fun onDestroy() {
        binding?.webFilechooser?.destroy()
        super.onDestroy()

    }
}