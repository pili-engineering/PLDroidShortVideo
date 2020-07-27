package com.qiniu.shortvideo.app.view.thumbline;

import android.view.MotionEvent;
import android.view.View;

/**
 * 编辑模块视频缩略图效果选中时的 RangeBar 左右拖动控件（HeadView、TailView）
 */
public class ThumbLineHandleView implements View.OnTouchListener {

    private View mView;
    // 显示的时间点
    private long mPosition;
    private float mStartX;
    private OnPositionChangedListener mOnPositionChangedListener;

    public interface OnPositionChangedListener {
        void onPositionChanged(float offset);
        void onChangeComplete();
    }

    public ThumbLineHandleView(View view, long position) {
        mPosition = position;
        mView = view;
        if (mView != null) {
            mView.setOnTouchListener(this);
        }
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        mOnPositionChangedListener = listener;
    }

    public void active() {
        if(mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    public void fix() {
        if(mView != null) {
            mView.setVisibility(View.INVISIBLE);
        }
    }

    public void changePosition(long duration) {
        mPosition += duration;
    }

    public long getPosition() {
        return mPosition;
    }

    public View getView() {
        return mView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - mStartX;
                mStartX = event.getRawX();
                if(mOnPositionChangedListener != null) {
                    mOnPositionChangedListener.onPositionChanged(dx);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mOnPositionChangedListener != null) {
                    mOnPositionChangedListener.onChangeComplete();
                }
                mStartX = 0;
                break;
            default:
                mStartX = 0;
        }
        return true;
    }
}
