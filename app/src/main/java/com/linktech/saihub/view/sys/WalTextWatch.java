package com.linktech.saihub.view.sys;

import android.text.Editable;
import android.text.TextWatcher;


public abstract class WalTextWatch implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String s = editable.toString();
        onTextChanged(s);
    }

    protected abstract void onTextChanged(String s);
}
