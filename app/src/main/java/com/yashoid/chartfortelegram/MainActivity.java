package com.yashoid.chartfortelegram;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;
import com.yashoid.chartfortelegram.data.Charts;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements AreaSelectorView.OnSelectedAreaChangedListener {

    private ChartView mChartMain;
    private ChartView mChartMap;
    private AreaSelectorView mAreaSelector;
    private ViewGroup mChoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartMain = findViewById(R.id.chart_main);
        mChartMap = findViewById(R.id.chart_map);
        mAreaSelector = findViewById(R.id.areaselector);
        mChoices = findViewById(R.id.choices);

        mAreaSelector.setOnSelectedAreaChangedListener(this);

        mChartMain.setOnTimeSelectedListener(new ChartView.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int timeIndex) {
                mChartMain.setSelectedTimeIndex(timeIndex);
            }
        });

        go();
    }

    @Override
    public void onSelectedAreaChanged(long start, long end) {
        mChartMain.setSelectedArea(start, end);
    }

    private void go() {
        try {
            List<Chart> charts = Charts.readChartsFromAssets(this, "chart_data.json");

            Chart chart = charts.get(0);

            mAreaSelector.setRange(chart.getStartTime(), chart.getEndTime());

            mChartMain.setChart(chart);
            mChartMap.setChart(chart);

            ChartLine[] lines = chart.getLines();

            for (final ChartLine line: lines) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(line.getName());
                checkBox.setChecked(false);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mChartMain.addChartLine(line);
                            mChartMap.addChartLine(line);
                        }
                        else {
                            mChartMain.removeChartLine(line);
                            mChartMap.removeChartLine(line);
                        }
                    }
                });

                mChoices.addView(checkBox);
            }
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "Failed to read charts: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
