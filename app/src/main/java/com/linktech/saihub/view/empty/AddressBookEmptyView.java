package com.linktech.saihub.view.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linktech.saihub.R;


public class AddressBookEmptyView extends FrameLayout {
    private Context mContext;

    public AddressBookEmptyView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public AddressBookEmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AddressBookEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AddressBookEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        mContext = context;
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_empty_address_book, this, true);
        inflate.findViewById(R.id.ll_base).setOnClickListener(v -> {
            if (addListener!=null) {
                addListener.addAddress();
            }
        });
        setBackgroundResource(R.color.transparent);
    }


    public void setAddListener(AddListener addListener) {
        this.addListener = addListener;
    }

    private AddListener addListener;

    public interface AddListener {
        void addAddress();
    }


}
