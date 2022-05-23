package com.linktech.saihub.ui.activity.poll

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
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
import com.linktech.saihub.databinding.ActivityPoolWebBinding
import com.linktech.saihub.db.bean.PollBean
import com.linktech.saihub.util.LogUtils
import com.linktech.saihub.util.RegexUtils
import com.qmuiteam.qmui.kotlin.onClick

@Route(path = ARouterUrl.WAL_POOL_WEB_URL_ACTIVITY_PATH)
class PoolWebActivity : BaseActivity() {

    private var binding: ActivityPoolWebBinding? = null
    private var mBnetworkError = false
    private var mUrl: String? = null
    private var mTitleStr: String? = null

    var pollBean: PollBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        isWhite = false
        super.onCreate(savedInstanceState)
    }

    override fun onInit() {
        super.onInit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pool_web)

        binding?.lvLoading?.imageAssetsFolder = "images/"
        showUiForType(STATUS_LOADING)

        mUrl = intent.getStringExtra(StringConstants.KEY_URL)
        mTitleStr = intent.getStringExtra(StringConstants.TITLE)
        pollBean = intent.getSerializableExtra(StringConstants.POLL_DATA) as PollBean?


        initWeb()
        initView()
    }

    private fun initView() {
        pollBean?.let {
            binding?.tvName?.text = pollBean?.pollName
            if (pollBean?.pollUrl?.length!! > 10) {
                binding?.tvUrl?.text = "...${
                    pollBean!!.pollUrl.slice(
                        IntRange(
                            pollBean?.pollUrl!!.length - 10,
                            pollBean?.pollUrl!!.length - 1
                        )
                    )
                }"
            } else {
                binding?.tvUrl?.text = pollBean!!.pollUrl
            }
        }
        binding?.topbar?.setTitle(mTitleStr)
        binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)?.visibility = View.GONE

        binding?.topbar?.setLeftOnClickListener {
            if (binding?.webFilechooser?.canGoBack() == true) binding?.webFilechooser?.goBack() else finish()
        }
        //关闭
        binding?.topbar?.findViewById<TextView>(R.id.title_bar_left_text)
            ?.onClick(Constants.CLICK_INTERVAL) {
                finish()
            }

        binding?.llNetworkError?.onClick(Constants.CLICK_INTERVAL) {
            showUiForType(STATUS_LOADING)
            loadUrL()
        }

        binding?.srlBase?.setOnRefreshListener {
            showUiForType(STATUS_LOADING, false)
            loadUrL()
        }
    }


    companion object {
        const val STATUS_SUCCESS = 0
        const val STATUS_LOADING = 1
        const val STATUS_ERROR = 2
        const val STATUS_FINISH = 3
    }

    private fun showUiForType(statusUi: Int) {
        showUiForType(statusUi, true)
    }

    private fun showUiForType(statusUi: Int, isShowLoading: Boolean) {
        when (statusUi) {
            STATUS_SUCCESS -> {
                binding?.llNetworkError?.visibility = View.GONE
                binding?.lvLoading?.visibility = View.GONE
//                binding?.viewLoading?.visibility = View.GONE
                binding?.webFilechooser?.visibility = View.VISIBLE
                binding?.lvLoading?.pauseAnimation()
                binding?.srlBase?.finishRefresh()
            }
            STATUS_LOADING -> {
                binding?.llNetworkError?.visibility = View.GONE
                binding?.lvLoading?.visibility = if (isShowLoading) View.VISIBLE else View.GONE
//                binding?.viewLoading?.visibility = View.VISIBLE
                binding?.webFilechooser?.visibility = View.GONE
                binding?.lvLoading?.playAnimation()
            }
            STATUS_ERROR -> {
                binding?.llNetworkError?.visibility = View.VISIBLE
                binding?.lvLoading?.visibility = View.GONE
//                binding?.viewLoading?.visibility = View.GONE
                binding?.webFilechooser?.visibility = View.GONE
                binding?.lvLoading?.pauseAnimation()
                binding?.srlBase?.finishRefresh()
            }
            STATUS_FINISH -> {
                binding?.progressBar1?.visibility = View.GONE
                binding?.lvLoading?.visibility = View.GONE
//                binding?.viewLoading?.visibility = View.GONE
                binding?.srlBase?.finishRefresh()
                binding?.lvLoading?.pauseAnimation()
            }
        }

    }

    private fun initWeb() {

        binding?.progressBar1?.max = 100
        binding?.progressBar1?.progressDrawable =
            this.resources.getDrawable(R.drawable.color_progressbar)

        binding?.webFilechooser?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(p0: WebView?, p1: String?): Boolean {
                return super.shouldOverrideUrlLoading(p0, p1)
            }

            override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
                super.onPageStarted(p0, p1, p2)
                mBnetworkError = false
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
                super.onPageFinished(view, url)
                if (!mBnetworkError) {
                    showUiForType(STATUS_SUCCESS)
                }
                /*   binding?.webFilechooser?.postDelayed({
                       if (mBnetworkError) {
                           showUiForType(STATUS_ERROR)
                       } else {
                           showUiForType(STATUS_SUCCESS)
                       }
                   }, 300)*/
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                mBnetworkError = true
                showUiForType(STATUS_ERROR)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                //6.0以上执行
                LogUtils.e("WebActivity---onReceivedError")
                if (request?.isForMainFrame == true) {
                    mBnetworkError = true
                    showUiForType(STATUS_ERROR)
                }
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

            override fun onReceivedHttpError(
                p0: WebView?,
                p1: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(p0, p1, errorResponse)
                var statusCode = errorResponse?.statusCode;
                if (404 == statusCode || 500 == statusCode) {
                    mBnetworkError = true
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

        }

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
            }

            override fun onReceivedTitle(p0: WebView?, p1: String?) {
                super.onReceivedTitle(p0, p1)
                LogUtils.e("WebActivity---onReceivedTitle")
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (title.contains("404") ||
                        title.contains("500") ||
                        title.contains("Error") ||
                        title.contains("找不到网页") ||
                        title.contains("网页无法打开")
                    ) {
                        showUiForType(STATUS_ERROR)
                    }
                }
                if (TextUtils.isEmpty(mTitleStr)) {
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//少于4.4（不包括4.4）用这个
            webSetting?.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
        }
        //适应屏幕，大于等于4.4用这个
        webSetting?.useWideViewPort = true
        webSetting?.setSupportZoom(true)
        webSetting?.loadWithOverviewMode = true

        webSetting?.setAppCacheMaxSize(Long.MAX_VALUE)
        webSetting?.setAppCachePath(getDir("appcache", 0).path)
        webSetting?.databasePath = getDir("databases", 0).path
        webSetting?.setGeolocationDatabasePath(getDir("geolocation", 0).path)
        webSetting?.pluginState = WebSettings.PluginState.ON_DEMAND
        val time = System.currentTimeMillis()
        CookieSyncManager.createInstance(this)
        CookieSyncManager.getInstance().sync()
        loadUrL()
    }

    private fun loadUrL() {
        if (mUrl == null || (!RegexUtils.checkURL(mUrl) && !mUrl!!.startsWith("www."))) {
            showUiForType(STATUS_ERROR)
            return
        }
        binding?.webFilechooser?.loadUrl(mUrl.toString())
    }


    override fun onDestroy() {
        binding?.webFilechooser?.destroy()
        super.onDestroy()
    }
}