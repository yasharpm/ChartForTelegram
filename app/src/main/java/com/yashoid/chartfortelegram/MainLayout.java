package com.yashoid.chartfortelegram;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainLayout extends ViewGroup {

    private View mToolbar;
    private View mFollowers;
    private View mMainChart;
    private View mMapChart;
    private View mAreaSelector;
    private View mChoices;
    private View mCredit;
    private View mBody;
    private View mInfoView;

    public MainLayout(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
//        setClipChildren(false);

        LayoutInflater.from(context).inflate(R.layout.main, this, true);

        mToolbar = findViewById(R.id.toolbar);
        mFollowers = findViewById(R.id.text_followers);
        mMainChart = findViewById(R.id.chart_main);
        mMapChart = findViewById(R.id.chart_map);
        mAreaSelector = findViewById(R.id.areaselector);
        mChoices = findViewById(R.id.choices);
        mCredit = findViewById(R.id.credit);
        mBody = findViewById(R.id.body);
        mInfoView = findViewById(R.id.infoview);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getWidth();
        final int height = getHeight();

        MarginLayoutParams params;

        mToolbar.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mToolbar.getLayoutParams().height, MeasureSpec.EXACTLY));
        mToolbar.layout(0, 0, width, mToolbar.getLayoutParams().height);

        params = (MarginLayoutParams) mFollowers.getLayoutParams();
        mFollowers.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mFollowers.layout(params.leftMargin, mToolbar.getBottom() + params.topMargin, params.leftMargin + mFollowers.getMeasuredWidth(), mToolbar.getBottom() + params.topMargin + mFollowers.getMeasuredHeight());

        params = (MarginLayoutParams) mMainChart.getLayoutParams();
        int mainHeight = (int) ((width - params.leftMargin - params.rightMargin) / 1.2f);
        mMainChart.layout(params.leftMargin, mFollowers.getBottom() + params.topMargin, width - params.rightMargin, mFollowers.getBottom() + params.topMargin + mainHeight);

        params = (MarginLayoutParams) mMapChart.getLayoutParams();
        mMapChart.layout(params.leftMargin, mMainChart.getBottom() + params.topMargin, width - params.rightMargin, mMainChart.getBottom() + params.topMargin + params.height);

        mAreaSelector.layout(mMapChart.getLeft(), mMapChart.getTop(), mMapChart.getRight(), mMapChart.getBottom());

        params = (MarginLayoutParams) mChoices.getLayoutParams();
        mChoices.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - params.bottomMargin - (mMapChart.getBottom() + params.topMargin), MeasureSpec.EXACTLY));
        mChoices.layout(0, mMapChart.getBottom() + params.topMargin, width, height - params.bottomMargin);

        mCredit.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mCredit.layout(0, height - mCredit.getMeasuredHeight(), width, height);

        mBody.layout(0, mToolbar.getBottom(), width, mChoices.getBottom());

        mInfoView.layout(mMainChart.getLeft(), mMainChart.getTop(), mMapChart.getRight(), mMapChart.getBottom());
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

}
