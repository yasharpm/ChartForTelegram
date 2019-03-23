package com.yashoid.chartfortelegram;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class SimpleChartView extends View implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    public static final long ANIMATION_DURATION = 300; // Duration in the given animation was 333.

    private HashMap<Chart, List<ChartLine>> mCharts = new HashMap<>();

    private List<ChartLine> mChartLines = new ArrayList<>();
    private List<ChartDrawable> mChartDrawables = new ArrayList<>();

    private List<ChartDrawable> mRemovingChartDrawables = new ArrayList<>();

    private DefaultHorizontalMeasurementInfo mHorizontalMeasurementInfo;

    private int mCurrentAreaMaxValue = 0;
    private int mTargetAreaMaxValue = 0;

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
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);

        invalidate();
    }

    public void addChartLine(ChartLine chartLine) {
        List<ChartLine> lines = mCharts.get(chartLine.getChart());

        if (lines == null) {
            lines = new ArrayList<>();
            mCharts.put(chartLine.getChart(), lines);

            mHorizontalMeasurementInfo.addChart(chartLine.getChart());
        }

        lines.add(chartLine);

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
            chartDrawable = new ChartDrawable(3);
            chartDrawable.setChartLine(chartLine);
        }

        chartDrawable.setHorizontalMeasurementInfo(mHorizontalMeasurementInfo);
        chartDrawable.setCallback(this);

        mChartDrawables.add(chartDrawable);

        setSelectedArea();

        if (getWidth() > 0) {
            chartDrawable.setBounds(0, 0, getWidth(), getHeight());
            chartDrawable.setVerticalMeasurementInfo(mCurrentAreaMaxValue);
        }

        chartDrawable.fadeIn();
    }

    public void removeChartLine(ChartLine chartLine) {
        List<ChartLine> lines = mCharts.get(chartLine.getChart());

        if (lines == null) {
            return;
        }

        if (!lines.remove(chartLine)) {
            return;
        }

        if (lines.isEmpty()) {
            mCharts.remove(chartLine.getChart());

            mHorizontalMeasurementInfo.removeChart(chartLine.getChart());
        }

        int index = mChartLines.indexOf(chartLine);

        if (index < 0) {
            return;
        }

        mChartLines.remove(index);

        ChartDrawable chartDrawable = mChartDrawables.remove(index);
        chartDrawable.fadeOut();

        mRemovingChartDrawables.add(chartDrawable);

        setSelectedArea();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mHorizontalMeasurementInfo.onLaidOut(getWidth());

        stopAnimation();

        updateDrawableMeasurements();
    }

    public void setSelectedArea() {
        int newTargetMaxValue = measureAreaMaxValue();

        if (mCurrentAreaMaxValue == 0) {
            stopAnimation();

            mTargetAreaMaxValue = newTargetMaxValue;
            mCurrentAreaMaxValue = mTargetAreaMaxValue;

            for (ChartDrawable chartDrawable: mChartDrawables) {
                chartDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }

            for (ChartDrawable chartDrawable: mRemovingChartDrawables) {
                chartDrawable.setVerticalMeasurementInfo(mTargetAreaMaxValue);
            }
        }
        else if (mTargetAreaMaxValue != newTargetMaxValue) {
            mTargetAreaMaxValue = newTargetMaxValue;

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
        if (getWidth() == 0) {
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
            maxValue = Math.max(maxValue, line.getMaxValue());
        }

        return maxValue;
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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
    }

}
