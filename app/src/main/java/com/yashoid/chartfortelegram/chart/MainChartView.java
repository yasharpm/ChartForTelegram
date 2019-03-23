package com.yashoid.chartfortelegram.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.yashoid.chartfortelegram.chart.legend.LegendDrawable;
import com.yashoid.chartfortelegram.chart.scale.ScaleDrawable;
import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.List;

public class MainChartView extends SimpleChartView {

    public interface OnTimeSelectedListener {

        void onTimeSelected(int timeIndex);

    }

    private OnTimeSelectedListener mOnTimeSelectedListener = null;

    private float mLineStrokeWidth;

    private LegendDrawable mLegendDrawable;
    private ScaleDrawable mScaleDrawable;

    private SelectionLineDrawable mSelectionDrawable;

    public MainChartView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MainChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MainChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        float density = getResources().getDisplayMetrics().density;

        mLineStrokeWidth = density * 2;

        final HorizontalMeasurementInfo horizontalMeasurementInfo = getHorizontalMeasurementInfo();

        horizontalMeasurementInfo.setOnRangeChangedListener(new HorizontalMeasurementInfo.OnRangeChangedListener() {

            @Override
            public void onRangeChanged(long start, long end) {
                setSelectedArea(start, end);
            }

        });

        mLegendDrawable = new LegendDrawable(density * 12);
        mLegendDrawable.setMeasurementInfo(horizontalMeasurementInfo);
        mLegendDrawable.setCallback(this);

        mScaleDrawable = new ScaleDrawable();
        mScaleDrawable.setLabelTextSize(12 * density);
        mScaleDrawable.setLabelColor(0xff96a2aa);
        mScaleDrawable.setLabelMargin(4 * density);
        mScaleDrawable.setCallback(this);
        mScaleDrawable.setLineWidth(density * 0.75f);

        mSelectionDrawable = new SelectionLineDrawable();
        mSelectionDrawable.setBackgroundColor(0xffffffff);
        mSelectionDrawable.setRadius(5 * density);
        mSelectionDrawable.setLineWidth(density * 1);
        mSelectionDrawable.setHorizontalMeasurementsInfo(horizontalMeasurementInfo);
        mSelectionDrawable.setCallback(this);
    }

    public void setOnSelectionInfoChangedListener(SelectionLineDrawable.OnSelectionInfoChangedListener listener) {
        mSelectionDrawable.setOnSelectionInfoChangedListener(listener);
    }

    public void setLegendColor(int color) {
        mLegendDrawable.setTextColor(color);
        mScaleDrawable.setLabelColor(color);
    }

    public void setLinesWidth(float width) {
        mScaleDrawable.setLineWidth(width);
        mSelectionDrawable.setLineWidth(width);
    }

    public void setScaleLinesColor(int color) {
        mScaleDrawable.setLineColor(color);
    }

    public void setSelectionLineColor(int color) {
        mSelectionDrawable.setLineColor(color);
    }

    public void setSelectionBackgroundColor(int color) {
        mSelectionDrawable.setBackgroundColor(color);
    }

    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        mOnTimeSelectedListener = listener;
    }

    @Override
    public void onChartAdded(Chart chart) {
        super.onChartAdded(chart);

        mLegendDrawable.setTimestamps(getHorizontalMeasurementInfo().getTimestamps());
    }

    @Override
    public void onChartLineAdded(ChartLine chartLine) {
        super.onChartLineAdded(chartLine);

        mSelectionDrawable.addChartLineDrawable(getChildLineDrawable(chartLine));
    }

    @Override
    public void onChartLineRemoved(ChartLine chartLine) {
        mSelectionDrawable.removeChartLineDrawable(getChildLineDrawable(chartLine));

        super.onChartLineRemoved(chartLine);
    }

    @Override
    public void onChartRemoved(Chart chart) {
        super.onChartRemoved(chart);

        mLegendDrawable.setTimestamps(getHorizontalMeasurementInfo().getTimestamps());
    }

    @Override
    protected float getChartLineStrokeWidth() {
        return mLineStrokeWidth;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int legendsHeight = mLegendDrawable.getIntrinsicHeight();

        mLegendDrawable.setBounds(0, getHeight() - legendsHeight, getWidth(), getHeight());

        mScaleDrawable.setBounds(0, 0, getWidth(), getHeight() - legendsHeight);
        mScaleDrawable.setMaxValue(getMaxValue());

        mSelectionDrawable.setBounds(mScaleDrawable.getBounds());
        mSelectionDrawable.onMaxValueChanged();

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void setChartLineDrawableBounds(ChartLineDrawable drawable) {
        drawable.setBounds(mScaleDrawable.getBounds());
    }

    @Override
    public void setSelectedArea(long start, long end) {
        super.setSelectedArea(start, end);

        mSelectionDrawable.onMaxValueChanged();
        mScaleDrawable.setMaxValue(getMaxValue());
    }

    @Override
    protected void onCurrentAreaMaxValueChanged(int currentAreaMaxValue) {
        super.onCurrentAreaMaxValueChanged(currentAreaMaxValue);

        mSelectionDrawable.onMaxValueChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mScaleDrawable.draw(canvas);

        super.onDraw(canvas);
    }

    @Override
    protected void drawContent(Canvas canvas) {
        super.drawContent(canvas);

        mSelectionDrawable.draw(canvas);
        mLegendDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        List<ChartLine> ChartLines = getChartTracker().getChartLines();

        if (ChartLines.isEmpty()) {
            return false;
        }

        HorizontalMeasurementInfo horizontalMeasurementInfo = getHorizontalMeasurementInfo();

        int timeIndex = horizontalMeasurementInfo.getTimeIndexForX(event.getX() - getXFix());

        if (mOnTimeSelectedListener != null) {
            mOnTimeSelectedListener.onTimeSelected(timeIndex);
        }

        return true;
    }

    public void setSelectedTimeIndex(int timeIndex) {
        mSelectionDrawable.setSelectedIndex(timeIndex);
    }

}
