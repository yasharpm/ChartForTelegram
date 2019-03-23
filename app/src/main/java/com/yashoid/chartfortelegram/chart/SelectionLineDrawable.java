package com.yashoid.chartfortelegram.chart;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.List;

public class SelectionLineDrawable extends Drawable implements HorizontalMeasurementInfo.OnHorizontalMeasurementsChangedListener {

    public static class SelectionInfo {

        public final float x;
        public final long time;
        public final List<SelectionIntersectionInfo> intersections = new ArrayList<>();

        private SelectionInfo(float x, long time) {
            this.x = x;
            this.time = time;
        }

    }

    public static class SelectionIntersectionInfo {

        public final Chart chart;
        public final ChartLine chartLine;
        public final float y;
        public final int value;

        private final ChartLineDrawable drawable;

        private SelectionIntersectionInfo(ChartLineDrawable line, float y, int value) {
            drawable = line;

            chartLine = line.getChartLine();
            chart = chartLine.getChart();
            this.y = y;
            this.value = value;
        }

    }

    public interface OnSelectionInfoChangedListener {

        void onSelectionInfoChanged(SelectionInfo selectionInfo);

    }

    private OnSelectionInfoChangedListener mOnSelectionInfoChangedListener = null;

    private Paint mLinePaint;
    private Paint mBackgroundPaint;

    private float mRadius;

    private List<ChartLineDrawable> mChartLines = new ArrayList<>();

    private int mSelectedIndex = -1;

    private HorizontalMeasurementInfo mHorizontalMeasurementsInfo;

    private boolean mSelectionInfoInvalidated = true;
    private SelectionInfo mSelectionInfo = null;

    public SelectionLineDrawable() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setOnSelectionInfoChangedListener(OnSelectionInfoChangedListener listener) {
        mOnSelectionInfoChangedListener = listener;
    }

    public void setLineWidth(float width) {
        mLinePaint.setStrokeWidth(width);

        invalidateSelf();
    }

    public void setLineColor(int color) {
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

        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    @Override
    public void onHorizontalMeasurementsChanged() {
        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    public void onMaxValueChanged() {
        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    public void addChartLineDrawable(ChartLineDrawable drawable) {
        mChartLines.add(drawable);

        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    public void removeChartLineDrawable(ChartLineDrawable drawable) {
        mChartLines.remove(drawable);

        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;

        mSelectionInfoInvalidated = true;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mSelectionInfoInvalidated) {
            updateSelectionInfo();

            mSelectionInfoInvalidated = false;
        }

        if (mSelectionInfo == null) {
            return;
        }

        final Rect bounds = getBounds();

        final float x = mSelectionInfo.x;

        canvas.drawLine(x, bounds.top, x, bounds.bottom, mLinePaint);

        for (SelectionIntersectionInfo info: mSelectionInfo.intersections) {
            canvas.drawCircle(x, info.y, mRadius, mBackgroundPaint);
        }

        for (SelectionIntersectionInfo info: mSelectionInfo.intersections) {
            canvas.drawCircle(x, info.y, mRadius, info.drawable.getPaint());
        }
    }

    private void updateSelectionInfo() {
        if (mSelectedIndex == -1 || mHorizontalMeasurementsInfo == null) {
            mSelectionInfo = null;

            notifySelectionInfoChanged();
            return;
        }

        float x = mHorizontalMeasurementsInfo.getXForIndex(mSelectedIndex);

        mSelectionInfo = new SelectionInfo(x, mHorizontalMeasurementsInfo.getTimestamps()[mSelectedIndex]);

        for (ChartLineDrawable line: mChartLines) {
            int lineIndex = mHorizontalMeasurementsInfo.getChartIndexForIndex(mSelectedIndex, line.getChartLine().getChart());

            if (lineIndex == -1) {
                continue;
            }

            float y = line.getY(lineIndex);

            mSelectionInfo.intersections.add(new SelectionIntersectionInfo(line, y, line.getChartLine().getValues()[lineIndex]));
        }

        notifySelectionInfoChanged();
    }

    private void notifySelectionInfoChanged() {
        if (mOnSelectionInfoChangedListener != null) {
            mOnSelectionInfoChangedListener.onSelectionInfoChanged(mSelectionInfo);
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
