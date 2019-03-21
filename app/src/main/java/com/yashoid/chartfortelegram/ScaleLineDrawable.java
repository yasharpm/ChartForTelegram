package com.yashoid.chartfortelegram;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class ScaleLineDrawable extends Drawable {

    private Paint mLabelPaint;
    private Paint mLinePaint;

    private int mMaxValue = 0;
    private int mBaseHeight = 0;

    private float mLabelHeight;
    private float[] mLines = new float[24];

    private float mLabelMargin = 0;
    private float mLabelsTopRatio = 0;
    private String[] mValues = { "0", "", "", "", "", "" };

    private boolean mLabelPaintInvalidated = true;
    private boolean mLinesInvalidated = true;
    private boolean mMaxValueInvalidated = true;

    public ScaleLineDrawable() {
        mLabelPaint = new Paint();
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setTextAlign(Paint.Align.LEFT);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
    }

    public ScaleLineDrawable(ScaleLineDrawable source) {
        this();

        mLabelPaint.setTextSize(source.mLabelPaint.getTextSize());
        mLabelPaint.setColor(source.mLabelPaint.getColor());
        mLabelPaint.setTypeface(source.mLabelPaint.getTypeface());

        mLinePaint.setStrokeWidth(source.mLinePaint.getStrokeWidth());
        mLinePaint.setColor(source.mLinePaint.getColor());

        mLabelMargin = source.mLabelMargin;
    }

    public void setLabelStyle(float textSize, int color) {
        mLabelPaint.setColor(color);

        if (mLabelPaint.getTextSize() != textSize) {
            mLabelPaint.setTextSize(textSize);

            mLabelPaintInvalidated = true;
        }

        invalidateSelf();
    }

    public void setLabelTypeFace(Typeface typeFace) {
        mLabelPaint.setTypeface(typeFace);

        mLabelPaintInvalidated = true;

        invalidateSelf();
    }

    public void setLineStyle(float width, int color) {
        mLinePaint.setStrokeWidth(width);
        mLinePaint.setColor(color);

        invalidateSelf();
    }

    public void setLabelMargin(float margin) {
        mLabelMargin = margin;

        mMaxValueInvalidated = true;
        mLinesInvalidated = true;

        invalidateSelf();
    }

    public void setMaxValue(int maxValue, int baseHeight) {
        if (maxValue == mMaxValue && baseHeight == mBaseHeight) {
            return;
        }

        mMaxValue = maxValue;
        mBaseHeight = baseHeight;

        mMaxValueInvalidated = true;
        mLinesInvalidated = true;

        invalidateSelf();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        for (int i = 0; i < 6; i++) {
            mLines[i * 4] = bounds.left;
            mLines[i * 4 + 2] = bounds.right;
        }

        mLinesInvalidated = true;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mLabelPaintInvalidated) {
            measureLabelPaint();

            mLabelPaintInvalidated = false;
        }

        if (mMaxValueInvalidated) {
            measureMaxValue();

            mMaxValueInvalidated = false;
        }

        if (mLinesInvalidated) {
            measureLines();

            mLinesInvalidated = false;
        }

        canvas.drawLines(mLines, mLinePaint);
        canvas.drawText(mValues[5], 0, mLines[1] - mLabelMargin, mLabelPaint);
        canvas.drawText(mValues[4], 0, mLines[5] - mLabelMargin, mLabelPaint);
        canvas.drawText(mValues[3], 0, mLines[9] - mLabelMargin, mLabelPaint);
        canvas.drawText(mValues[2], 0, mLines[13] - mLabelMargin, mLabelPaint);
        canvas.drawText(mValues[1], 0, mLines[17] - mLabelMargin, mLabelPaint);
        canvas.drawText(mValues[0], 0, mLines[21] - mLabelMargin, mLabelPaint);
    }

    private void measureLines() {
        final Rect bounds = getBounds();
        final int top = bounds.top;

        final float step = bounds.height() * mLabelsTopRatio / 5;

        float y = bounds.height();

        for (int i = 5; i >= 0; i--) {
            mLines[i * 4 + 1] = top + y;
            mLines[i * 4 + 3] = top + y;

            y -= step;
        }
    }

    private void measureMaxValue() {
        if (mBaseHeight == 0) {
            return;
        }

        float heightRatio = (float) mMaxValue / mBaseHeight;

        int maxValueStart = (int) (mMaxValue - heightRatio * (mLabelHeight + mLabelMargin));

        maxValueStart = maxValueStart / 10 * 10;

        mLabelsTopRatio = (float) maxValueStart / mMaxValue;

        mValues[1] = commaSeparated(maxValueStart / 5);
        mValues[2] = commaSeparated(2 * maxValueStart / 5);
        mValues[3] = commaSeparated(3 * maxValueStart / 5);
        mValues[4] = commaSeparated(4 * maxValueStart / 5);
        mValues[5] = commaSeparated(maxValueStart);
    }

    private String commaSeparated(int value) {
        int current = value % 1000;
        int thousands = value / 1000;

        return (thousands == 0) ? "" + current : commaSeparated(thousands) + "," + fixLength(current);
    }

    private String fixLength(int value) {
        String s = "" + value;

        while (s.length() < 3) {
            s = "0" + s;
        }

        return s;
    }

    private void measureLabelPaint() {
        Paint.FontMetrics fm = mLabelPaint.getFontMetrics();

        mLabelHeight = fm.bottom - fm.top;
    }

    @Override
    public void setAlpha(int alpha) {
        mLinePaint.setAlpha(alpha);
        mLabelPaint.setAlpha(alpha);

        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

}
