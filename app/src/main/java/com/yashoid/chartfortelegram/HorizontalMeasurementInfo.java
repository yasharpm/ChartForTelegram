package com.yashoid.chartfortelegram;

public interface HorizontalMeasurementInfo {

    interface OnHorizontalMeasurementsChangedListener {

        void onHorizontalMeasurementsChanged();

    }

    float getXForIndex(int index);

    float getXForTime(long time);

    void getIndexesForRange(long start, long end, int[] indexes);

    int getTimeIndexForX(float x);

    void addOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener);

    void removeOnHorizontalMeasurementsChangedListener(OnHorizontalMeasurementsChangedListener listener);

}
