package com.linktech.saihub.view.sys

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.linktech.saihub.R
import com.linktech.saihub.util.system.setVisible
import com.qmuiteam.qmui.kotlin.onClick

/**
 * 展示或隐藏明文的edittext
 * Created by tromo on 2021/11/17.
 */
class SwitchEditText(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    private var isShow = false
    var etSwitch: EditText? = null
    var viewLine: View? = null
    private var tvTip: TextView? = null
    private var tvDesc: TextView? = null
    var inflate: View? = null
        private set
    var oldLength = 0

    init {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_switch_edit_text, this)
        etSwitch = inflate?.findViewById(R.id.et_switch)
        tvDesc = inflate?.findViewById(R.id.tv_desc)
        val btnClear = inflate?.findViewById<ImageButton>(R.id.btn_clear)
        val btnSwitch = inflate?.findViewById<ImageButton>(R.id.btn_switch)
        viewLine = inflate?.findViewById(R.id.view_line)
        tvTip = inflate?.findViewById(R.id.tv_tip)

        val ta: TypedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.SwitchEditText
        )
        val hint = ta.getString(R.styleable.SwitchEditText_switch_hint)
        val isShowDesc = ta.getBoolean(R.styleable.SwitchEditText_front_desc, false)
        val isPassword = ta.getBoolean(R.styleable.SwitchEditText_is_password, true)
        val textSize = ta.getFloat(R.styleable.SwitchEditText_et_text_size, 14f)
        val isShowLine = ta.getBoolean(R.styleable.SwitchEditText_is_show_line, true)
        val textColor = ta.getResourceId(R.styleable.SwitchEditText_et_text_color, R.color.black)
        ta.recycle()

        tvDesc?.setVisible(isShowDesc)
        etSwitch?.textSize = textSize
        etSwitch?.hint = hint
        etSwitch?.setTextColor(context.resources.getColor(textColor))
        btnSwitch?.setVisible(isPassword)
        viewLine?.setVisible(isShowLine)

        if (!isPassword)
            etSwitch?.transformationMethod = HideReturnsTransformationMethod.getInstance()

        etSwitch?.addTextChangedListener(object : WalTextWatch() {
            override fun onTextChanged(s: String?) {
                btnClear?.setVisible(!TextUtils.isEmpty(s))
                if (oldLength > 0 != s?.length!! > 0) {
                    setTextFont(s.isNotEmpty())
                }
                oldLength = s.length
                resetStatus()
            }

        })

        btnClear?.onClick {
            etSwitch?.setText("")
        }

        btnSwitch?.setOnClickListener {
            isShow = !isShow
            if (isShow) {
                etSwitch?.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnSwitch.setImageDrawable(context.getDrawable(R.mipmap.icon_eye_close))
            } else {
                etSwitch?.transformationMethod = PasswordTransformationMethod.getInstance()
                btnSwitch.setImageDrawable(context.getDrawable(R.mipmap.icon_eye_open))
            }
            etSwitch?.text?.length?.let { it1 -> etSwitch?.setSelection(it1) }
        }

    }

    fun setErrorTip(content: String) {
        viewLine?.setBackgroundColor(context.resources.getColor(R.color.color_FFFF3750))
        tvTip?.setVisible(true)
        tvTip?.text = content
    }

    fun setErrorTipLine() {
        viewLine?.setBackgroundColor(context.resources.getColor(R.color.color_FFFF3750))
    }

    fun setErrorOnlyTip(content: String) {
        tvTip?.setVisible(true)
        tvTip?.text = content
    }

    private fun setTextFont(isNotEmpty: Boolean) {
        if (isNotEmpty) {
            etSwitch?.setTypeface(
                Typeface.createFromAsset(
                    context.assets,
                    "fonts/montserrat_medium.ttf"
                )
            )
        } else {
            etSwitch?.setTypeface(
                Typeface.createFromAsset(
                    context.assets,
                    "fonts/montserrat_regular.ttf"
                )
            )
        }
    }


    fun resetStatus() {
        viewLine?.setBackgroundColor(context.resources.getColor(R.color.color_1F000000))
        tvTip?.setVisible(false)
    }

    fun setDesc(desc: String) {
        tvDesc?.text = desc
    }

    fun getText(): String {
        return if (TextUtils.isEmpty(etSwitch?.text.toString().trim())) {
            ""
        } else {
            etSwitch?.text.toString().trim()
        }

    }

    fun setContentText(content: String?) {
        etSwitch?.setText(content)
    }

    fun setHintText(content: String?) {
        etSwitch?.hint = content
    }
}