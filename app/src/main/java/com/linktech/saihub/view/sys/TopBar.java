package com.linktech.saihub.view.sys;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linktech.saihub.R;
import com.linktech.saihub.util.PixelUtils;


public class TopBar extends RelativeLayout {
    private static final float TOPBAR_DEFAULT_HEIGHT = 56f;
    private String title;

    private String leftText;

    private String rightText;

    private int leftVisiblity;

    private int rightVisiblity;

    private RelativeLayout leftLayout;

    private LinearLayout rightLayout;

    private ImageView imgLeft;

    private TextView tvLeft;

    private TextView tvTitle;

    private TextView tvRight;
    private TextView tvRight2;

    private ImageView imgRight;
    private ImageView imgTitleIcon;
    private LinearLayout titleLayout;
    private Drawable leftIconRes;
    private Drawable rightIconRes;
    private Drawable rightIconRes2;
    private int bgColor;
    private int dividerVisiblity;
    private View divider;
    private View redPointRight;
    private int titleColor;
    private int right_text_color;
    private Context mContext;
    private Drawable tilteIconRes;
    private ImageView imgRight2;
    private View rootView;

    public TopBar(Context context) {
        super(context);
        init(context, null);
    }

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setTitleWidth(int dp) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) tvTitle.getLayoutParams();
        linearParams.width = PixelUtils.dp2px(dp);
        tvTitle.setLayoutParams(linearParams);
    }


    private void init(final Context context, AttributeSet attrs) {
        mContext = context;
        View finder = LayoutInflater.from(context).inflate(R.layout.topbar_layout, this, true);
        setBackgroundResource(R.color.transparent);

        rootView = finder.findViewById(R.id.rlRela);
        rootView.setOnClickListener(null);
        titleLayout = finder.findViewById(R.id.title_layout);
        tvTitle = finder.findViewById(R.id.title_bar_text);
        imgTitleIcon = finder.findViewById(R.id.title_bar_icon);

        leftLayout = finder.findViewById(R.id.title_bar_left_layout);
        tvLeft = finder.findViewById(R.id.title_bar_left_text);
        imgLeft = finder.findViewById(R.id.title_bar_left_img);

        rightLayout = finder.findViewById(R.id.title_bar_right_layout);
        imgRight = finder.findViewById(R.id.title_bar_right_image);
        imgRight2 = finder.findViewById(R.id.title_bar_right_image2);
        tvRight = finder.findViewById(R.id.title_bar_right_text);
        tvRight2 = finder.findViewById(R.id.title_bar_right_text2);
        divider = findViewById(R.id.topbar_divider);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopBar);
            bgColor = ta.getColor(R.styleable.TopBar_bgcolor, context.getResources().getColor(R.color.white));
            titleColor = ta.getColor(R.styleable.TopBar_titlecolor, context.getResources().getColor(R.color.black));
            right_text_color = ta.getColor(R.styleable.TopBar_right_text_color, context.getResources().getColor(R.color.color_FF090E16));
            title = ta.getString(R.styleable.TopBar_title_text);
            leftText = ta.getString(R.styleable.TopBar_left_text);
            tilteIconRes = ta.getDrawable(R.styleable.TopBar_title_icon);
            leftIconRes = ta.getDrawable(R.styleable.TopBar_left_icon);
            leftVisiblity = ta.getInt(R.styleable.TopBar_left_visiblity, VISIBLE);
            rightVisiblity = ta.getInt(R.styleable.TopBar_right_visiblity, VISIBLE);
            dividerVisiblity = ta.getInt(R.styleable.TopBar_divider_visiblity, GONE);

            rightText = ta.getString(R.styleable.TopBar_right_text);
            rightIconRes = ta.getDrawable(R.styleable.TopBar_right_icon);
            rightIconRes2 = ta.getDrawable(R.styleable.TopBar_right_icon_next);

            ta.recycle();

            setBgColor(bgColor);

            if (tilteIconRes != null) {
                imgTitleIcon.setImageDrawable(tilteIconRes);
                imgTitleIcon.setVisibility(View.VISIBLE);
            } else {
                imgTitleIcon.setVisibility(View.GONE);
            }
            if (leftIconRes != null) {
                imgLeft.setImageDrawable(leftIconRes);
            }
            if (rightIconRes != null) {
                imgRight.setImageDrawable(rightIconRes);
                imgRight.setVisibility(View.VISIBLE);
            } else {
                imgRight.setVisibility(View.GONE);
            }
            if (rightIconRes2 != null) {
                imgRight2.setImageDrawable(rightIconRes2);
                imgRight2.setVisibility(View.VISIBLE);
            } else {
                imgRight2.setVisibility(View.GONE);
            }
            tvTitle.setTextColor(titleColor);
            tvRight.setTextColor(right_text_color);
            setTitle(title);
            setLeftText(leftText);
            setRightText(rightText);

            setLeftVisiblity(leftVisiblity);
            setRightVisiblity(rightVisiblity);

            divider.setVisibility(dividerVisiblity);
        }
    }

    public TextView getLeftTextView() {
        return tvLeft;
    }

    public ImageView getleftImgView() {
        return imgLeft;
    }

    public ImageView getRightImg() {
        return imgRight;
    }

    public ImageView getRightImg2() {
        return imgRight2;
    }

    public ImageView getTitleIcon() {
        return imgTitleIcon;
    }

    public TextView getTitleView() {
        return tvTitle;
    }

    public TextView getRightTextView() {
        return tvRight;
    }

    public TextView getRightTextView2() {
        return tvRight2;
    }

    public void setBgColor(int bgColor) {
        rootView.setBackgroundColor(bgColor);
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(VISIBLE);
            tvTitle.setText(title);
        } else {
            tvTitle.setVisibility(GONE);
        }
    }

    public void setTitleAppearance(int id) {
        tvTitle.setTextAppearance(mContext, id);
    }

    public void setTitleColor(int color) {
        tvTitle.setTextColor(color);
    }

    public void setTitle(SpannableString title) {
        tvTitle.setVisibility(VISIBLE);
        tvTitle.setText(title);
    }

    /**
     * 设置标题右边图标
     *
     * @param imgRes
     */
    public void setTitleRightDrawable(int imgRes) {
        imgTitleIcon.setVisibility(VISIBLE);
        imgTitleIcon.setImageResource(imgRes);
    }

    public void setLeftText(String leftText) {
        if (!TextUtils.isEmpty(leftText)) {
            tvLeft.setText(leftText);
            tvLeft.setVisibility(VISIBLE);
        } else {
            tvLeft.setVisibility(GONE);
        }
    }

    public void setLeftTextAndBackGround(String text, int style, int imRes) {
        if (!TextUtils.isEmpty(text)) {
            tvLeft.setText(text);
            tvLeft.setBackgroundResource(imRes);
            leftLayout.setVisibility(VISIBLE);
            //imgLeft.setVisibility(GONE);
            //setLeftOnClickListener(null);
            tvLeft.setVisibility(VISIBLE);
            tvLeft.setTextAppearance(mContext, style);
            tvLeft.setPadding(PixelUtils.dp2px(2), PixelUtils.dp2px(2), PixelUtils.dp2px(2), PixelUtils.dp2px(2));

            LayoutParams layoutParams = (LayoutParams) titleLayout.getLayoutParams();
            layoutParams.leftMargin = PixelUtils.dp2px(2);
            titleLayout.setLayoutParams(layoutParams);
        } else {
            tvLeft.setVisibility(GONE);
        }
    }

    public void setRightText(String rightText) {
        if (!TextUtils.isEmpty(rightText)) {
            tvRight.setVisibility(VISIBLE);
            tvRight.setText(rightText);
        } else {
            tvRight.setVisibility(GONE);
        }
    }

    public void setRightTextColor(int color) {
        if (tvRight != null) {
            tvRight.setTextColor(color);
        }
    }

    public void setRightTextAppearance(Context context, int style) {
        tvRight.setTextAppearance(context, style);
    }

    /**
     * 设置右边标题左右边距
     *
     * @param marginRight
     */
    public void setRightTextMargin(int marginLeft, int marginRight) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvRight.getLayoutParams();
        layoutParams.leftMargin = marginLeft;
        layoutParams.rightMargin = marginRight;
        tvRight.setLayoutParams(layoutParams);
    }

    public void setRightImg(int res) {
        if (res != -1) {
            imgRight.setImageResource(res);
            imgRight.setVisibility(View.VISIBLE);
        } else {
            imgRight.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题右边图标拓展
     *
     * @param res
     */
    public void setRightImg2(int res) {
        if (res != -1) {
            imgRight2.setVisibility(VISIBLE);
            imgRight2.setImageResource(res);
        } else {
            imgRight.setVisibility(View.GONE);
        }
    }

    public void setLeftImg(int res) {
        if (res != -1) {
            imgLeft.setImageResource(res);
//            RelativeLayout.LayoutParams layoutParams = (LayoutParams) imgLeft.getLayoutParams();
//            layoutParams.topMargin = PixelUtils.dp2px(30);
//            imgLeft.setLayoutParams(layoutParams);
            imgLeft.setVisibility(View.VISIBLE);
        } else {
            imgLeft.setVisibility(View.GONE);
        }
    }

    public void setLeftImgVisible(int visible) {

        imgLeft.setVisibility(visible);
    }

    public void setTitleGravity() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        titleLayout.setLayoutParams(layoutParams);
    }

    public void setTitleOnClickListener(OnClickListener listener) {
        titleLayout.setOnClickListener(listener);
    }

    public void setLeftOnClickListener(OnClickListener listener) {
        leftLayout.setOnClickListener(listener);
    }

    public void setRightOnClickListener(OnClickListener listener) {
//        imgRight.setOnClickListener(listener);
        rightLayout.setOnClickListener(listener);
    }

    public void setRight2OnClickListener(OnClickListener listener) {
        imgRight2.setOnClickListener(listener);
    }

    public void setLeftVisiblity(int visibility) {
        leftLayout.setVisibility(visibility);
        if (visibility != GONE) {
            setLeftOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).finish();
                    }
                }
            });
        }
    }

    public void setRightVisiblity(int visibility) {
        rightLayout.setVisibility(visibility);
    }

    public void setRightRedPointVisiblity(int visibility) {
        redPointRight.setVisibility(visibility);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) //wrap_conent的情况
        {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(PixelUtils.dp2px(TOPBAR_DEFAULT_HEIGHT), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTitleLength(int len) {
        ViewGroup.LayoutParams layoutParams = tvTitle.getLayoutParams();
        layoutParams.width = PixelUtils.dp2px(len);
        tvTitle.setLayoutParams(layoutParams);
    }

    public void setTitleMaxLength(int len) {
        tvTitle.setMaxWidth(PixelUtils.dp2px(len));
    }

    public void setTitleRightImage(int imgRes) {
        imgTitleIcon.setVisibility(GONE);
        tvTitle.setCompoundDrawables(null, null, mContext.getResources().getDrawable(imgRes), null);
    }

    public View getDivider() {
        return findViewById(R.id.topbar_divider);
    }

}
