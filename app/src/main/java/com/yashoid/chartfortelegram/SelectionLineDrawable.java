package com.yashoid.chartfortelegram;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class SelectionLineDrawable extends Drawable implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    private Paint mLinePaint;
    private Paint mBackgroundPaint;

    private float mRadius;

    private List<ChartDrawable> mChartLines = new ArrayList<>();

    private int mSelectedIndex = -1;

    private HorizontalMeasurementInfo mHorizontalMeasurementsInfo;
    private int mMaxValue;

    public SelectionLineDrawable() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setLineStyle(float width, int color) {
        mLinePaint.setStrokeWidth(width);
        mLinePaint.setColor(color);

        invalidateSelf();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);

        invalidateSelf();
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public void setHorizontalMeasurementsInfo(HorizontalMeasurementInfo horizontalMeasurementsInfo) {
        if (mHorizontalMeasurementsInfo != null) {
            mHorizontalMeasurementsInfo.removeOnHorizontalMeasurementsChangedListener(this);
        }

        mHorizontalMeasurementsInfo = horizontalMeasurementsInfo;

        if (mHorizontalMeasurementsInfo != null) {
            mHorizontalMeasurementsInfo.addOnHorizontalMeasurementsChangedListener(this);
        }

        invalidateSelf();
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        invalidateSelf();
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;

        invalidateSelf();
    }

    public void setChartLines(List<ChartDrawable> lines) {
        mChartLines.clear();

        mChartLines.addAll(lines);

        invalidateSelf();
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSelectedIndex == -1 || mHorizontalMeasurementsInfo == null) {
            return;
        }

        final Rect bounds = getBounds();

        final float x = mHorizontalMeasurementsInfo.getXForIndex(mSelectedIndex);

        canvas.drawLine(x, bounds.top, x, bounds.bottom, mLinePaint);

        for (ChartDrawable line: mChartLines) {
            int lineIndex = mHorizontalMeasurementsInfo.getChartIndexForIndex(mSelectedIndex, line.getChartLine().getChart());

            if (lineIndex == -1) {
                continue;
            }

            float y = line.getY(lineIndex);

            canvas.drawCircle(x, y, mRadius, mBackgroundPaint);
        }

        for (ChartDrawable line: mChartLines) {
            int lineIndex = mHorizontalMeasurementsInfo.getChartIndexForIndex(mSelectedIndex, line.getChartLine().getChart());

            if (lineIndex == -1) {
                continue;
            }

            float y = line.getY(lineIndex);

            canvas.drawCircle(x, y, mRadius, line.getPaint());
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
