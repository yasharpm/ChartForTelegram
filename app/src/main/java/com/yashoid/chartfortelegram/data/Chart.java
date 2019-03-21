package com.yashoid.chartfortelegram.data;

public class Chart {

    private long[] mTimestamps;
    private ChartLine[] mLines;

    private int mMaxValue = 0;

    protected Chart(long[] timestamps, ChartLine[] lines) {
        mTimestamps = timestamps;
        mLines = lines;

        for (ChartLine line: mLines) {
            mMaxValue = Math.max(mMaxValue, line.getMaxValue());
        }
    }

    public ChartLine[] getLines() {
        return mLines;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public long[] getTimestamps() {
        return mTimestamps;
    }

    public long getStartTime() {
        return mTimestamps[0];
    }

    public long getEndTime() {
        return mTimestamps[mTimestamps.length - 1];
    }

    public int getMaxValueInRange(long start, long end) {
        int maxValue = 0;

        for (ChartLine line: mLines) {
            maxValue = Math.max(maxValue, line.getMaxValueInRange(mTimestamps, start, end));
        }

        return maxValue;
    }

}
