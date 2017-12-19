package com.qiniu.pili.droid.shortvideo.demo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.LinkedList;

public class SectionProgressBar extends View {

    private static final long DEFAULT_DRAW_CUSOR_INTERNAL = 500;
    private static final float DEFAULT_CURSOR_WIDTH = 4f;
    private static final float DEFAULT_BREAK_POINT_WIDTH = 2f;
    private static final long DEFAULT_FIRST_POINT_TIME = 2 * 1000;
    private static final long DEFAULT_TOTAL_TIME = 10 * 1000;

    private final LinkedList<Long> mBreakPointList = new LinkedList<>();

    private Paint mCursorPaint;
    private Paint mProgressBarPaint;
    private Paint mFirstPointPaint;
    private Paint mBreakPointPaint;

    private boolean mIsCursorVisible = true;

    private float mPixelUnit;

    private float mFirstPointTime = DEFAULT_FIRST_POINT_TIME;
    private float mTotalTime = DEFAULT_TOTAL_TIME;

    private volatile State mCurrentState = State.PAUSE;

    private float mPixelsPerMilliSecond;

    private float mProgressWidth;

    private long mLastUpdateTime;
    private long mLastCursorUpdateTime;

    /**
     * The enum State.
     */
    public enum State {
        /**
         * Start state.
         */
        START,
        /**
         * Pause state.
         */
        PAUSE
    }

    /**
     * Instantiates a new Progress view.
     *
     * @param context the context
     */
    public SectionProgressBar(Context context) {
        super(context);
        init(context);
    }

    /**
     * Instantiates a new Progress view.
     *
     * @param paramContext      the param context
     * @param paramAttributeSet the param attribute set
     */
    public SectionProgressBar(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);

    }

    /**
     * Instantiates a new Progress view.
     *
     * @param paramContext      the param context
     * @param paramAttributeSet the param attribute set
     * @param paramInt          the param int
     */
    public SectionProgressBar(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
    }

    private void init(Context paramContext) {
        mCursorPaint = new Paint();
        mProgressBarPaint = new Paint();
        mFirstPointPaint = new Paint();
        mBreakPointPaint = new Paint();

        setBackgroundColor(Color.parseColor("#161823"));

        mProgressBarPaint.setStyle(Paint.Style.FILL);
        mProgressBarPaint.setColor(Color.parseColor("#ff0097"));

        mCursorPaint.setStyle(Paint.Style.FILL);
        mCursorPaint.setColor(Color.parseColor("#ffffff"));

        mFirstPointPaint.setStyle(Paint.Style.FILL);
        mFirstPointPaint.setColor(Color.parseColor("#622a1d"));

        mBreakPointPaint.setStyle(Paint.Style.FILL);
        mBreakPointPaint.setColor(Color.parseColor("#000000"));

        setTotalTime(paramContext, DEFAULT_TOTAL_TIME);
    }

    /**
     * Reset.
     */
    public synchronized void reset() {
        setCurrentState(State.PAUSE);
        mBreakPointList.clear();
    }

    /**
     * Sets first point time.
     *
     * @param millisecond the millisecond
     */
    public void setFirstPointTime(long millisecond) {
        mFirstPointTime = millisecond;
    }

    /**
     * Sets total time in millisecond
     *
     * @param context the context
     * @param millisecond the millisecond
     */
    public void setTotalTime(Context context, long millisecond) {
        mTotalTime = millisecond;

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mPixelUnit = dm.widthPixels / mTotalTime;

        mPixelsPerMilliSecond = mPixelUnit;
    }

    /**
     * Sets current state
     *
     * @param state the state
     */
    public void setCurrentState(State state) {
        mCurrentState = state;
        if (state == State.PAUSE) {
            mProgressWidth = mPixelsPerMilliSecond;
        }
    }

    /**
     * Add break point time.
     *
     * @param millisecond the millisecond
     */
    public synchronized void addBreakPointTime(long millisecond) {
        mBreakPointList.add(millisecond);
    }

    /**
     * Remove last break point.
     */
    public synchronized void removeLastBreakPoint() {
        mBreakPointList.removeLast();
    }

    public synchronized boolean isRecorded(){
        return !mBreakPointList.isEmpty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long curTime = System.currentTimeMillis();

        // redraw all the break point
        int startPoint = 0;
        synchronized (this) {
            if (!mBreakPointList.isEmpty()) {
                float lastTime = 0;
                for (Long time : mBreakPointList) {
                    float left = startPoint;
                    startPoint += (time - lastTime) * mPixelUnit;
                    canvas.drawRect(left, 0, startPoint, getMeasuredHeight(), mProgressBarPaint);
                    canvas.drawRect(startPoint, 0, startPoint + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(), mBreakPointPaint);
                    startPoint += DEFAULT_BREAK_POINT_WIDTH;
                    lastTime = time;
                }
            }

            // draw the first point
            if (mBreakPointList.isEmpty() || mBreakPointList.getLast() <= mFirstPointTime) {
                canvas.drawRect(mPixelUnit * mFirstPointTime, 0, mPixelUnit * mFirstPointTime + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(), mFirstPointPaint);
            }
        }

        // increase the progress bar in start state
        if (mCurrentState == State.START) {
            mProgressWidth += mPixelsPerMilliSecond * (curTime - mLastUpdateTime);
            if (startPoint + mProgressWidth <= getMeasuredWidth()) {
                canvas.drawRect(startPoint, 0, startPoint + mProgressWidth, getMeasuredHeight(), mProgressBarPaint);
            } else {
                canvas.drawRect(startPoint, 0, getMeasuredWidth(), getMeasuredHeight(), mProgressBarPaint);
            }
        }

        // Draw cursor every 500ms
        if (mLastCursorUpdateTime == 0 || curTime - mLastCursorUpdateTime >= DEFAULT_DRAW_CUSOR_INTERNAL) {
            mIsCursorVisible = !mIsCursorVisible;
            mLastCursorUpdateTime = System.currentTimeMillis();
        }
        if (mIsCursorVisible) {
            if (mCurrentState == State.START) {
                canvas.drawRect(startPoint + mProgressWidth, 0, startPoint + DEFAULT_CURSOR_WIDTH + mProgressWidth, getMeasuredHeight(), mCursorPaint);
            } else {
                canvas.drawRect(startPoint, 0, startPoint + DEFAULT_CURSOR_WIDTH, getMeasuredHeight(), mCursorPaint);
            }
        }

        mLastUpdateTime = System.currentTimeMillis();

        invalidate();
    }
}