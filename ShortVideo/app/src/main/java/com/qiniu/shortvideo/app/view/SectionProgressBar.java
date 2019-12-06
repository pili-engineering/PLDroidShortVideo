package com.qiniu.shortvideo.app.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.LinkedList;

/**
 * 视频录制的进度条控件
 */
public class SectionProgressBar extends View {

    private static final long DEFAULT_DRAW_CUSOR_INTERNAL = 500;
    private static final float DEFAULT_CURSOR_WIDTH = 4f;
    private static final float DEFAULT_BREAK_POINT_WIDTH = 2f;
    private static final long DEFAULT_FIRST_POINT_TIME = 2 * 1000;
    private static final long DEFAULT_TOTAL_TIME = 10 * 1000;

    private final LinkedList<BreakPointInfo> mBreakPointInfoList = new LinkedList<>();

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

    private double mProceedingSpeed = 1;

    private float mProgressWidth;

    private long mLastUpdateTime;
    private long mLastCursorUpdateTime;

    /**
     * 状态枚举
     */
    public enum State {
        /**
         * 开始状态
         */
        START,
        /**
         * 暂停状态
         */
        PAUSE
    }

    /**
     * 设置 ProgressBar 的颜色
     */
    public void setBarColor(int color) {
        mProgressBarPaint.setColor(color);
    }

    /**
     * 设置处理速度
     */
    public void setProceedingSpeed(double speed) {
        mProceedingSpeed = speed;
    }

    public SectionProgressBar(Context context) {
        super(context);
        init(context);
    }

    public SectionProgressBar(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);

    }

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
        mProgressBarPaint.setColor(Color.parseColor("#ffd700"));

        mCursorPaint.setStyle(Paint.Style.FILL);
        mCursorPaint.setColor(Color.parseColor("#ffffff"));

        mFirstPointPaint.setStyle(Paint.Style.FILL);
        mFirstPointPaint.setColor(Color.parseColor("#622a1d"));

        mBreakPointPaint.setStyle(Paint.Style.FILL);
        mBreakPointPaint.setColor(Color.parseColor("#000000"));

        setTotalTime(paramContext, DEFAULT_TOTAL_TIME);
    }

    /**
     * 重置
     */
    public synchronized void reset() {
        setCurrentState(State.PAUSE);
        mBreakPointInfoList.clear();
    }

    /**
     * 设置第一个断点
     *
     * @param millisecond 断点对应时间，单位：ms
     */
    public void setFirstPointTime(long millisecond) {
        mFirstPointTime = millisecond;
    }

    /**
     * 设置总时间，单位：ms
     *
     * @param context     上下文
     * @param millisecond 总时间，单位：ms
     */
    public void setTotalTime(Context context, long millisecond) {
        mTotalTime = millisecond;

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mPixelUnit = dm.widthPixels / mTotalTime;

        mPixelsPerMilliSecond = mPixelUnit;
    }

    /**
     * 设置当前状态
     *
     * @param state 状态
     */
    public void setCurrentState(State state) {
        mCurrentState = state;
        if (state == State.PAUSE) {
            mProgressWidth = mPixelsPerMilliSecond;
        }
    }

    /**
     * 添加断点
     *
     * @param millisecond 断点时间
     */
    public synchronized void addBreakPointTime(long millisecond) {
        BreakPointInfo info = new BreakPointInfo(millisecond, mProgressBarPaint.getColor());
        mBreakPointInfoList.add(info);
    }

    /**
     * 移除最后一个断点
     */
    public synchronized void removeLastBreakPoint() {
        if (!mBreakPointInfoList.isEmpty()) {
            mBreakPointInfoList.removeLast();
        }
    }

    public synchronized boolean isRecorded() {
        return !mBreakPointInfoList.isEmpty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long curTime = System.currentTimeMillis();

        // redraw all the break point
        int startPoint = 0;
        synchronized (this) {
            if (!mBreakPointInfoList.isEmpty()) {
                float lastTime = 0;
                int color = mProgressBarPaint.getColor();
                for (BreakPointInfo info : mBreakPointInfoList) {
                    mProgressBarPaint.setColor(info.getColor());
                    float left = startPoint;
                    startPoint += (info.getTime() - lastTime) * mPixelUnit;
                    canvas.drawRect(left, 0, startPoint, getMeasuredHeight(), mProgressBarPaint);
                    canvas.drawRect(startPoint, 0, startPoint + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(), mBreakPointPaint);
                    startPoint += DEFAULT_BREAK_POINT_WIDTH;
                    lastTime = info.getTime();
                }
                mProgressBarPaint.setColor(color);
            }

            // draw the first point
            if (mBreakPointInfoList.isEmpty() || mBreakPointInfoList.getLast().getTime() <= mFirstPointTime) {
                canvas.drawRect(mPixelUnit * mFirstPointTime, 0, mPixelUnit * mFirstPointTime + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(), mFirstPointPaint);
            }
        }

        // increase the progress bar in start state
        if (mCurrentState == State.START) {
            mProgressWidth += mPixelsPerMilliSecond * (curTime - mLastUpdateTime) / mProceedingSpeed;
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

    private class BreakPointInfo {
        private long mTime;
        private int mColor;

        public BreakPointInfo(long time, int color) {
            mTime = time;
            mColor = color;
        }

        public void setTime(long mTime) {
            this.mTime = mTime;
        }

        public void setColor(int mColor) {
            this.mColor = mColor;
        }

        public long getTime() {
            return mTime;
        }

        public int getColor() {
            return mColor;
        }
    }
}