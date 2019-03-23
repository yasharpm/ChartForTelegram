package com.yashoid.chartfortelegram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class AreaSelectorView extends View {

    private static final int TARGET_LEFT = 0;
    private static final int TARGET_RIGHT = 1;
    private static final int TARGET_AREA = 2;

    public interface OnSelectedAreaChangedListener {

        void onSelectedAreaChanged(long start, long end);

    }

    private OnSelectedAreaChangedListener mOnSelectedAreaChangedListener = null;

    private float mTouchArea;
    private int mTouchTarget;

    private Paint mCoverPaint;
    private Paint mEdgePaint;
    private float mHorizontalEdgeWidth;
    private float mVerticalEdgeWidth;

    private long mStart;
    private long mEnd;

    private long mMinimumSelectedArea;

    private long mSelectionStart = -1;
    private long mSelectionEnd = -1;

    private boolean mMeasurementsInvalidated = false;

    private int mMinimumSelectedAreaLength;
    private float mSelectionStartPosition = -1;
    private float mSelectionEndPosition = -1;

    private GestureDetector mGestureDetector;

    public AreaSelectorView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public AreaSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public AreaSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        float density = getResources().getDisplayMetrics().density;

        mTouchArea = density * Config.AREA_SELECTOR_TOUCH_SIZE;

        mCoverPaint = new Paint();
        mCoverPaint.setStyle(Paint.Style.FILL);

        mEdgePaint = new Paint(mCoverPaint);

        mHorizontalEdgeWidth = density * 1f;
        mVerticalEdgeWidth = density * 4f;

        setMinimumSelectedArea(Config.MINIMUM_SELECTABLE_AREA_LENGTH);

        mGestureDetector = new GestureDetector(context, mOnGestureListener);
    }

    public void setCoverColor(int color) {
        mCoverPaint.setColor(color);

        invalidate();
    }

    public void setEdgeColor(int color) {
        mEdgePaint.setColor(color);

        invalidate();
    }

    public void setOnSelectedAreaChangedListener(OnSelectedAreaChangedListener listener) {
        mOnSelectedAreaChangedListener = listener;
    }

    public void setRange(long start, long end) {
        mStart = start;
        mEnd = end;

        boolean selectionChanged = false;

        if (mSelectionStart == -1 || mSelectionStart < mStart) {
            mSelectionStart = mStart;

            selectionChanged = true;
        }

        if (mSelectionEnd == -1 || mSelectionEnd > mEnd) {
            mSelectionEnd = mEnd;

            selectionChanged = true;
        }

        if (mSelectionStart == mSelectionEnd) {
            mSelectionStart = start;
            mSelectionEnd = end;

            selectionChanged = true;
        }

        mMeasurementsInvalidated = true;

        invalidate();

        if (selectionChanged && mOnSelectedAreaChangedListener != null) {
            mOnSelectedAreaChangedListener.onSelectedAreaChanged(mSelectionStart, mSelectionEnd);
        }
    }

    public void setMinimumSelectedArea(long area) {
        mMinimumSelectedArea = area;
    }

    public void setSelection(long start, long end) {
        mSelectionStart = start;
        mSelectionEnd = end;

        mMeasurementsInvalidated = true;

        invalidate();
    }

    public long getSelectionStart() {
        return mSelectionStart;
    }

    public long getSelectionEnd() {
        return mSelectionEnd;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mMeasurementsInvalidated = true;
    }

    private void updateMeasurements() {
        if (mEnd == mStart) {
            return;
        }

        int width = getWidth();

        mSelectionStartPosition = (int) ((mSelectionStart - mStart) * width / (mEnd - mStart));
        mSelectionEndPosition = (int) ((mSelectionEnd - mStart) * width / (mEnd - mStart));

        mMinimumSelectedAreaLength = Math.max((int) (2 * mTouchArea), (int) (mMinimumSelectedArea * width / (mEnd - mStart)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mMeasurementsInvalidated) {
            updateMeasurements();

            mMeasurementsInvalidated = false;
        }

        final int width = getWidth();
        final int height = getHeight();

        canvas.drawRect(0, 0, mSelectionStartPosition, height, mCoverPaint);
        canvas.drawRect(mSelectionStartPosition, 0, mSelectionStartPosition + mVerticalEdgeWidth, height, mEdgePaint);
        canvas.drawRect(mSelectionEndPosition - mVerticalEdgeWidth, 0, mSelectionEndPosition, height, mEdgePaint);
        canvas.drawRect(mSelectionEndPosition, 0, width, height, mCoverPaint);
        canvas.drawRect(mSelectionStartPosition + mVerticalEdgeWidth, 0, mSelectionEndPosition - mVerticalEdgeWidth, mHorizontalEdgeWidth, mEdgePaint);
        canvas.drawRect(mSelectionStartPosition + mVerticalEdgeWidth, height - mHorizontalEdgeWidth, mSelectionEndPosition - mVerticalEdgeWidth, height, mEdgePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            if (Math.abs(e.getX() - mSelectionStartPosition) < mTouchArea / 2) {
                mTouchTarget = TARGET_LEFT;
                return true;
            }

            if (Math.abs(e.getX() - mSelectionEndPosition) < mTouchArea / 2) {
                mTouchTarget = TARGET_RIGHT;
                return true;
            }

            if (e.getX() > mSelectionStartPosition && e.getX() < mSelectionEndPosition) {
                mTouchTarget = TARGET_AREA;
                return true;
            }

            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) { }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            distanceX = -distanceX;

            switch (mTouchTarget) {
                case TARGET_LEFT:
                    mSelectionStartPosition += distanceX;

                    if (mSelectionStartPosition < 0) {
                        mSelectionStartPosition = 0;
                    }

                    if (mSelectionEndPosition - mSelectionStartPosition < mMinimumSelectedAreaLength) {
                        mSelectionStartPosition = mSelectionEndPosition - mMinimumSelectedAreaLength;
                    }

                    updateSelectionStartFromPosition();
                    break;
                case TARGET_RIGHT:
                    mSelectionEndPosition += distanceX;

                    if (mSelectionEndPosition > getWidth()) {
                        mSelectionEndPosition = getWidth();
                    }

                    if (mSelectionEndPosition - mSelectionStartPosition < mMinimumSelectedAreaLength) {
                        mSelectionEndPosition = mSelectionStartPosition + mMinimumSelectedAreaLength;
                    }

                    updateSelectionEndFromPosition();
                    break;
                case TARGET_AREA:
                    float areaLength = mSelectionEndPosition - mSelectionStartPosition;

                    mSelectionStartPosition += distanceX;
                    mSelectionEndPosition += distanceX;

                    if (mSelectionStartPosition < 0) {
                        mSelectionStartPosition = 0;
                        mSelectionEndPosition = mSelectionStartPosition + areaLength;
                    }
                    else if (mSelectionEndPosition > getWidth()) {
                        mSelectionEndPosition = getWidth();
                        mSelectionStartPosition = mSelectionEndPosition - areaLength;
                    }

                    updateSelectionStartFromPosition();
                    updateSelectionEndFromPosition();
                    break;
            }

            if (mOnSelectedAreaChangedListener != null) {
                mOnSelectedAreaChangedListener.onSelectedAreaChanged(mSelectionStart, mSelectionEnd);
            }

            invalidate();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) { }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    };

    private void updateSelectionStartFromPosition() {
        mSelectionStart = (long) ((mEnd - mStart) / 10000 / (float) getWidth() * mSelectionStartPosition) * 10000 + mStart;
    }

    private void updateSelectionEndFromPosition() {
        mSelectionEnd = (long) ((mEnd - mStart) / 10000 / (float) getWidth() * mSelectionEndPosition) * 10000 + mStart;
    }

}
