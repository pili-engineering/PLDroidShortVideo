package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class ObservableHorizontalScrollView extends HorizontalScrollView {
    public interface OnScrollListener {
        void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y, int oldX, int oldY, boolean dragScroll);
    }

    private boolean mIsScrolling;
    private boolean mIsTouching;
    private boolean mIsDragScroll;
    private Runnable mScrollingRunnable;
    private OnScrollListener mOnScrollListener;

    public ObservableHorizontalScrollView(Context context) {
        this(context, null, 0);
    }

    public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_MOVE) {
            mIsTouching = true;
            mIsScrolling = true;
            mIsDragScroll = true;
        } else if (action == MotionEvent.ACTION_UP) {
            mIsTouching = false;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        if (Math.abs(oldX - x) > 0) {
            if (mScrollingRunnable != null) {
                removeCallbacks(mScrollingRunnable);
            }

            mScrollingRunnable = new Runnable() {
                public void run() {
                    if (mIsScrolling && !mIsTouching) {
                        if (mOnScrollListener != null) {
                            mIsDragScroll = false;
                        }
                    }
                    mIsScrolling = false;
                    mScrollingRunnable = null;
                }
            };
            postDelayed(mScrollingRunnable, 200);
        } else {
            mIsDragScroll = false;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollChanged(this, x, y, oldX, oldY, mIsDragScroll);
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }
}