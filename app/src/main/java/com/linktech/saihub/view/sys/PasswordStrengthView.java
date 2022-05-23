package com.linktech.saihub.view.sys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linktech.saihub.R;
import com.linktech.saihub.app.Constants;


public class PasswordStrengthView extends RelativeLayout {

    private TextView tvStrength;
    private View view0;
    private View view1;
    private View view2;
    private Context mContext;
    private View rootView;

    public PasswordStrengthView(Context context) {
        super(context);
        init(context, null);
    }

    public PasswordStrengthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PasswordStrengthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        mContext = context;
        View finder = LayoutInflater.from(context).inflate(R.layout.strength_layout, this, true);
        setBackgroundResource(R.color.transparent);

        rootView = finder.findViewById(R.id.rlRela);
        rootView.setOnClickListener(null);

        tvStrength = finder.findViewById(R.id.tv_strength);
        view0 = finder.findViewById(R.id.view_0);
        view1 = finder.findViewById(R.id.view_1);
        view2 = finder.findViewById(R.id.view_2);

    }

    public void setBgColor(int bgColor) {
        rootView.setBackgroundColor(bgColor);
    }

    public void setStrengthMode(int mode) {
        switch (mode) {
            case Constants.Level_1:
                tvStrength.setText(mContext.getString(R.string.pwd_weak));
                tvStrength.setTextColor(mContext.getResources().getColor(R.color.color_FFFF3750));
                view0.setBackground(mContext.getDrawable(R.drawable.shape_strength_weak));
                view1.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                view2.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                break;
            case Constants.Level_2:
                tvStrength.setText(mContext.getString(R.string.pwd_medium));
                tvStrength.setTextColor(mContext.getResources().getColor(R.color.color_FF4670E6));
                view0.setBackground(mContext.getDrawable(R.drawable.shape_strength_medium));
                view1.setBackground(mContext.getDrawable(R.drawable.shape_strength_medium));
                view2.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                break;
            case Constants.Level_3:
                tvStrength.setText(mContext.getString(R.string.pwd_strong));
                tvStrength.setTextColor(mContext.getResources().getColor(R.color.color_FF00C873));
                view0.setBackground(mContext.getDrawable(R.drawable.shape_strength_strong));
                view1.setBackground(mContext.getDrawable(R.drawable.shape_strength_strong));
                view2.setBackground(mContext.getDrawable(R.drawable.shape_strength_strong));
                break;

            default:
                tvStrength.setText("");
                view0.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                view1.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                view2.setBackground(mContext.getDrawable(R.drawable.shape_strength));
                break;
        }
    }

}
