package com.yashoid.chartfortelegram.selectioninfo;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.yashoid.chartfortelegram.R;
import com.yashoid.chartfortelegram.SelectionLineDrawable;

public class InfoViewHolder extends ViewGroup {

    private InfoView mInfoView;
    private View mBackground;

    private int mPadding;

    private SelectionLineDrawable.SelectionInfo mInfo;
    private float mX;

    public InfoViewHolder(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public InfoViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public InfoViewHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        float density = getResources().getDisplayMetrics().density;

        mPadding = (int) (8 * density);

        mInfoView = new InfoView(context);
        mBackground = new View(context);

        mBackground.setBackgroundResource(R.drawable.selection_background);

        addView(mBackground);
        addView(mInfoView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBackground.setElevation(density * 4);
            mInfoView.setElevation(density * 4);
        }
    }

    public void setColors(int textColor, int backgroundColor) {
        mBackground.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN);

        mInfoView.setTextColor(textColor);
    }

    public void setIntersectionInfo(SelectionLineDrawable.SelectionInfo info, float xFix) {
        mInfo = info;

        mX = mInfo.x + xFix;

        mInfoView.setSelectionInfo(mInfo);

        placeViews();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mInfo == null) {
            return;
        }

        placeViews();
    }

    private void placeViews() {
        mInfoView.measure(0, 0);

        int left;
        int top;

        if (mX > getWidth() / 2) {
            left = (int) (mX - mInfoView.getMeasuredWidth() - 2 * mPadding - getResources().getDisplayMetrics().density * 12);
            top = mPadding;
        }
        else {
            left = (int) (mX + getResources().getDisplayMetrics().density * 12);
            top = mPadding;
        }

        if (left < mPadding) {
            left = mPadding;
        }
        else if (left + mInfoView.getMeasuredWidth() + 3 * mPadding > getWidth()) {
            left = getWidth() - mInfoView.getMeasuredWidth() - 3 * mPadding;
        }

        mBackground.layout(left, top, left + mInfoView.getMeasuredWidth() + 2 * mPadding, top + 2 * mPadding + mInfoView.getMeasuredHeight());
        mInfoView.layout(left + mPadding, top + mPadding, left + mPadding + mInfoView.getMeasuredWidth(), top + 2 * mPadding + mInfoView.getMeasuredHeight());
    }

}
