package com.yashoid.chartfortelegram;

import com.yashoid.chartfortelegram.data.Chart;

import java.util.ArrayList;
import java.util.List;

public class DefaultHorizontalMeasurementInfo implements HorizontalMeasurementInfo {

    private static final long ONE_HOUR = 3600_000L;
    private static final long ONE_DAY = 24L * ONE_HOUR;
    private static final long X_DIFF_UNIT = ONE_DAY;

    private Chart mChart = null;

    private int mWidth = 0;

    private long mStart = -1;
    private long mEnd = -1;

    private float mXDiff = 0;
    private float[] mXValues = null;

    private boolean mMeasurementsInvalidated = true;

    private List<OnHorizontalMeasurementsChangedListener> mListeners = new ArrayList<>();

    public DefaultHorizontalMeasurementInfo() {
    }

    public void setChart(Chart chart) {
        mChart = chart;

        mXValues = new float[mChart.getTimestamps().length];

        mMeasurementsInvalidated = true;

        notifyHorizontalMeasurementsChanged();
    }

    public void setRange(long start, long end) {
        if (mStart == start && mEnd == end) {
            return;
        }

        mStart = start;
        mEnd = end;

        mMeasurementsInvalidated = true;

        notifyHorizontalMeasurementsChanged();
    }

    public void onLaidOut(int width) {
        if (width == mWidth) {
            return;
        }

        mWidth = width;

        mMeasurementsInvalidated = true;

        notifyHorizontalMeasurementsChanged();
    }

    @Override
    public float getXForIndex(int index) {
        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        return mXValues[index];
    }

    @Override
    public float getXForTime(long time) {
        if (mChart == null) {
            return 0;
        }

        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        return (mXDiff / X_DIFF_UNIT * (time - mChart.getStartTime()));
    }

    private void updateMeasurements() {
        if (mChart == null) {
            return;
        }

        long start = mStart == -1 ? mChart.getStartTime() : mStart;
        long end = mEnd == -1 ? mChart.getEndTime() : mEnd;

        if (end == start) {
            return;
        }

        float xDiff = ((float) mWidth * X_DIFF_UNIT) / (end - start);

        if (mXDiff == xDiff) {
            return;
        }

        mXDiff = xDiff;

        final long[] timestamps = mChart.getTimestamps();

        for (int i = 0; i < timestamps.length; i++) {
            mXValues[i] = (mXDiff / X_DIFF_UNIT * (timestamps[i] - timestamps[0]));
        }
    }

    @Override
    public void getIndexesForRange(long start, long end, int[] indexes) {
        int startIndex = -1;
        int endIndex = -1;

        final long[] timestamps = mChart.getTimestamps();

        for (int i = 0; i < timestamps.length; i++) {
            if (timestamps[i] < start) {
                startIndex = i;
            }

            if (endIndex == -1 && timestamps[i] > end) {
                endIndex = i;
                break;
            }
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = timestamps.length;
        }

        indexes[0] = startIndex;
        indexes[1] = endIndex;
    }

    @Override
    public int getTimeIndexForX(float x) {
        return getTimeIndexForX(x,0, mXValues.length);
    }

    private int getTimeIndexForX(float x, int start, int end) {
        if (end - start < 2) {
            return start;
        }

        int mid = (start + end) / 2;

        if (mXValues[mid] > x) {
            return getTimeIndexForX(x, start, mid);
        }
        else if (mXValues[mid] < x) {
            return getTimeIndexForX(x, mid, end);
        }
        else {
            return mid;
        }
    }

    private void notifyHorizontalMeasurementsChanged() {
        List<OnHorizontalMeasurementsChangedListener> listeners = new ArrayList<>(mListeners);

        for (OnHorizontalMeasurementsChangedListener listener: listeners) {
            listener.onHorizontalMeasurementsChanged();
        }
    }

    @Override
    public void addOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener) {
        mListeners.remove(listener);
    }

}
