package com.yashoid.chartfortelegram;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yashoid.chartfortelegram.checkbox.CheckDrawable;
import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;
import com.yashoid.chartfortelegram.data.Charts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements AreaSelectorView.OnSelectedAreaChangedListener {

    private MainChartView mChartMain;
    private SimpleChartView mChartMap;
    private AreaSelectorView mAreaSelector;
    private ChartSelector mChartSelector;

    private HashMap<Chart, List<ChartLine>> mCharts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartMain = findViewById(R.id.chart_main);
        mChartMap = findViewById(R.id.chart_map);
        mAreaSelector = findViewById(R.id.areaselector);
        mChartSelector = findViewById(R.id.chartselector);

        TextView credit = findViewById(R.id.credit);
        credit.setMovementMethod(LinkMovementMethod.getInstance());
        credit.setText(Html.fromHtml("Moon icon made by <a href=\"https://www.freepik.com/\" title=\"Freepik\">Freepik</a> from <a href=\"https://www.flaticon.com/\" \t\t\t    title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" \t\t\t    title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a>"));

        mAreaSelector.setOnSelectedAreaChangedListener(this);

        mChartMain.setOnTimeSelectedListener(new MainChartView.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int timeIndex) {
                mChartMain.setSelectedTimeIndex(timeIndex);
            }
        });

        mChartSelector.setOnChartLineSelectionChangedListener(new ChartSelector.OnChartLineSelectionChangedListener() {

            @Override
            public void onChartLineSelected(ChartLine line) {
                List<ChartLine> lines = mCharts.get(line.getChart());

                if (lines == null) {
                    lines = new ArrayList<>();
                    mCharts.put(line.getChart(), lines);

                    updateAreaSelectorRange();
                }

                lines.add(line);

                mChartMain.addChartLine(line);
                mChartMap.addChartLine(line);

                mChartMain.setSelectedArea(mAreaSelector.getSelectionStart(), mAreaSelector.getSelectionEnd());
            }

            @Override
            public void onChartLineUnselected(ChartLine line) {
                mChartMain.removeChartLine(line);
                mChartMap.removeChartLine(line);

                List<ChartLine> lines = mCharts.get(line.getChart());

                if (lines != null && lines.remove(line) && lines.isEmpty()) {
                    mCharts.remove(line.getChart());

                    updateAreaSelectorRange();
                }

                mChartMain.setSelectedArea(mAreaSelector.getSelectionStart(), mAreaSelector.getSelectionEnd());
            }

        });

        go();
    }

    private void updateAreaSelectorRange() {
        mChartMain.setSelectedTimeIndex(-1);

        if (mCharts.isEmpty()) {
            return;
        }

        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;

        for (Chart chart: mCharts.keySet()) {
            start = Math.min(start, chart.getStartTime());
            end = Math.max(end, chart.getEndTime());
        }

        mAreaSelector.setRange(start, end);
    }

    @Override
    public void onSelectedAreaChanged(long start, long end) {
        mChartMain.setSelectedArea(start, end);
    }

    private void go() {
        try {
            List<Chart> charts = Charts.readChartsFromAssets(this, "chart_data.json");

            mChartSelector.setCharts(charts);
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "Failed to read charts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
