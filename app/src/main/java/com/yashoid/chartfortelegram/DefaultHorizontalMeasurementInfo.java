package com.yashoid.chartfortelegram;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DefaultHorizontalMeasurementInfo implements HorizontalMeasurementInfo {

    private static final long ONE_HOUR = 3600_000L;
    private static final long ONE_DAY = 24L * ONE_HOUR;
    private static final long X_DIFF_UNIT = ONE_DAY;

    private HashMap<Chart, Integer> mCharts = new HashMap<>();

    private long[] mTimestamps = { };

    private int mWidth = 0;

    private long mStart = -1;
    private long mEnd = -1;

    private float mXDiff = 0;
    private float[] mXValues = null;

    private boolean mMeasurementsInvalidated = true;

    private List<OnHorizontalMeasurementsChangedListener> mListeners = new ArrayList<>();
    private OnRangeChangedListener mOnRangeChangedListener = null;

    public DefaultHorizontalMeasurementInfo() {

    }

    @Override
    public void addChart(Chart chart) {
        mCharts.put(chart, 0);

        measureRange();

        mMeasurementsInvalidated = true;

        notifyHorizontalMeasurementsChanged();
    }

    @Override
    public void removeChart(Chart chart) {
        mCharts.remove(chart);

        measureRange();

        mMeasurementsInvalidated = true;

        notifyHorizontalMeasurementsChanged();
    }

    @Override
    public long[] getTimestamps() {
        return mTimestamps;
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
    public int getChartIndexForIndex(int index, Chart chart) {
        Integer baseIndex = mCharts.get(chart);

        if (baseIndex == null) {
            return -1;
        }

        if (index < baseIndex) {
            return -1;
        }

        index -= baseIndex;

        if (index >= chart.getTimestamps().length) {
            return -1;
        }

        return index;
    }

    @Override
    public float getXForIndex(Chart chart, int index) {
        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        Integer baseIndex = mCharts.get(chart);

        if (baseIndex == null || baseIndex + index >= mXValues.length) {
            return 0;
        }

        return mXValues[baseIndex + index];
    }

    @Override
    public float getXForIndex(int index) {
        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        if (index >= mXValues.length) {
            return 0;
        }

        return mXValues[index];
    }

    @Override
    public float getXForTime(long time) {
        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        if (mTimestamps.length == 0) {
            return 0;
        }

        return (mXDiff / X_DIFF_UNIT * (time - mTimestamps[0]));
    }

    private void measureRange() {
        if (mCharts.isEmpty()) {
            return;
        }

        int[] indexes = new int[mCharts.size()];

        List<Long> timestamps = new ArrayList<>();

        List<Chart> charts = new ArrayList<>(mCharts.keySet());

        // Assuming no chart is empty.
        while (thereAreUnfinishedCharts(charts, indexes)) {
            long timestamp = findMinimumTimestamp(charts, indexes);

            timestamps.add(timestamp);

            passTimestampOnCharts(charts, indexes, timestamp, timestamps.size() - 1);
        }

        mTimestamps = new long[timestamps.size()];
        mXValues = new float[mTimestamps.length];
        mXDiff = 0;

        int index = 0;

        for (Long timestamp: timestamps) {
            mTimestamps[index++] = timestamp;
        }

        if (mTimestamps.length == 0) {
            return;
        }

        boolean rangeChanged = false;

        if (mStart != -1 && mStart < mTimestamps[0]) {
            mStart = mTimestamps[0];

            rangeChanged = true;
        }

        if (mEnd != -1 && mEnd > mTimestamps[mTimestamps.length - 1]) {
            mEnd = mTimestamps[mTimestamps.length - 1];

            rangeChanged = true;
        }

        if (rangeChanged) {
            notifyOnRangeChanged();
        }
    }

    private void notifyOnRangeChanged() {
        if (mOnRangeChangedListener != null) {
            mOnRangeChangedListener.onRangeChanged(mStart, mEnd);
        }
    }

    private boolean thereAreUnfinishedCharts(List<Chart> charts, int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] < charts.get(i).getTimestamps().length) {
                return true;
            }
        }

        return false;
    }

    private long findMinimumTimestamp(List<Chart> charts, int[] indexes) {
        long minimum = Long.MAX_VALUE;

        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == charts.get(i).getTimestamps().length) {
                continue;
            }

            minimum = Math.min(minimum, charts.get(i).getTimestamps()[indexes[i]]);
        }

        return minimum;
    }

    private void passTimestampOnCharts(List<Chart> charts, int[] indexes, long timestamp, int timestampIndex) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == charts.get(i).getTimestamps().length) {
                continue;
            }

            if (charts.get(i).getTimestamps()[indexes[i]] == timestamp) {
                if (indexes[i] == 0) {
                    mCharts.put(charts.get(i), timestampIndex);
                }

                indexes[i]++;
            }
        }
    }

    private void updateMeasurements() {
        if (mTimestamps.length == 0) {
            return;
        }

        long start = mStart == -1 ? mTimestamps[0] : mStart;
        long end = mEnd == -1 ? mTimestamps[mTimestamps.length - 1] : mEnd;

        if (end == start) {
            return;
        }

        float xDiff = ((float) mWidth * X_DIFF_UNIT) / (end - start);

        if (mXDiff == xDiff) {
            return;
        }

        mXDiff = xDiff;

        for (int i = 0; i < mTimestamps.length; i++) {
            mXValues[i] = (mXDiff / X_DIFF_UNIT * (mTimestamps[i] - mTimestamps[0]));
        }
    }

    @Override
    public void getIndexesForRange(long start, long end, int[] indexes) {
        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < mTimestamps.length; i++) {
            if (mTimestamps[i] < start) {
                startIndex = i;
            }

            if (endIndex == -1 && mTimestamps[i] > end) {
                endIndex = i;
                break;
            }
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = mTimestamps.length;
        }

        indexes[0] = startIndex;
        indexes[1] = endIndex;

        // TODO
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

    @Override
    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        mOnRangeChangedListener = listener;
    }

}
