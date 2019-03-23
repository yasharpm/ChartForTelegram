package com.yashoid.chartfortelegram;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartTracker {

    public interface OnChartsChangedListener {

        void onChartAdded(Chart chart);

        void onChartRemoved(Chart chart);

    }

    public interface OnChartLinesChangedListener {

        void onChartLineAdded(ChartLine chartLine);

        void onChartLineRemoved(ChartLine chartLine);

    }

    private List<OnChartsChangedListener> mChartChangedListeners = new ArrayList<>();
    private List<OnChartLinesChangedListener> mChartLineChangedListeners = new ArrayList<>();

    private HashMap<Chart, List<ChartLine>> mCharts = new HashMap<>();
    private List<ChartLine> mChartLines = new ArrayList<>();

    public ChartTracker() {

    }

    public void addOnChartsChangedListener(OnChartsChangedListener listener) {
        mChartChangedListeners.add(listener);
    }

    public void removeOnChartsChangedListener(OnChartsChangedListener listener) {
        mChartChangedListeners.remove(listener);
    }

    public void addOnChartLinesChangedListener(OnChartLinesChangedListener listener) {
        mChartLineChangedListeners.add(listener);
    }

    public void removeOnChartLinesChangedListener(OnChartLinesChangedListener listener) {
        mChartLineChangedListeners.add(listener);
    }

    public void addChartLine(ChartLine chartLine) {
        final Chart chart = chartLine.getChart();

        List<ChartLine> lines = mCharts.get(chart);

        if (lines == null) {
            lines = new ArrayList<>();

            mCharts.put(chart, lines);

            notifyChartAdded(chart);
        }

        lines.add(chartLine);

        mChartLines.add(chartLine);

        notifyChartLineAdded(chartLine);
    }

    public void removeChartLine(ChartLine chartLine) {
        final Chart chart = chartLine.getChart();

        List<ChartLine> lines = mCharts.get(chart);

        if (lines == null) {
            return;
        }

        if (!lines.remove(chartLine)) {
            return;
        }

        mChartLines.remove(chartLine);

        notifyChartLineRemoved(chartLine);

        if (lines.isEmpty()) {
            mCharts.remove(chart);

            notifyChartRemoved(chart);
        }
    }

    public List<Chart> getCharts() {
        return new ArrayList<>(mCharts.keySet());
    }

    public List<ChartLine> getChartLines() {
        return mChartLines;
    }

    private void notifyChartAdded(Chart chart) {
        List<OnChartsChangedListener> listeners = new ArrayList<>(mChartChangedListeners);

        for (OnChartsChangedListener listener: listeners) {
            listener.onChartAdded(chart);
        }
     }

    private void notifyChartRemoved(Chart chart) {
        List<OnChartsChangedListener> listeners = new ArrayList<>(mChartChangedListeners);

        for (OnChartsChangedListener listener: listeners) {
            listener.onChartRemoved(chart);
        }
    }

    private void notifyChartLineAdded(ChartLine chartLine) {
        List<OnChartLinesChangedListener> listeners = new ArrayList<>(mChartLineChangedListeners);

        for (OnChartLinesChangedListener listener: listeners) {
            listener.onChartLineAdded(chartLine);
        }
    }

    private void notifyChartLineRemoved(ChartLine chartLine) {
        List<OnChartLinesChangedListener> listeners = new ArrayList<>(mChartLineChangedListeners);

        for (OnChartLinesChangedListener listener: listeners) {
            listener.onChartLineRemoved(chartLine);
        }
    }

}
