package com.yashoid.chartfortelegram.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.yashoid.chartfortelegram.ChartTracker;
import com.yashoid.chartfortelegram.Config;
import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class SimpleChartView extends View implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener,
        ChartTracker.OnChartLinesChangedListener, ChartTracker.OnChartsChangedListener {

    private ChartTracker mChartTracker = null;

    private HashMap<ChartLine, ChartLineDrawable> mChartLineDrawables = new HashMap<>();
    private List<ChartLineDrawable> mRemovingChartLineDrawables = new ArrayList<>();

    private DefaultHorizontalMeasurementInfo mHorizontalMeasurementInfo;

    private int mCurrentAreaMaxValue = 0;
    private int mTargetAreaMaxValue = 0;

    private long mSelectedAreaStart = -1;
    private long mSelectedAreaEnd = -1;

    private ValueAnimator mAnimator = null;

    public SimpleChartView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public SimpleChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public SimpleChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        mHorizontalMeasurementInfo = new DefaultHorizontalMeasurementInfo();
        mHorizontalMeasurementInfo.addOnHorizontalMeasurementsChangedListener(this);

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(Config.ANIMATION_DURATION);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int maxValue = (int) animation.getAnimatedValue();

                if (maxValue != mCurrentAreaMaxValue) {
                    mCurrentAreaMaxValue = maxValue;

                    for (ChartLineDrawable chartLineDrawable : mChartLineDrawables.values()) {
                        chartLineDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
                    }

                    for (ChartLineDrawable chartLineDrawable : mRemovingChartLineDrawables) {
                        chartLineDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
                    }

                    onCurrentAreaMaxValueChanged(mCurrentAreaMaxValue);
                }
            }
        });
    }

    public void setChartTracker(ChartTracker chartTracker) {
        if (mChartTracker != null) {
            throw new RuntimeException("Chart tracker is already set.");
        }

        mChartTracker = chartTracker;

        mChartTracker.addOnChartLinesChangedListener(this);
        mChartTracker.addOnChartsChangedListener(this);
    }

    protected ChartTracker getChartTracker() {
        return mChartTracker;
    }

    protected HorizontalMeasurementInfo getHorizontalMeasurementInfo() {
        return mHorizontalMeasurementInfo;
    }

    protected ChartLineDrawable getChildLineDrawable(ChartLine chartLine) {
        return mChartLineDrawables.get(chartLine);
    }

    public int getMaxValue() {
        return mTargetAreaMaxValue;
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);

        invalidate();
    }

    @Override
    public void onChartAdded(Chart chart) {
        mHorizontalMeasurementInfo.addChart(chart);
    }

    @Override
    public void onChartLineAdded(ChartLine chartLine) {
        ChartLineDrawable chartLineDrawable = null;

        for (ChartLineDrawable cd: mRemovingChartLineDrawables) {
            if (cd.getChartLine() == chartLine) {
                chartLineDrawable = cd;
                break;
            }
        }

        if (chartLineDrawable != null) {
            mRemovingChartLineDrawables.remove(chartLineDrawable);
        }
        else {
            chartLineDrawable = new ChartLineDrawable(getChartLineStrokeWidth());
            chartLineDrawable.setChartLine(chartLine);
        }

        chartLineDrawable.setHorizontalMeasurementInfo(mHorizontalMeasurementInfo);
        chartLineDrawable.setCallback(this);

        mChartLineDrawables.put(chartLine, chartLineDrawable);

        refreshSelectedArea();

        if (getWidth() > 0) {
            setChartLineDrawableBounds(chartLineDrawable);
            chartLineDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
        }

        chartLineDrawable.fadeIn();
    }

    protected float getChartLineStrokeWidth() {
        return getResources().getDisplayMetrics().density * 1;
    }

    @Override
    public void onChartLineRemoved(ChartLine chartLine) {
        ChartLineDrawable chartLineDrawable = mChartLineDrawables.remove(chartLine);
        chartLineDrawable.fadeOut();

        mRemovingChartLineDrawables.add(chartLineDrawable);

        refreshSelectedArea();
    }

    @Override
    public void onChartRemoved(Chart chart) {
        mHorizontalMeasurementInfo.removeChart(chart);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mHorizontalMeasurementInfo.onLaidOut(getWidth());

        stopAnimation();

        layoutChartLineDrawables();
    }

    private void layoutChartLineDrawables() {
        if (getWidth() == 0) {
            return;
        }

        mTargetAreaMaxValue = measureAreaMaxValue();
        mCurrentAreaMaxValue = mTargetAreaMaxValue;

        for (int i = 0; i < mChartLineDrawables.size(); i++) {
            ChartLineDrawable chartLineDrawable = mChartLineDrawables.get(i);

            setChartLineDrawableBounds(chartLineDrawable);
            chartLineDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
        }
    }

    private void refreshSelectedArea() {
        setSelectedArea(mSelectedAreaStart, mSelectedAreaEnd);
    }

    public void setSelectedArea(long start, long end) {
        mSelectedAreaStart = start;
        mSelectedAreaEnd = end;

        mHorizontalMeasurementInfo.setRange(start, end);

        int newTargetMaxValue = measureAreaMaxValue();

        if (mCurrentAreaMaxValue == 0) {
            stopAnimation();

            mTargetAreaMaxValue = newTargetMaxValue;
            mCurrentAreaMaxValue = mTargetAreaMaxValue;

            for (ChartLineDrawable chartLineDrawable : mChartLineDrawables.values()) {
                chartLineDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }

            for (ChartLineDrawable chartLineDrawable : mRemovingChartLineDrawables) {
                chartLineDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }
        }
        else if (mTargetAreaMaxValue != newTargetMaxValue) {
            mTargetAreaMaxValue = newTargetMaxValue;

            startAnimation();
        }
    }

    private int measureAreaMaxValue() {
        if (mChartTracker == null) {
            return 0;
        }

        int maxValue = 0;

        for (ChartLine line: mChartTracker.getChartLines()) {
            if (mSelectedAreaStart == -1 || mSelectedAreaEnd == -1) {
                maxValue = Math.max(maxValue, line.getMaxValue());
            }
            else {
                maxValue = Math.max(maxValue, line.getMaxValueInRange(mHorizontalMeasurementInfo.getTimestamps(), mSelectedAreaStart, mSelectedAreaEnd));
            }
        }

        return maxValue;
    }

    protected void onCurrentAreaMaxValueChanged(int currentAreaMaxValue) {

    }

    private void stopAnimation() {
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    private void startAnimation() {
        stopAnimation();

        mAnimator.setIntValues(mCurrentAreaMaxValue, mTargetAreaMaxValue);
        mAnimator.start();
    }

    protected void setChartLineDrawableBounds(ChartLineDrawable drawable) {
        drawable.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        invalidate();
    }

    public float getXFix() {
        return -mHorizontalMeasurementInfo.getXForTime(mSelectedAreaStart);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        if (mSelectedAreaStart != -1) {
            canvas.translate(-mHorizontalMeasurementInfo.getXForTime(mSelectedAreaStart), 0);
        }

        drawContent(canvas);

        canvas.restore();
    }

    protected void drawContent(Canvas canvas) {
        drawCharts(canvas);
    }

    protected void drawCharts(Canvas canvas) {
        for (ChartLineDrawable chartLineDrawable : mChartLineDrawables.values()) {
            chartLineDrawable.draw(canvas);
        }

        ListIterator<ChartLineDrawable> iterator = mRemovingChartLineDrawables.listIterator();

        while (iterator.hasNext()) {
            ChartLineDrawable chartLineDrawable = iterator.next();

            if (chartLineDrawable.hasFadedOut()) {
                chartLineDrawable.setHorizontalMeasurementInfo(null);
                chartLineDrawable.setCallback(null);

                iterator.remove();
            }
            else {
                chartLineDrawable.draw(canvas);
            }
        }
    }

}
