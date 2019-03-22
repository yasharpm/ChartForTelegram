package com.yashoid.chartfortelegram;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.yashoid.chartfortelegram.checkbox.CheckDrawable;
import com.yashoid.chartfortelegram.data.Chart;
import com.yashoid.chartfortelegram.data.ChartLine;

import java.util.List;

public class ChartSelector extends LinearLayout {

    public interface OnChartLineSelectionChangedListener {

        void onChartLineSelected(ChartLine line);

        void onChartLineUnselected(ChartLine line);

    }

    private OnChartLineSelectionChangedListener mOnChartLineSelectionChangedListener = null;

    public ChartSelector(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ChartSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ChartSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setOrientation(VERTICAL);
    }

    public void setOnChartLineSelectionChangedListener(OnChartLineSelectionChangedListener listener) {
        mOnChartLineSelectionChangedListener = listener;
    }

    public void setCharts(List<Chart> charts) {
        removeAllViews();

        final float density = getResources().getDisplayMetrics().density;

        int chartIndex = 0;

        for (Chart chart: charts) {
            ChartLine[] lines = chart.getLines();

            for (final ChartLine line: lines) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setButtonDrawable(new CheckDrawable(line.getColor(), density));
                checkBox.setText("Chart " + chartIndex + " / " + line.getName());
                checkBox.setChecked(false);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (mOnChartLineSelectionChangedListener == null) {
                            return;
                        }

                        if (isChecked) {
                            mOnChartLineSelectionChangedListener.onChartLineSelected(line);
                        }
                        else {
                            mOnChartLineSelectionChangedListener.onChartLineUnselected(line);
                        }
                    }
                });

                addView(checkBox);
            }

            chartIndex++;
        }
    }

}
