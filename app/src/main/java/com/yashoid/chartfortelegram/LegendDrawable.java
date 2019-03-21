package com.yashoid.chartfortelegram;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import com.yashoid.chartfortelegram.data.Chart;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LegendDrawable extends Drawable implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    private static final long FADE_DURATION = ChartView.ANIMATION_DURATION;

    private Paint mPaint;
    private Paint mFadePaint;

    private float mMinimumRequiredSpaceForLabels;
    private float mAvailableSpace;

    private Chart mChart = null;

    private HorizontalMeasurementInfo mMeasurementInfo = null;

    private String[] mLabels;
    private float[] mLabelXOffsets;
    private float[] mLabelPositions;

    private float mTextY = 0;

    private int mOmitRatio = -1;
    private int mPreviousOmitRatio = -1;
    private ValueAnimator mAnimator;

    private boolean mChartInvalidated = true;
    private boolean mPaintInvalidated = true;
    private boolean mLabelOffsetsInvalidated = true;
    private boolean mMeasurementInfoInvalidated = true;
    private boolean mOmitRatioInvalidated = true;

    public LegendDrawable() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.CYAN);
        mPaint.setTextSize(10 * 3);
        mPaint.setTextAlign(Paint.Align.LEFT);

        mFadePaint = new Paint(mPaint);
        mFadePaint.setAlpha(0);

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(FADE_DURATION);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFadePaint.setAlpha((int) animation.getAnimatedValue());

                invalidateSelf();
            }

        });
    }

    public void setTextSize(float textSize) {
        if (mPaint.getTextSize() == textSize) {
            return;
        }

        mPaint.setTextSize(textSize);
        mFadePaint.setTextSize(textSize);

        mPaintInvalidated = true;
        mLabelOffsetsInvalidated = true;
        mOmitRatioInvalidated = true;

        invalidateSelf();
    }

    public void setChart(Chart chart) {
        mChart = chart;

        mChartInvalidated = true;
        mLabelOffsetsInvalidated = true;
        mMeasurementInfoInvalidated = true;
        mOmitRatioInvalidated = true;

        invalidateSelf();
    }

    public void setMeasurementInfo(HorizontalMeasurementInfo measurementInfo) {
        if (mMeasurementInfo != null) {
            mMeasurementInfo.removeOnHorizontalMeasurementsChangedListener(this);
        }

        mMeasurementInfo = measurementInfo;

        if (mMeasurementInfo != null) {
            mMeasurementInfo.addOnHorizontalMeasurementsChangedListener(this);
        }

        mMeasurementInfoInvalidated = true;
        mOmitRatioInvalidated = true;

        invalidateSelf();
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        mMeasurementInfoInvalidated = true;
        mOmitRatioInvalidated = true;

        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mTextY = bounds.top + bounds.height() / 2;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mChart == null || mMeasurementInfo == null) {
            return;
        }

        if (mChartInvalidated) {
            prepareForChart();

            mChartInvalidated = false;
        }

        if (mPaintInvalidated) {
            measureMinimumRequiredSpaceForLabels();

            mPaintInvalidated = false;
        }

        if (mLabelOffsetsInvalidated) {
            measureLabelOffsets();

            mLabelOffsetsInvalidated = false;
        }

        if (mMeasurementInfoInvalidated) {
            measureLabelPositions();

            mMeasurementInfoInvalidated = false;
        }

        if (mOmitRatioInvalidated) {
            measureOmitRatio();

            mOmitRatioInvalidated = false;
        }

        final int step = mPreviousOmitRatio == -1 ? mOmitRatio : Math.min(mOmitRatio, mPreviousOmitRatio);

        boolean isOnMainLabel = true;

        final boolean shouldDrawFade = mPreviousOmitRatio == -1 ? false : mFadePaint.getAlpha() != 0;

        for (int i = mLabels.length - 1; i >= 0; i -= step) {
            if (isOnMainLabel) {
                canvas.drawText(mLabels[i], mLabelPositions[i] + mLabelXOffsets[i], mTextY, mPaint);
            }
            else if (shouldDrawFade) {
                canvas.drawText(mLabels[i], mLabelPositions[i] + mLabelXOffsets[i], mTextY, mFadePaint);
            }

            isOnMainLabel = !isOnMainLabel;
        }
    }

    private void prepareForChart() {
        long[] timestamps = mChart.getTimestamps();

        mLabels = new String[timestamps.length];
        mLabelXOffsets = new float[timestamps.length];
        mLabelPositions = new float[timestamps.length];

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");

        for (int i = 0; i < mLabels.length; i++) {
            mLabels[i] = dateFormat.format(new Date(timestamps[i]));
        }
    }

    private void measureMinimumRequiredSpaceForLabels() {
        mMinimumRequiredSpaceForLabels = 1.5f * mPaint.measureText("Jan 01");
    }

    private void measureLabelOffsets() {
        for (int i = 0; i < mLabels.length; i++) {
            mLabelXOffsets[i] = -mMinimumRequiredSpaceForLabels * i / (mLabels.length - 1);
        }
}

    private void measureLabelPositions() {
        for (int i = 0; i < mLabels.length; i++) {
            mLabelPositions[i] = mMeasurementInfo.getXForIndex(i);
        }

        mAvailableSpace = mMeasurementInfo.getXForIndex(mLabels.length - 1);
    }

    private void measureOmitRatio() {
        int numberOfLabelsPossibleToBeDisplayed = (int) Math.floor(mAvailableSpace / mMinimumRequiredSpaceForLabels);

        int omitRatio = 1;
        int numberToDisplay = mLabels.length;

        while (numberOfLabelsPossibleToBeDisplayed < numberToDisplay) {
            omitRatio *= 2;

            numberToDisplay /= 2;
        }

        if (omitRatio == mOmitRatio) {
            return;
        }

        boolean shouldStartAnimation = mPreviousOmitRatio != -1;

        mPreviousOmitRatio = mOmitRatio;
        mOmitRatio = omitRatio;

        if (shouldStartAnimation) {
            if (mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            if (mOmitRatio > mPreviousOmitRatio) {
                mAnimator.setIntValues(mFadePaint.getAlpha() > 0 ? mFadePaint.getAlpha() : 255, 0);
            }
            else {
                mAnimator.setIntValues(mFadePaint.getAlpha() == 255 ? 0 : mFadePaint.getAlpha(), 255);
            }

            mAnimator.start();
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

}
