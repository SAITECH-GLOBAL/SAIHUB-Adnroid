
package com.linktech.saihub.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.linktech.saihub.R;
import com.linktech.saihub.entity.power.DeviceInfoHourData;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class DeFiMarkerView extends MarkerView {
    private final int DEFAULT_INDICATOR_COLOR = 0xffFF584f;//指示器默认的颜色
    private final int ARROW_HEIGHT = dp2px(3); // 箭头的高度
    private final int ARROW_WIDTH = dp2px(15); // 箭头的宽度
    private final float ARROW_OFFSET = dp2px(2);//箭头偏移量
    private final float BG_CORNER = dp2px(2);//背景圆角
    private Bitmap bitmapForDot;//选中点图片
    private int bitmapWidth;//点宽
    private int bitmapHeight;//点高

    private ValueFormatter valueFormatter;
    DecimalFormat df = new DecimalFormat("0.00");
    private final TextView tvContent;
    private final List<DeviceInfoHourData> dataList;

    public DeFiMarkerView(Context context, int layoutResource, List<DeviceInfoHourData> trendDataList) {
        super(context, layoutResource);
        dataList = trendDataList;
        tvContent = findViewById(R.id.tvContent);


        //图片自行替换
        bitmapForDot = getBitmap(context, R.mipmap.icon_chart_dot);
        bitmapWidth = bitmapForDot.getWidth();
        bitmapHeight = bitmapForDot.getHeight();
    }


    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
        } else {
            DeviceInfoHourData resultDTO = dataList.get((int) e.getX());
//            String time = DateUtils.getSimpleTimeFormat(resultDTO.getCreatedDate(), DateUtils.DATE_FORMAT_19);
            tvContent.setText(resultDTO.getUnitEnergyConsumption() + "\n" + resultDTO.getOutputHeat());
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        Chart chart = getChartView();
        if (chart == null) {
            super.draw(canvas, posX, posY);
            return;
        }

        //指示器背景画笔
        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(DEFAULT_INDICATOR_COLOR);
        //剪头画笔
        Paint arrowPaint = new Paint();
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setColor(DEFAULT_INDICATOR_COLOR);

        float width = getWidth();
        float height = getHeight();

        int saveId = canvas.save();
        //移动画布到点并绘制点
        canvas.translate(posX, posY);
        canvas.drawBitmap(bitmapForDot, -bitmapWidth / 2f, -bitmapHeight / 2f, null);

        //画指示器
        Path path = new Path();
        RectF bRectF;
        if (posY < height + ARROW_HEIGHT + bitmapHeight / 2f) {//处理超过上边界
            //移动画布并绘制三角形和背景
            canvas.translate(0, height + ARROW_HEIGHT + bitmapHeight / 2f);
            if (posX > chart.getWidth() - (width / 2f)) {//超过右边界
                canvas.translate(-(width / 2 - (chart.getWidth() - posX)), 0);
                /*************绘制三角形  超过上边界 / 超过右边界**/
                path.moveTo(width / 2 - (chart.getWidth() - posX) - ARROW_OFFSET, -(height + ARROW_HEIGHT + ARROW_OFFSET));
                path.lineTo(ARROW_WIDTH / 2f, -(height - BG_CORNER));
                path.lineTo(-ARROW_WIDTH / 2f, -(height - BG_CORNER));
                path.moveTo(width / 2 - (chart.getWidth() - posX) - ARROW_OFFSET, -(height + ARROW_HEIGHT + ARROW_OFFSET));

            } else {
                if (posX > width / 2f) {//在图表中间
                    path.moveTo(0, -(height + ARROW_HEIGHT));
                    path.lineTo(ARROW_WIDTH / 2f, -(height - BG_CORNER));
                    path.lineTo(-ARROW_WIDTH / 2f, -(height - BG_CORNER));
                    path.lineTo(0, -(height + ARROW_HEIGHT));
                } else {//超过左边界
                    canvas.translate(width / 2f - posX, 0);
                    /*************绘制三角形 超过上边界 /  超过左边界**/
                    path.moveTo(-(width / 2f - posX) - ARROW_OFFSET, -(height + ARROW_HEIGHT + ARROW_OFFSET));
                    path.lineTo(ARROW_WIDTH / 2f, -(height - BG_CORNER));
                    path.lineTo(-ARROW_WIDTH / 2f, -(height - BG_CORNER));
                    path.moveTo(-(width / 2f - posX) - ARROW_OFFSET, -(height + ARROW_HEIGHT + ARROW_OFFSET));

                }
            }

            bRectF = new RectF(-width / 2, -height, width / 2, 0);
//            canvas.drawPath(path, arrowPaint);
//            canvas.drawRoundRect(bRectF, BG_CORNER, BG_CORNER, bgPaint);
            canvas.translate(-width / 2f, -height);
        } else {//没有超过上边界
            //移动画布并绘制三角形和背景
            canvas.translate(0, -height - ARROW_HEIGHT - bitmapHeight / 2f);
            if (posX < width / 2f) {//超过左边界  平移view
                canvas.translate(width / 2f - posX, 0);

                /*************绘制三角形 超过下边界 /  超过左边界**/
                path.moveTo(-(width / 2f - posX) + ARROW_OFFSET, height + ARROW_HEIGHT + ARROW_OFFSET);
                path.lineTo(ARROW_WIDTH / 2f, height - BG_CORNER);
                path.lineTo(-ARROW_WIDTH / 2f, height - BG_CORNER);
                path.moveTo(-(width / 2f - posX) + ARROW_OFFSET, height + ARROW_HEIGHT + ARROW_OFFSET);
            } else {
                if (posX > chart.getWidth() - (width / 2f)) {//超过右边界  绘制三角
                    /*************绘制三角形 超过下边界 /  超过右边界**/
                    canvas.translate(-(width / 2 - (chart.getWidth() - posX)), 0);
                    path.moveTo(width / 2 - (chart.getWidth() - posX) + ARROW_OFFSET, height + ARROW_HEIGHT + ARROW_OFFSET);
                    path.lineTo(ARROW_WIDTH / 2f, height - BG_CORNER);
                    path.lineTo(-ARROW_WIDTH / 2f, height - BG_CORNER);
                    path.moveTo(width / 2 - (chart.getWidth() - posX) + ARROW_OFFSET, height + ARROW_HEIGHT + ARROW_OFFSET);

                } else {
                    path.moveTo(0, height + ARROW_HEIGHT);
                    path.lineTo(ARROW_WIDTH / 2f, height - BG_CORNER);
                    path.lineTo(-ARROW_WIDTH / 2f, height - BG_CORNER);
                    path.moveTo(0, height + ARROW_HEIGHT);
                }

            }

            bRectF = new RectF(-width / 2, 0, width / 2, height);

//            canvas.drawPath(path, arrowPaint);
//            canvas.drawRoundRect(bRectF, BG_CORNER, BG_CORNER, bgPaint);
            canvas.translate(-width / 2f, 0);
        }
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    private int dp2px(int dpValues) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpValues,
                getResources().getDisplayMetrics());
    }
}
