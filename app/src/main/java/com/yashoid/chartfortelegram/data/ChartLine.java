package com.yashoid.chartfortelegram.data;

import android.graphics.Paint;

public class ChartLine {

    private Chart mChart;

    private String mName;

    private int mColor;

    private int[] mValues;

    private CartesianTree mSearchTree;
    private int mMaxValue;

    protected ChartLine(String name, int color, int[] values) {
        mName = name;
        mColor = color;
        mValues = values;

        mSearchTree = new CartesianTree(values);

        mMaxValue = mSearchTree.getMaxValue();
    }

    protected void setChart(Chart chart) {
        mChart = chart;
    }

    public Chart getChart() {
        return mChart;
    }

    public String getName() {
        return mName;
    }

    public int getColor() {
        return mColor;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int[] getValues() {
        return mValues;
    }

    public int getMaxValueInRange(int start, int end) {
        return mSearchTree.findMaximumValueInRange(start, end);
    }

    public int getMaxValueInRange(long[] base, long start, long end) {
        return mSearchTree.findMaximumValueInRange(base, start, end);
    }

    public void applyPaint(Paint paint) {
        paint.setColor(mColor);
    }

}
