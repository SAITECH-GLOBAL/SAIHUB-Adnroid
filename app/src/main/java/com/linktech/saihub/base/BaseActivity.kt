package com.linktech.saihub.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.linktech.saihub.R
import com.linktech.saihub.ui.dialog.common.LoadingDialog
import com.linktech.saihub.ui.dialog.common.LoadingFullScreenDialog
import com.linktech.saihub.util.LocalManageUtil
import com.linktech.saihub.util.system.ActivitiesStack
import com.linktech.saihub.util.system.SystemBarTintManager
import java.util.*

/**
 * Created by tromo on 2018/8/28.
 */
abstract class BaseActivity : BaseWindowActivity() {
    protected var mContext: Context? = null
    private var progressbar: ProgressBar? = null
    protected var isWhite: Boolean = false

    private val loadingNormalDialog: LoadingDialog? by lazy {
        LoadingDialog()
    }

    val loadingFullScreenDialog: LoadingFullScreenDialog? by lazy {
        LoadingFullScreenDialog()
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        ARouter.getInstance().inject(this)
        initSystemBarTint()
        onInit()
        onData()
    }

    override fun moveTaskToBack(nonRoot: Boolean): Boolean {
//        return super.moveTaskToBack(nonRoot)
        return super.moveTaskToBack(true)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalManageUtil.attachBaseContext(newBase, ""))
    }


    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    /**
     * ???????????????
     */
    open fun onInit() {}

    /**
     * ???????????????
     */
    open fun onData() {}

    /**
     * ???????????????????????????????????????
     */
    open fun setStatusBarColor(): Int {
        return colorPrimary
    }

    /**
     * ???????????????????????????????????????????????????
     */
    open fun translucentStatusBar(): Boolean {
        return false
    }

    open fun getStatusBlackMode(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        val isStatusBlack = getStatusBlackMode()
        if (!isStatusBlack) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //?????????????????????????????????????????????
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.decorView.findViewById<View>(android.R.id.content).setPadding(0, 0, 0, 0)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //?????????????????????????????????????????????
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.decorView.findViewById<View>(android.R.id.content).setPadding(0, 0, 0, 0)
            }
        }
    }

    /**
     * ?????????????????????
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun initSystemBarTint() {
        val window = window
        if (translucentStatusBar()) {
            // ????????????????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
            return
        }
        // ??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0????????????????????????
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = setStatusBarColor()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4-5.0??????????????????????????????4.4???????????????????????????????????????????????????????????????
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            val tintManager = SystemBarTintManager(this)
            tintManager.isStatusBarTintEnabled = true
            tintManager.setStatusBarTintColor(setStatusBarColor())
        }

        if (isWhite) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.findViewById<View>(android.R.id.content).setPadding(0, 0, 0, 0)
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.findViewById<View>(android.R.id.content).setPadding(0, 0, 0, 0)
        }
    }

    /**
     * ???????????????
     *
     * @param isTransparent
     */
    @SuppressLint("ObsoleteSdkInt")
    open fun switchStatusBar(isTransparent: Boolean) {
        val window = window
        if (isTransparent) {
            // ????????????????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
            return
        }
        // ??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0????????????????????????
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = setStatusBarColor()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4-5.0??????????????????????????????4.4???????????????????????????????????????????????????????????????
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            val tintManager = SystemBarTintManager(this)
            tintManager.isStatusBarTintEnabled = true
            tintManager.setStatusBarTintColor(setStatusBarColor())
        }
    }

    /**
     * ???????????????
     */
    private val colorPrimary: Int
        get() = ContextCompat.getColor(this, R.color.white)

    /**
     * ??????progressbar
     */
    fun showLoading() {
        showLoading(0, 0)
    }

    /**
     * ??????progressbar
     *
     * @param topSpace    progressbar??????????????????
     * @param bottomSpace progressbar??????????????????
     */
    @SuppressLint("WrongConstant")
    fun showLoading(topSpace: Int, bottomSpace: Int) {
        if (loadingNormalDialog?.dialog?.isShowing == true) {
            return
        } else {
            if (loadingNormalDialog?.isAdded == false) {
                loadingNormalDialog?.showNow(supportFragmentManager, "")
            }
        }
    }

    fun showLoadingFullScreen() {
        if (loadingFullScreenDialog?.dialog?.isShowing == true) {
            return
        } else {
            if (loadingFullScreenDialog?.isAdded == false) {
                loadingFullScreenDialog?.showNow(supportFragmentManager, "")
            }
        }
    }

    /**
     * ??????progressbar
     */
    @SuppressLint("WrongConstant")
    fun hideLoading() {
        if (loadingNormalDialog?.dialog?.isShowing == true) {
            loadingNormalDialog?.dismiss()
        }
    }

    fun hideLoadingFullScreen() {
        if (loadingFullScreenDialog?.dialog?.isShowing == true) {
            loadingFullScreenDialog?.dismiss()
        }
    }


    @SuppressLint("WrongConstant")
    fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Activity??????
     *
     * @param cls
     */
    fun startActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
    }

    /**
     * ????????????????????????
     *
     * @param
     */
    fun dismissSoftKeyboard() {
        val view = this.window.peekDecorView()
        if (view != null) {
            @SuppressLint("WrongConstant") val inputmanger =
                this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputmanger.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * view?????????????????????????????????
     *
     * @param view
     */
    fun getSoftInput(view: View?) {
        @SuppressLint("WrongConstant") val imm =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus;
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v?.windowToken);
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * ??????EditText???????????????????????????????????????????????????????????????????????????????????????????????????EditText??????????????????
     *
     * @param v
     * @param event
     * @return
     */
    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        // ??????????????????EditText?????????????????????????????????????????????????????????????????????EditText????????????????????????????????????????????????
        return false
    }

    /**
     * ??????InputMethodManager??????????????????
     *
     * @param token
     */
    private fun hideKeyboard(token: IBinder?) {
        if (token != null) {
            @SuppressLint("WrongConstant") val im =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun setHintEt(et: EditText, hasFocus: Boolean) {
        val hint: String
        if (hasFocus) {
            hint = et.hint.toString()
            et.tag = hint
            et.hint = ""
        } else {
            hint = et.tag.toString()
            et.hint = hint
        }
    }

    fun showSoftInputFromWindow(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 ->                 //??????????????????
                if (grantResults.isNotEmpty()) {
                    val deniedPermissions: MutableList<String> = ArrayList()
                    var i = 0
                    while (i < grantResults.size) {
                        val grantResult = grantResults[i]
                        val permission = permissions[i]
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedPermissions.add(permission)
                        }
                        i++
                    }
                    //??????????????????
                    if (deniedPermissions.isEmpty()) {
                        mListener!!.onGranted()
                    } else {
                        mListener!!.onDenied(deniedPermissions)
                    }
                }
            else -> {
            }
        }
    }

    interface PermissionListener {
        //???????????????
        fun onGranted()

        //??????
        fun onDenied(deniedPermission: List<String>?)
    }

    override fun onDestroy() {
        super.onDestroy()
        mListener = null
    }


    companion object {
        private var mListener: PermissionListener? = null
        fun requestRuntimePermission(permissions: Array<String>, listener: PermissionListener?) {
            try {
                mListener = listener
                val permissionList: MutableList<String> = ArrayList()
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(
                            ActivitiesStack.getInstance().currentActivity,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionList.add(permission)
                    }
                }
                if (permissionList.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        ActivitiesStack.getInstance().currentActivity,
                        permissionList.toTypedArray(),
                        1
                    )
                } else {
                    //????????????
                    mListener!!.onGranted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * ?????? app ????????????????????????????????????
     */
    override fun getResources(): Resources {
        val res: Resources? = super.getResources()
        if (res != null) {
            val config: Configuration? = res.configuration
            if (config?.fontScale != 1.0f) {
                config?.fontScale = 1.0f
                createConfigurationContext(config!!)
                res.updateConfiguration(config, res.displayMetrics)
            }
        }
        return res ?: super.getResources()
    }
}