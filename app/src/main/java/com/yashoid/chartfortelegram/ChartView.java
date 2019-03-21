package com.yashoid.chartfortelegram;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ChartView extends View implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    private static final String TAG = "ChartView";

    public static final long ANIMATION_DURATION = 300; // Duration in the given animation was 333.

    public interface OnTimeSelectedListener {

        void onTimeSelected(int timeIndex);

    }

    private OnTimeSelectedListener mOnTimeSelectedListener = null;

    private Chart mChart;

    private List<ChartLine> mChartLines = new ArrayList<>();
    private List<ChartDrawable> mChartDrawables = new ArrayList<>();

    private List<ChartDrawable> mRemovingChartDrawables = new ArrayList<>();

    private LegendDrawable mLegendDrawable;
    private ScaleDrawable mScaleDrawable;

    private SelectionLineDrawable mSelectionDrawable;

    private DefaultHorizontalMeasurementInfo mHorizontalMeasurementInfo;

    private long mSelectedAreaStart = 0;
    private long mSelectedAreaEnd = 0;

    private int mCurrentAreaMaxValue = 0;
    private int mTargetAreaMaxValue = 0;

    private ValueAnimator mAnimator = null;

    public ChartView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        mHorizontalMeasurementInfo = new DefaultHorizontalMeasurementInfo();
        mHorizontalMeasurementInfo.addOnHorizontalMeasurementsChangedListener(this);

        mLegendDrawable = new LegendDrawable();
        mLegendDrawable.setMeasurementInfo(mHorizontalMeasurementInfo);
        mLegendDrawable.setCallback(this);

        mScaleDrawable = new ScaleDrawable();
        mScaleDrawable.setLabelStyle(12 * 3, Color.LTGRAY);
        mScaleDrawable.setLabelMargin(4 * 3);
        mScaleDrawable.setCallback(this);

        mSelectionDrawable = new SelectionLineDrawable();
        mSelectionDrawable.setBackgroundColor(0xffffffff);
        mSelectionDrawable.setRadius(4 * 3);
        mSelectionDrawable.setHorizontalMeasurementsInfo(mHorizontalMeasurementInfo);
        mSelectionDrawable.setCallback(this);
    }

    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        mOnTimeSelectedListener = listener;
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);

        invalidate();
    }

    public void setChart(Chart chart) {
        mChart = chart;

        mLegendDrawable.setChart(mChart);

        mHorizontalMeasurementInfo.setChart(mChart);

        if (mSelectedAreaStart == mSelectedAreaEnd) {
            mSelectedAreaStart = mChart.getStartTime();
            mSelectedAreaEnd = mChart.getEndTime();
        }

        updateDrawableMeasurements();
    }

    public void addChartLine(ChartLine chartLine) {
        mChartLines.add(chartLine);

        ChartDrawable chartDrawable = null;

        for (ChartDrawable cd: mRemovingChartDrawables) {
            if (cd.getChartLine() == chartLine) {
                chartDrawable = cd;
                break;
            }
        }

        if (chartDrawable != null) {
            mRemovingChartDrawables.remove(chartDrawable);
        }
        else {
            chartDrawable = new ChartDrawable();
            chartDrawable.setChartLine(chartLine);
        }

        chartDrawable.setHorizontalMeasurementInfo(mHorizontalMeasurementInfo);
        chartDrawable.setCallback(this);

        mChartDrawables.add(chartDrawable);

        setSelectedArea(mSelectedAreaStart, mSelectedAreaEnd);

        if (getWidth() > 0) {
            chartDrawable.setBounds(0, 0, getWidth(), getHeight());
            chartDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
        }

        chartDrawable.fadeIn();

        mSelectionDrawable.setChartLines(mChartDrawables);
    }

    public void removeChartLine(ChartLine chartLine) {
        int index = mChartLines.indexOf(chartLine);

        if (index < 0) {
            return;
        }

        mChartLines.remove(index);

        ChartDrawable chartDrawable = mChartDrawables.remove(index);
        chartDrawable.fadeOut();

        mRemovingChartDrawables.add(chartDrawable);

        setSelectedArea(mSelectedAreaStart, mSelectedAreaEnd);

        mSelectionDrawable.setChartLines(mChartDrawables);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mLegendDrawable.setBounds(0, getHeight() - 60, getWidth(), getHeight());

        mScaleDrawable.setBounds(0, 0, getWidth(), getHeight());
        mScaleDrawable.setMaxValue(mTargetAreaMaxValue);

        mSelectionDrawable.setBounds(mScaleDrawable.getBounds());
        mSelectionDrawable.setMaxValue(mTargetAreaMaxValue);

        mHorizontalMeasurementInfo.onLaidOut(getWidth());

        stopAnimation();

        updateDrawableMeasurements();
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

            mScaleDrawable.setMaxValue(mTargetAreaMaxValue);
            mSelectionDrawable.setMaxValue(mCurrentAreaMaxValue);

            for (ChartDrawable chartDrawable: mChartDrawables) {
                chartDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }

            for (ChartDrawable chartDrawable: mRemovingChartDrawables) {
                chartDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }
        }
        else if (mTargetAreaMaxValue != newTargetMaxValue) {
            mTargetAreaMaxValue = newTargetMaxValue;

            mScaleDrawable.setMaxValue(mTargetAreaMaxValue);

            startAnimation();
        }
    }

    private void stopAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = null;
    }

    private void startAnimation() {
        stopAnimation();

        mAnimator = new ValueAnimator();
        mAnimator.setIntValues(mCurrentAreaMaxValue, mTargetAreaMaxValue);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int maxValue = (int) animation.getAnimatedValue();

                if (maxValue != mCurrentAreaMaxValue) {
                    mCurrentAreaMaxValue = maxValue;

                    mSelectionDrawable.setMaxValue(mCurrentAreaMaxValue);

                    for (ChartDrawable chartDrawable: mChartDrawables) {
                        chartDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
                    }

                    for (ChartDrawable chartDrawable: mRemovingChartDrawables) {
                        chartDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
                    }
                }
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }

        });
        mAnimator.start();
    }

    private void updateDrawableMeasurements() {
        if (mChart == null || getWidth() == 0) {
            return;
        }

        mTargetAreaMaxValue = measureAreaMaxValue();
        mCurrentAreaMaxValue = mTargetAreaMaxValue;

        for (int i = 0; i < mChartDrawables.size(); i++) {
            ChartDrawable chartDrawable = mChartDrawables.get(i);

            chartDrawable.setBounds(0, 0, getWidth(), getHeight());
            chartDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
        }
    }

    private int measureAreaMaxValue() {
        int maxValue = 0;

        for (ChartLine line: mChartLines) {
            maxValue = Math.max(maxValue, line.getMaxValueInRange(mChart.getTimestamps(), mSelectedAreaStart, mSelectedAreaEnd));
        }

        return maxValue;
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long start = System.currentTimeMillis();

        super.onDraw(canvas);

        mScaleDrawable.draw(canvas);

        canvas.save();
        canvas.translate(-mHorizontalMeasurementInfo.getXForTime(mSelectedAreaStart), 0);

        for (ChartDrawable chartDrawable: mChartDrawables) {
            chartDrawable.draw(canvas);
        }

        ListIterator<ChartDrawable> iterator = mRemovingChartDrawables.listIterator();

        while (iterator.hasNext()) {
            ChartDrawable chartDrawable = iterator.next();

            if (chartDrawable.hasFadedOut()) {
                chartDrawable.setHorizontalMeasurementInfo(null);
                chartDrawable.setCallback(null);

                iterator.remove();
            }
            else {
                chartDrawable.draw(canvas);
            }
        }

        mSelectionDrawable.draw(canvas);

        mLegendDrawable.draw(canvas);

        canvas.restore();

        Log.d(TAG, "Draw time: " + (System.currentTimeMillis() - start));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int timeIndex = mHorizontalMeasurementInfo.getTimeIndexForX(mHorizontalMeasurementInfo.getXForTime(mSelectedAreaStart) + event.getX());

        if (mOnTimeSelectedListener != null) {
            mOnTimeSelectedListener.onTimeSelected(timeIndex);
        }

        return true;
    }

    public void setSelectedTimeIndex(int timeIndex) {
        mSelectionDrawable.setSelectedIndex(timeIndex);
    }

}
