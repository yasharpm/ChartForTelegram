package com.yashoid.chartfortelegram;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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

    private int mTextColor;
    private int mBackgroundColor;

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

        boolean first = true;
        LayoutParams dividerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (0.75f * density));
        dividerParams.leftMargin = (int) (40 * density);

        for (Chart chart: charts) {
            ChartLine[] lines = chart.getLines();

            for (final ChartLine line: lines) {
                if (!first) {
                    View divider = new View(getContext());
                    divider.setBackgroundColor(0xff000000);
                    divider.setLayoutParams(dividerParams);
                    addView(divider);
                }

                first = false;

                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setTag(line.getColor());

                CheckDrawable drawable = new CheckDrawable(line.getColor(), density);
                drawable.setBackgroundColor(mBackgroundColor);

                checkBox.setButtonDrawable(drawable);
                checkBox.setText("Chart " + chartIndex + " / " + line.getName());
                checkBox.setTypeface(Typeface.DEFAULT_BOLD);
                checkBox.setTextColor(mTextColor);
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

    public void setColors(int textColor, int backgroundColor, int dividerColor) {
        mTextColor = textColor;
        mBackgroundColor = backgroundColor;

        final int count = getChildCount();

        final float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child instanceof CheckBox) {
                ((CheckBox) child).setTextColor(mTextColor);

                CheckDrawable drawable = new CheckDrawable((int) child.getTag(), density);
                drawable.setBackgroundColor(mBackgroundColor);
                ((CheckBox) child).setButtonDrawable(drawable);
            }
            else {
                child.setBackgroundColor(dividerColor);
            }
        }
    }

}
