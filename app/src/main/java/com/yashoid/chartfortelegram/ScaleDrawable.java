package com.yashoid.chartfortelegram;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

public class ScaleDrawable extends Drawable implements Drawable.Callback {

    private static final long ANIMATION_DURATION = ChartView.ANIMATION_DURATION;

    private ScaleLineDrawable mDrawable;
    private ScaleLineDrawable mAnimatedDrawable = null;

    private int mMaxValue = 0;

    private ValueAnimator mAnimator;

    private Rect mPreviousBounds = new Rect();

    public ScaleDrawable() {
        mDrawable = new ScaleLineDrawable();
        mDrawable.setCallback(this);

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatedDrawable = new ScaleLineDrawable(mDrawable);
                mAnimatedDrawable.setCallback(ScaleDrawable.this);
                mAnimatedDrawable.setMaxValue(mMaxValue, getBounds().height());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDrawable.setCallback(null);

                mDrawable = mAnimatedDrawable;

                mAnimatedDrawable = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }

        });
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) (animation.getAnimatedFraction() * 255);

                final int maxValue = (int) animation.getAnimatedValue();
                final int previousMaxValue = mDrawable.getMaxValue();
                final int newMaxValue = mMaxValue;

                final Rect bounds = getBounds();

                final int height = bounds.height();

                mDrawable.setBounds(bounds.left, bounds.bottom - (int) ((long) previousMaxValue * height / maxValue), bounds.right, bounds.bottom);
                mDrawable.setAlpha(255 - alpha);

                mAnimatedDrawable.setBounds(bounds.left, bounds.bottom - (int) ((long) newMaxValue * height / maxValue), bounds.right, bounds.bottom);
                mAnimatedDrawable.setAlpha(alpha);
            }

        });
    }

    public void setMaxValue(int maxValue) {
        if (maxValue == mMaxValue) {
            return;
        }

        if (mMaxValue == 0 || maxValue == 0) {
            mMaxValue = maxValue;
            mDrawable.setMaxValue(maxValue, getBounds().height());
            return;
        }

        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator.setIntValues(mMaxValue, maxValue);

        mMaxValue = maxValue;

        mAnimator.start();
    }

    public void setLabelColor(int color) {
        mDrawable.setLabelColor(color);
    }

    public void setLabelTextSize(float textSize) {
        mDrawable.setLabelTextSize(textSize);
    }

    public void setLabelTypeface(Typeface typeface) {
        mDrawable.setLabelTypeFace(typeface);
    }

    public void setLineWidth(float width) {
        mDrawable.setLineWidth(width);
    }

    public void setLineColor(int color) {
        mDrawable.setLineColor(color);
    }

    public void setLabelMargin(float margin) {
        mDrawable.setLabelMargin(margin);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.equals(mPreviousBounds)) {
            return;
        }

        mPreviousBounds = bounds;

        mDrawable.setBounds(bounds);
        mDrawable.setMaxValue(mMaxValue, bounds.height());
    }

    @Override
    public void draw(Canvas canvas) {
        mDrawable.draw(canvas);

        if (mAnimatedDrawable != null) {
            mAnimatedDrawable.draw(canvas);
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

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

}
