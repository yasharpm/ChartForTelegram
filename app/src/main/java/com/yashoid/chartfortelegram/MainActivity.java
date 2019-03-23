package com.yashoid.chartfortelegram;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yashoid.chartfortelegram.chart.MainChartView;
import com.yashoid.chartfortelegram.chart.SelectionLineDrawable;
import com.yashoid.chartfortelegram.chart.SimpleChartView;
import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;
import com.yashoid.chartfortelegram.data.Charts;
import com.yashoid.chartfortelegram.selectioninfo.InfoViewHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yashar PourMohammad
 *
 * Github profile : https://github.com/yasharpm
 * Project Github link : https://github.com/yasharpm/ChartForTelegram
 *
 */
public class MainActivity extends Activity implements AreaSelectorView.OnSelectedAreaChangedListener,
        SelectionLineDrawable.OnSelectionInfoChangedListener, ChartTracker.OnChartsChangedListener,
        MainChartView.OnTimeSelectedListener {

    private MainChartView mChartMain;
    private SimpleChartView mChartMap;
    private AreaSelectorView mAreaSelector;
    private ChartSelector mChartSelector;
    private InfoViewHolder mInfoView;

    private ChartTracker mChartTracker;

    private boolean mDay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChartMain = findViewById(R.id.chart_main);
        mChartMap = findViewById(R.id.chart_map);
        mAreaSelector = findViewById(R.id.areaselector);
        mChartSelector = findViewById(R.id.chartselector);
        mInfoView = findViewById(R.id.infoview);

        TextView credit = findViewById(R.id.credit);
        credit.setMovementMethod(LinkMovementMethod.getInstance());
        credit.setText(Html.fromHtml("Moon icon made by <a href=\"https://www.freepik.com/\" title=\"Freepik\">Freepik</a> from <a href=\"https://www.flaticon.com/\" \t\t\t    title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" \t\t\t    title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a>"));

        mChartTracker = new ChartTracker();
        mChartTracker.addOnChartsChangedListener(this);

        mChartMain.setChartTracker(mChartTracker);
        mChartMap.setChartTracker(mChartTracker);

        mAreaSelector.setOnSelectedAreaChangedListener(this);

        mChartMain.setOnTimeSelectedListener(this);
        mChartMain.setOnSelectionInfoChangedListener(this);

        mChartSelector.setOnChartLineSelectionChangedListener(new ChartSelector.OnChartLineSelectionChangedListener() {

            @Override
            public void onChartLineSelected(ChartLine chartLine) {
                mChartTracker.addChartLine(chartLine);

                mChartMain.setSelectedArea(mAreaSelector.getSelectionStart(), mAreaSelector.getSelectionEnd());
            }

            @Override
            public void onChartLineUnselected(ChartLine chartLine) {
                mChartTracker.removeChartLine(chartLine);

                mChartMain.setSelectedArea(mAreaSelector.getSelectionStart(), mAreaSelector.getSelectionEnd());
            }

        });

        findViewById(R.id.button_lightmode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDay) {
                    switchNightMode();
                }
                else {
                    switchDayMode();
                }

                mDay = !mDay;
            }
        });

        switchDayMode();

        List<Chart> charts = getCharts();

        if (charts != null) {
            mChartSelector.setCharts(charts);
        }
    }

    @Override
    public void onTimeSelected(int timeIndex) {
        mChartMain.setSelectedTimeIndex(timeIndex);
    }

    @Override
    public void onChartAdded(Chart chart) {
        updateAreaSelectorRange();
    }

    @Override
    public void onChartRemoved(Chart chart) {
        updateAreaSelectorRange();
    }

    private void updateAreaSelectorRange() {
        mChartMain.setSelectedTimeIndex(-1);

        List<Chart> charts = mChartTracker.getCharts();

        if (charts.isEmpty()) {
            return;
        }

        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;

        for (Chart chart: charts) {
            start = Math.min(start, chart.getStartTime());
            end = Math.max(end, chart.getEndTime());
        }

        mAreaSelector.setRange(start, end);
    }

    @Override
    public void onSelectedAreaChanged(long start, long end) {
        mChartMain.setSelectedArea(start, end);
    }

    @Override
    public void onSelectionInfoChanged(SelectionLineDrawable.SelectionInfo selectionInfo) {
        if (selectionInfo == null) {
            mInfoView.setVisibility(View.INVISIBLE);
        }
        else {
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setIntersectionInfo(selectionInfo, mChartMain.getXFix());
        }
    }

    private List<Chart> getCharts() {
        try {
            return Charts.readChartsFromAssets(this, "chart_data.json");
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "Failed to read charts: " + e.getMessage(), Toast.LENGTH_LONG).show();

            return null;
        }
    }

    private void switchNightMode() {
        Resources res = getResources();

        findViewById(R.id.root).setBackgroundColor(res.getColor(R.color.dark_background));
        findViewById(R.id.toolbar).setBackgroundColor(res.getColor(R.color.dark_toolbar));
        findViewById(R.id.body).setBackgroundColor(res.getColor(R.color.dark_body_background));
        ((TextView) findViewById(R.id.text_followers)).setTextColor(res.getColor(R.color.dark_title));
        mChartMain.setLegendColor(res.getColor(R.color.dark_chart_legend));
        mChartMain.setScaleLinesColor(res.getColor(R.color.dark_scale));
        mChartMain.setSelectionLineColor(res.getColor(R.color.dark_selection));
        mChartMain.setSelectionBackgroundColor(res.getColor(R.color.dark_body_background));
        mAreaSelector.setCoverColor(res.getColor(R.color.dark_area_solid));
        mAreaSelector.setEdgeColor(res.getColor(R.color.dark_area_border));
        mChartSelector.setColors(
                res.getColor(R.color.dark_textcolor),
                res.getColor(R.color.dark_body_background),
                res.getColor(R.color.dark_chartline_divider)
        );
        mInfoView.setColors(res.getColor(R.color.dark_textcolor), res.getColor(R.color.dark_body_background));
    }

    private void switchDayMode() {
        Resources res = getResources();

        findViewById(R.id.root).setBackgroundColor(res.getColor(R.color.light_background));
        findViewById(R.id.toolbar).setBackgroundColor(res.getColor(R.color.light_toolbar));
        findViewById(R.id.body).setBackgroundColor(res.getColor(R.color.light_body_background));
        ((TextView) findViewById(R.id.text_followers)).setTextColor(res.getColor(R.color.light_title));
        mChartMain.setLegendColor(res.getColor(R.color.light_chart_legend));
        mChartMain.setScaleLinesColor(res.getColor(R.color.light_scale));
        mChartMain.setSelectionLineColor(res.getColor(R.color.light_selection));
        mChartMain.setSelectionBackgroundColor(res.getColor(R.color.light_body_background));
        mAreaSelector.setCoverColor(res.getColor(R.color.light_area_solid));
        mAreaSelector.setEdgeColor(res.getColor(R.color.light_area_border));
        mChartSelector.setColors(
                res.getColor(R.color.light_textcolor),
                res.getColor(R.color.light_body_background),
                res.getColor(R.color.light_chartline_divider)
        );
        mInfoView.setColors(res.getColor(R.color.light_textcolor), res.getColor(R.color.light_body_background));
    }

}
