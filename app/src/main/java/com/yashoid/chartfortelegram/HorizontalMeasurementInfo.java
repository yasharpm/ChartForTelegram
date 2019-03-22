package com.yashoid.chartfortelegram;

import com.yashoid.chartfortelegram.data.Chart;

public interface HorizontalMeasurementInfo {

    interface OnHorizontalMeasurementsChangedListener {

        void onHorizontalMeasurementsChanged();

    }

    interface OnRangeChangedListener {

        void onRangeChanged(long start, long end);

    }

    void addChart(Chart chart);

    void removeChart(Chart chart);

    long[] getTimestamps();

    int getChartIndexForIndex(int index, Chart chart);

    float getXForIndex(Chart chart, int index);

    float getXForIndex(int index);

    float getXForTime(long time);

    void getIndexesForRange(long start, long end, int[] indexes);

    int getTimeIndexForX(float x);

    void addOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener);

    void removeOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener);

    void setOnRangeChangedListener(OnRangeChangedListener listener);

}
