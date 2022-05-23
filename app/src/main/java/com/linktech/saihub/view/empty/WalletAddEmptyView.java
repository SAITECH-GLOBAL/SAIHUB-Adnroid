package com.linktech.saihub.view.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linktech.saihub.R;


public class WalletAddEmptyView extends FrameLayout {
    private Context mContext;

    public WalletAddEmptyView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public WalletAddEmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WalletAddEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WalletAddEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        mContext = context;
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_empty_wallet_list, this, true);
        inflate.findViewById(R.id.ll_base).setOnClickListener(v -> {
            if (addListener!=null) {
                addListener.addWallet();
            }
        });
        setBackgroundResource(R.color.transparent);
    }


    public void setAddListener(AddListener addListener) {
        this.addListener = addListener;
    }

    private AddListener addListener;

    public interface AddListener {
        void addWallet();
    }


}
