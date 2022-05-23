package com.linktech.saihub.view.ed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.errorprone.annotations.Var;


public class FontEditText extends AppCompatEditText implements TextWatcher {

    public FontEditText(final Context context) {
        super(context);
        init(context);
    }

    public FontEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FontEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(final Context context) {
        addTextChangedListener(this);
    }

    int oldLength = 0;

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if ((oldLength > 0) != (text.length() > 0)) {
            setTextFont(text.length() > 0);
        }
        oldLength = text.length();
    }

    private void setTextFont(boolean isNotEmpty) {
        if (isNotEmpty) {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/montserrat_medium.ttf"));
        } else {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/montserrat_regular.ttf"));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
