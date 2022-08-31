package com.linktech.saihub.view.line;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.linktech.saihub.R;
import com.linktech.saihub.entity.power.DeviceInfoHourData;
import com.linktech.saihub.util.DateUtils;
import com.linktech.saihub.util.NumberCountUtils;
import com.linktech.saihub.view.DeFiMarkerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 博客 https://www.cnblogs.com/kangweifeng/p/11250907.html
 */
public class DeFiChartView extends LineChart {


    public DeFiChartView(Context context) {
        super(context);
    }

    public DeFiChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeFiChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        {   // // Chart Style // //

            // background color
            this.setBackgroundColor(Color.WHITE);

            // disable description text
            this.getDescription().setEnabled(false);

            // enable touch gestures
            this.setTouchEnabled(true);

            // set listeners
//            this.setOnthisValueSelectedListener(this);
            this.setDrawGridBackground(false);

            // enable scaling and dragging
//            this.setDragEnabled(false);
            //取消缩放
            this.setScaleEnabled(false);
            // this.setScaleXEnabled(true);
            // this.setScaleYEnabled(true);

            // force pinch zoom along both axis
            this.setPinchZoom(false);

            //四周是不是有边框
            //不显示边界
            this.setDrawBorders(false);
            //不显示图例
            this.getLegend().setEnabled(false);
            this.setAutoScaleMinMaxEnabled(false);
//            this.setDrawBorders(false);
//            this.setBorderWidth(0.5f);
        }
        //影藏底部色块
        Legend legend = this.getLegend();
        legend.setEnabled(false);
        this.setNoDataText("");
//        setExtraBottomOffset(10F);//防止底部数据显示不完整，设置底部偏移量
//        setExtraTopOffset(10F);//防止底部数据显示不完整，设置底部偏移量
    }

    public void setData(List<DeviceInfoHourData> data) {

        List<Double> yValues = new ArrayList();

        for (int i = 0; i < data.size(); i++) {
            yValues.add(Double.parseDouble(Objects.requireNonNull(data.get(i).getOutputHeat())));
            yValues.add(Double.parseDouble(Objects.requireNonNull(data.get(i).getUnitEnergyConsumption())));
        }

        DeFiMarkerView mv = new DeFiMarkerView(getContext(), R.layout.custom_marker_view, data);
        // Set the marker to the this
        mv.setChartView(this);
        this.setMarker(mv);

        setXAxisBasic(data);
        getAxisRight().setEnabled(false);
        YAxis axisLeft = getAxisLeft();
        axisLeft.setDrawAxisLine(false);
        axisLeft.setLabelCount(4);
        axisLeft.setGridLineWidth(1);
        if (yValues.size() > 0) {
            double min = NumberCountUtils.getMinFromList(yValues);
            double max = NumberCountUtils.getMaxFromList(yValues);
            if (min == 0 && max == 0) {
                axisLeft.setAxisMinimum(-1f);
                axisLeft.setAxisMaximum(1f);
            } else if (min > 0) {
                axisLeft.setAxisMinimum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(min), "0.9", 6)));
                axisLeft.setAxisMaximum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(max), "1.1", 6)));
            } else if (max < 0) {
                axisLeft.setAxisMinimum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(min), "1.1", 6)));
                axisLeft.setAxisMaximum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(max), "0.9", 6)));
            } else {
                axisLeft.setAxisMinimum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(min), "1.1", 6)));
                axisLeft.setAxisMaximum(Float.parseFloat(NumberCountUtils.getConvert(String.valueOf(max), "1.1", 6)));
            }
        } else {
            axisLeft.setAxisMinimum(0f);
            axisLeft.setAxisMaximum(1f);
        }
        axisLeft.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/d_dinexp_bold.otf"));
        axisLeft.setTextColor(Color.parseColor("#FF3E475A"));
        axisLeft.setGridColor(Color.parseColor("#0F090E16"));
        this.setMinOffset(0f);
        this.setExtraOffsets(10F, 10F, 10F, 10F);


        //数据填充
        List<Entry> powerVales = new ArrayList<>();
        List<Entry> outputHeatVales = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            powerVales.add(new Entry(i, Float.parseFloat(data.get(i).getUnitEnergyConsumption())));
            outputHeatVales.add(new Entry(i, Float.parseFloat(data.get(i).getOutputHeat())));
        }
        LineDataSet powerSet;
        LineDataSet outputHeatSet;
        if (this.getData() != null &&
                this.getData().getDataSetCount() > 0) {
            powerSet = (LineDataSet) this.getData().getDataSetByIndex(0);
            outputHeatSet = (LineDataSet) this.getData().getDataSetByIndex(1);
            powerSet.setValues(powerVales);
            outputHeatSet.setValues(outputHeatVales);

        } else {
            // create a dataset and give it a type
            powerSet = new LineDataSet(powerVales, "DataSet 1");
            powerSet.setColor(Color.parseColor("#FF8A01"));
            powerSet.setDrawCircles(false);
            powerSet.setLineWidth(2f);
            powerSet.setDrawValues(false);
            powerSet.setValueTextSize(9f);
            //line绘制模式
            powerSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            powerSet.setHighLightColor(Color.TRANSPARENT);
            powerSet.setDrawFilled(false);

            outputHeatSet = new LineDataSet(outputHeatVales, "DataSet 2");
            outputHeatSet.setColor(Color.parseColor("#48ADC3"));
            outputHeatSet.setDrawCircles(false);
            outputHeatSet.setLineWidth(2f);
            outputHeatSet.setDrawValues(false);
            outputHeatSet.setValueTextSize(9f);
            //line绘制模式
            outputHeatSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            outputHeatSet.setHighLightColor(Color.TRANSPARENT);
            outputHeatSet.setDrawFilled(true);
            outputHeatSet.setFillFormatter((dataSet, dataProvider) -> DeFiChartView.this.getAxisLeft().getAxisMinimum());
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_blue);
            outputHeatSet.setFillDrawable(drawable);
            outputHeatSet.enableDashedLine(10, 10, 0);


          /*  ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
            dataSets1.add(set2);*/

            LineData lineData = new LineData(powerSet, outputHeatSet);
            // set data
            this.setData(lineData);
            this.setDoubleTapToZoomEnabled(false);
            powerSet.notifyDataSetChanged();
        }
        this.getData().notifyDataChanged();
        this.notifyDataSetChanged();
        this.setVisibleXRangeMaximum(data.size());
        this.invalidate();
        this.animateX(300, Easing.Linear);
    }

    private void setXAxisBasic(List<DeviceInfoHourData> data) {
        //得到x轴
        XAxis xAxis = getXAxis();
        //是否调用x轴
        xAxis.setEnabled(true);
        xAxis.setDrawAxisLine(false);//是否绘制x轴的直线
        xAxis.setDrawGridLines(false);//是否画网格线
        xAxis.setGridColor(Color.parseColor("#090E160F"));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);// 设置x轴数据的位置
        xAxis.setTextSize(10);//设置轴标签字体大小
        xAxis.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/d_dinexp_bold.otf"));

        xAxis.setTextColor(Color.parseColor("#FF3E475A"));//设置轴标签字体的颜色
        xAxis.setAvoidFirstLastClipping(true);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘
        //设置竖线的显示样式为虚线  lineLength控制虚线段的长度  spaceLength控制线之间的空间
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setLabelCount(data.size() >= 7 ? 7 : data.size(), true);// 设置X轴的刻度数量，第二个参数表示是否平均分配
        xAxis.setAxisMinimum(0f);
        xAxis.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {
                if (data.size() > 0) {
                    return DateUtils.getSimpleTimeFormat(data.get((int) value).getTimestamp(), DateUtils.DATE_FORMAT_18);
                }
                return "";
            }
        });
//        xAxis.setAxisMinimum(-1f);
//        xAxis.setAxisMaximum(maximum);//设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
//        xAxis.setAxisMaximum(maximum + 1f);//设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
//        xAxis.setGranularity(1f);//设置x轴坐标之间的最小间隔（因为此图有缩放功能，X轴,Y轴可设置可缩放），放在setValueFormatter之前设置
//        xAxis.setCenterAxisLabels(false);
        //设置当前图表中最多在x轴坐标线上显示的刻度线总量为6
//        chart.setVisibleXRangeMaximum(xRangeMaximum);//这行必须放到下面，和setLabelCount同时设置，值-1
//        chart.setScaleEnabled(false);
//        chart.setDoubleTapToZoomEnabled(false);
//        chart.moveViewToX(-1f);//切换时间时用来将折线图回归到0点
//        chart.moveViewToX(0f);//切换时间时用来将折线图回归到0点
    }

}

