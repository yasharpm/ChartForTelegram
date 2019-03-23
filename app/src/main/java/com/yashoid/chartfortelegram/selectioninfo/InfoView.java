package com.yashoid.chartfortelegram.selectioninfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.yashoid.chartfortelegram.chart.scale.ScaleLineDrawable;
import com.yashoid.chartfortelegram.chart.SelectionLineDrawable;
import com.yashoid.chartfortelegram.data.Chart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InfoView extends View {

    private Paint mPaintDate;
    private Paint mPaintValue;
    private Paint mPaintLabel;

    private float mBaseX;
    private float mDateY;
    private float mValueOffsetY;
    private float mValueDy;
    private float mLabelDy;
    private float mValueDx;

    private String mDate = null;
    private List<List<SelectionLineDrawable.SelectionIntersectionInfo>> mInfo = new ArrayList<>();
    private int mMaxValue;
    private int mMaxColumns;

    public InfoView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public InfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public InfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        final float density = getResources().getDisplayMetrics().density;

        mPaintDate = new Paint();
        mPaintDate.setAntiAlias(true);
        mPaintDate.setStyle(Paint.Style.FILL);
        mPaintDate.setTextAlign(Paint.Align.LEFT);
        mPaintDate.setTextSize(14 * density);

        mPaintValue = new Paint(mPaintDate);
        mPaintValue.setTextSize(16 * density);

        mPaintLabel = new Paint(mPaintDate);
        mPaintLabel.setTextSize(13 * density);
    }

    public void setSelectionInfo(SelectionLineDrawable.SelectionInfo info) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd");
        mDate = dateFormat.format(new Date(info.time));

        mMaxValue = 0;
        mMaxColumns = 0;

        HashMap<Chart, List<SelectionLineDrawable.SelectionIntersectionInfo>> map = new HashMap<>();

        for (SelectionLineDrawable.SelectionIntersectionInfo intersection: info.intersections) {
            List<SelectionLineDrawable.SelectionIntersectionInfo> list = map.get(intersection.chart);

            if (list == null) {
                list = new ArrayList<>();

                map.put(intersection.chart, list);
            }

            list.add(intersection);

            mMaxValue = Math.max(mMaxValue, intersection.value);
            mMaxColumns = Math.max(mMaxColumns, list.size());
        }

        mInfo.clear();
        mInfo.addAll(map.values());

        requestLayout();
    }

    public void setTextColor(int textColor) {
        mPaintDate.setColor(textColor);

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDate == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final float density = getResources().getDisplayMetrics().density;

        mBaseX = 0;
        mDateY = getTextHeight(mPaintDate);

        float valueHeight = getTextHeight(mPaintValue);

        mValueOffsetY = valueHeight + density * 4;

        float labelHeight = getTextHeight(mPaintLabel);
        mLabelDy = labelHeight;// + labelHeight / 2;

        mValueDy = valueHeight + mLabelDy + valueHeight / 2;

        mValueDx = Math.max(mPaintValue.measureText(convertValue(mMaxValue)), mPaintValue.measureText("10,000")) + density * 4;

        int width = (int) Math.max(mPaintDate.measureText(mDate), mValueDx * mMaxColumns);
        int height = (int) (mInfo.isEmpty() ? mDateY : (mDateY + mValueOffsetY + (mInfo.size() - 1) * mValueDy + mLabelDy));

        setMeasuredDimension(width, height);
    }

    private float getTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();

        return fm.descent - fm.ascent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDate == null) {
            return;
        }

        float x = mBaseX;
        float y = mDateY;

        canvas.drawText(mDate, x, y, mPaintDate);

        y += mValueOffsetY;

        for (List<SelectionLineDrawable.SelectionIntersectionInfo> row: mInfo) {
            x = mBaseX;

            for (SelectionLineDrawable.SelectionIntersectionInfo info: row) {
                mPaintValue.setColor(info.chartLine.getColor());
                canvas.drawText(convertValue(info.value), x, y, mPaintValue);

                mPaintLabel.setColor(mPaintValue.getColor());
                canvas.drawText(info.chartLine.getName(), x, y + mLabelDy, mPaintLabel);

                x += mValueDx;
            }

            y += mValueDy;
        }
    }

    private String convertValue(int value) {
        return ScaleLineDrawable.commaSeparated(value);
    }

}
