package com.linktech.saihub.view.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linktech.saihub.R;


public class PollEmptyView extends FrameLayout {
    private Context mContext;
    private ImageView ivEmptyImage;
    private TextView tvEmpty;
    private Button btnAddPair;

    public PollEmptyView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public PollEmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PollEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PollEmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        mContext = context;
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_empty_poll, this, true);
        inflate.findViewById(R.id.tv_add_poll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addListener!=null) {
                    addListener.addPoll();
                }
            }
        });
        setBackgroundResource(R.color.transparent);
    }


    public void setAddListener(AddListener addListener) {
        this.addListener = addListener;
    }

    private AddListener addListener;

    public interface AddListener {
        void addPoll();
    }


}
