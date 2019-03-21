package com.yashoid.chartfortelegram;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import com.yashoid.chartfortelegram.data.ChartLine;

public class ChartDrawable extends Drawable implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    private Paint mPaint;

    private ValueAnimator mAnimator;

    private HorizontalMeasurementInfo mHorizontalMeasurementInfo = null;
    private float mCeiling;

    private ChartLine mChartLine = null;

    private float[] mLines = null;

    private boolean mHorizontallyInvalidated = true;
    private boolean mVerticallyInvalidated = true;

    public ChartDrawable() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(ChartView.ANIMATION_DURATION);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPaint.setAlpha((Integer) animation.getAnimatedValue());
                invalidateSelf();
            }

        });
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setHorizontalMeasurementInfo(HorizontalMeasurementInfo horizontalMeasurementInfo) {
        if (mHorizontalMeasurementInfo != null) {
            mHorizontalMeasurementInfo.removeOnHorizontalMeasurementsChangedListener(this);
        }

        mHorizontalMeasurementInfo = horizontalMeasurementInfo;

        if (mHorizontalMeasurementInfo != null) {
            mHorizontalMeasurementInfo.addOnHorizontalMeasurementsChangedListener(this);
        }

        mHorizontallyInvalidated = true;

        invalidateSelf();
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        mHorizontallyInvalidated = true;

        invalidateSelf();
    }

    public void setVerticalMeasurementInfo(float ceiling) {
        mCeiling = ceiling;

        mVerticallyInvalidated = true;

        invalidateSelf();
    }

    public void setChartLine(ChartLine chartLine) {
        mChartLine = chartLine;

        mChartLine.applyPaint(mPaint);
        mPaint.setAlpha(0);

        mLines = new float[4 * (mChartLine.getValues().length - 1)];

        mHorizontallyInvalidated = true;
        mVerticallyInvalidated = true;

        invalidateSelf();
    }

    public ChartLine getChartLine() {
        return mChartLine;
    }

    public float getY(int index) {
        if (index == mChartLine.getValues().length - 1) {
            return mLines[(index - 1) * 4 + 3];
        }

        return mLines[index * 4 + 1];
    }

    public void fadeIn() {
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator.setIntValues(mPaint.getAlpha(), 255);
        mAnimator.start();
    }

    public void fadeOut() {
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator.setIntValues(mPaint.getAlpha(), 0);
        mAnimator.start();
    }

    public boolean hasFadedOut() {
        return mPaint.getAlpha() < 2 && !mAnimator.isRunning();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mHorizontallyInvalidated = true;
        mVerticallyInvalidated = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mChartLine == null || mHorizontalMeasurementInfo == null) {
            return;
        }

        if (mHorizontallyInvalidated || mVerticallyInvalidated) {
            updateMeasurements();

            mHorizontallyInvalidated = false;
            mVerticallyInvalidated = false;
        }

        canvas.drawLines(mLines, mPaint);
    }

    private void updateMeasurements() {
        if (mCeiling == 0) {
            return;
        }

        final Rect bounds = getBounds();

        final float height = bounds.height();

        final int[] values = mChartLine.getValues();

        final float yBase = bounds.top + height;
        final float yBaseRatio = -height / mCeiling;

        float x = 0;
        float y = mVerticallyInvalidated ? yBase + values[0] * yBaseRatio : 0;

        int lineIndex = 0;

        for (int i = 0; i < values.length - 1; i++) {
            if (mHorizontallyInvalidated) {
                mLines[lineIndex] = x;
                x = mHorizontalMeasurementInfo.getXForIndex(i + 1);
                mLines[lineIndex + 2] = x;
            }

            if (mVerticallyInvalidated) {
                mLines[lineIndex + 1] = y;
                y = yBase + values[i + 1] * yBaseRatio;
                mLines[lineIndex + 3] = y;
            }

            lineIndex += 4;
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
