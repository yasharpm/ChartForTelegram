package com.yashoid.chartfortelegram.checkbox;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class CheckDrawable extends Drawable {

    private static final float HEIGHT_RATIO = 0.5f;

    private int mWidth;
    private int mHeight;
    private float mCorners;

    private Paint mFillPaint;
    private Paint mBackgroundFillPaint;
    private Paint mBackgroundStrokePaint;

    private boolean mChecked = false;

    private RectF mHelper = new RectF();

    public CheckDrawable(int color, float density) {
        mWidth = (int) (40 * density);
        mHeight = (int) (40 * density);

        mCorners = 2 * density;

        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(color);

        mBackgroundFillPaint = new Paint();
        mBackgroundFillPaint.setAntiAlias(true);
        mBackgroundFillPaint.setColor(Color.WHITE);
        mBackgroundFillPaint.setStyle(Paint.Style.FILL);

        mBackgroundStrokePaint = new Paint(mBackgroundFillPaint);
        mBackgroundStrokePaint.setStyle(Paint.Style.STROKE);
        mBackgroundStrokePaint.setStrokeWidth(1.5f * density);
        mBackgroundStrokePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        mChecked = false;

        for (int s: state) {
            if (s == android.R.attr.state_checked) {
                mChecked = true;
                break;
            }
        }

        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        mHelper.set(getBounds());

        float size = mHelper.height() * HEIGHT_RATIO;
        float offset = (mHelper.height() - size) / 2;

        mHelper.right = mHelper.left + size;
        mHelper.top += offset;
        mHelper.bottom -= offset;

        canvas.drawRoundRect(mHelper, mCorners, mCorners, mFillPaint);

        if (mChecked) {
            canvas.drawLine(
                    mHelper.left + mHelper.width() / 5,
                    mHelper.top + mHelper.height() / 2,
                    mHelper.left + mHelper.width() * 2 / 5,
                    mHelper.top + mHelper.height() * 3 / 4,
                    mBackgroundStrokePaint);

            canvas.drawLine(
                    mHelper.left + mHelper.width() * 2 / 5,
                    mHelper.top + mHelper.height() * 3 / 4,
                    mHelper.left + mHelper.width() * 4 / 5,
                    mHelper.top + mHelper.height() / 4,
                    mBackgroundStrokePaint);
        }
        else {
            mHelper.offset(mBackgroundStrokePaint.getStrokeWidth(), mBackgroundStrokePaint.getStrokeWidth());
            mHelper.right -= 2 * mBackgroundStrokePaint.getStrokeWidth();
            mHelper.bottom -= 2 * mBackgroundStrokePaint.getStrokeWidth();

            canvas.drawRoundRect(mHelper, mCorners / 2, mCorners / 2, mBackgroundFillPaint);
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
