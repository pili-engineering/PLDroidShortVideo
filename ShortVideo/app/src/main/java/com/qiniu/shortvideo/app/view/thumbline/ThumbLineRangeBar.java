package com.qiniu.shortvideo.app.view.thumbline;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.shortvideo.app.R;

/**
 * 编辑模块视频缩略图效果选中时的 RangeBar 控件
 */
public class ThumbLineRangeBar {
    private static final String TAG = ThumbLineRangeBar.class.getName();
    public static final byte STATE_ACTIVE = 1; //激活态（编辑态）
    public static final byte STATE_FIX = 2;    //固定态(非编辑态)
    private byte mState;
    private long mMinDurationMs = 2000;   //最小时长，到达最小时长内再无法缩减, 默认2s
    private long mMaxDurationMs = 0;

    public long mDuration;     //时长
    private int mDistance;      //距离（TailView和HeadView的距离）（与时长对应）
    public long mStartTime;

    private ThumbLineHandleView mHeadView;
    private ThumbLineHandleView mTailView;
    private View mSelectedMiddleView;   //Tail和Head中间的View，编辑态和非编辑态呈现不同颜色

    private ThumbLineView mThumbLineView;
    private ViewGroup mOverlayContainer;
    private ThumbLineRangeBarView mThumbLineRangeBarView;
    private OnSelectedDurationChangeListener mOnSelectedDurationChangeListener;

    private int mMiddleViewColor;

    public interface ThumbLineRangeBarView {
        ViewGroup getContainer();

        View getHeadView();

        View getTailView();

        View getMiddleView();
    }

    public interface OnSelectedDurationChangeListener {
        void onRangeBarClicked(ThumbLineRangeBar rangeBar);
        void onDurationChange(long startTime, long duration);
    }

    public ThumbLineRangeBar(ThumbLineView thumbLineView, long startTime, long duration, long minDurationMs, long maxDurationMs, ThumbLineRangeBarView thumbLineRangeBarView, OnSelectedDurationChangeListener listener) {
        mThumbLineView = thumbLineView;
        mStartTime = startTime;
        mDuration = duration;
        mMinDurationMs = minDurationMs;
        mMaxDurationMs = maxDurationMs;
        mThumbLineRangeBarView = thumbLineRangeBarView;
        mOnSelectedDurationChangeListener = listener;
        initView(mStartTime);
        invalidate();
    }

    public void setVisibility(boolean isVisible) {
        if (isVisible) {
            mTailView.getView().setAlpha(1);
            mHeadView.getView().setAlpha(1);
            mSelectedMiddleView.setAlpha(1);
        } else {
            mTailView.getView().setAlpha(0);
            mHeadView.getView().setAlpha(0);
            mSelectedMiddleView.setAlpha(0);
        }
    }

    public boolean isActive() {
        return mState == STATE_ACTIVE;
    }

    public void requestLayout() {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mHeadView.getView().getLayoutParams();
        int margin;
        margin = layoutParams.leftMargin = mThumbLineView.calculateHeadViewPosition(mHeadView);
        mHeadView.getView().setLayoutParams(layoutParams);

        Log.d(TAG, "TailView Margin = "+ margin + "timeline over" + this);
    }

    public void switchState(byte state) {
        mState = state;
        switch (state) {
            case STATE_ACTIVE://显示HeadView和TailView
                mTailView.active();
                mHeadView.active();
                if (mMiddleViewColor != 0){
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(mMiddleViewColor));
                } else {
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(R.color.timeline_bar_active_overlay));
                }
                break;
            case STATE_FIX:
                mTailView.fix();
                mHeadView.fix();
                if (mMiddleViewColor != 0){
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(mMiddleViewColor));
                } else {
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(R.color.timeline_bar_active_overlay));
                }
                break;
            default:
                break;
        }
    }

    public void invalidate() {
        //首先根据duration 计算middleView 的宽度
        mDistance = mThumbLineView.duration2Distance(mDuration);
        ViewGroup.LayoutParams layoutParams = mSelectedMiddleView.getLayoutParams();
        layoutParams.width = mDistance;
        mSelectedMiddleView.setLayoutParams(layoutParams);
        switch (mState) {
            case STATE_ACTIVE:    //显示HeadView和TailView
                mTailView.active();
                mHeadView.active();
                if (mMiddleViewColor!=0){
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(mMiddleViewColor));
                } else {
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(R.color.timeline_bar_active_overlay));
                }
                break;
            case STATE_FIX:
                mTailView.fix();
                mHeadView.fix();
                if (mMiddleViewColor!=0){
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(mMiddleViewColor));
                } else {
                    mSelectedMiddleView.setBackgroundColor(mSelectedMiddleView.getContext().getResources()
                            .getColor(R.color.timeline_bar_active_overlay));
                }
                break;
            default:
                break;
        }
    }

    public void updateDuration(long duration) {
        mDuration = duration;
        invalidate();
        requestLayout();
    }

    public View getOverlayRangeBar() {
        return mOverlayContainer;
    }

    private void initView(long startTime) {
        mSelectedMiddleView = mThumbLineRangeBarView.getMiddleView();
        if (mDuration < mMinDurationMs) {
            mDuration = mMinDurationMs;
        } else if (startTime == mMaxDurationMs) {
            startTime = mMaxDurationMs - mMinDurationMs;
            mDuration = mMinDurationMs;
        } else if (mDuration + startTime > mMaxDurationMs) {
            mDuration = mMaxDurationMs - startTime;
        }

        if (mOnSelectedDurationChangeListener != null) {
            mOnSelectedDurationChangeListener.onDurationChange(startTime, mDuration);
        }
        mHeadView = new ThumbLineHandleView(mThumbLineRangeBarView.getHeadView(), startTime);
        mTailView = new ThumbLineHandleView(mThumbLineRangeBarView.getTailView(), startTime + mDuration);
        mOverlayContainer = mThumbLineRangeBarView.getContainer();
        mOverlayContainer.setTag(this);
        setVisibility(false);
        mThumbLineView.addOverlayView(mOverlayContainer, mHeadView, this);

        mSelectedMiddleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectedDurationChangeListener != null) {
                    mOnSelectedDurationChangeListener.onRangeBarClicked(ThumbLineRangeBar.this);
                }
            }
        });

        mHeadView.setOnPositionChangedListener(new ThumbLineHandleView.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float offset) {
                if (mState == STATE_FIX) {
                    return;
                }
                long offsetDuration = mThumbLineView.distance2Duration(offset);
                if (offsetDuration < 0 && mHeadView.getPosition() + offsetDuration < 0) {
                    offsetDuration = -mHeadView.getPosition();
                } else if (offsetDuration > 0 && mDuration - offsetDuration < mMinDurationMs) {
                    offsetDuration = mDuration - mMinDurationMs;
                }
                if (offsetDuration == 0) {
                    return;
                }
                mDuration -= offsetDuration;
                mHeadView.changePosition(offsetDuration);

                if (mHeadView.getView() != null) {
                    ViewGroup.LayoutParams lp = mHeadView.getView().getLayoutParams();
                    int dx = ((ViewGroup.MarginLayoutParams) lp).leftMargin;
                    requestLayout();
                    dx = ((ViewGroup.MarginLayoutParams) lp).leftMargin - dx;
                    mHeadView.getView().setLayoutParams(lp);
                    lp = (ViewGroup.MarginLayoutParams) mSelectedMiddleView.getLayoutParams();
                    lp.width -= dx;
                    mSelectedMiddleView.setLayoutParams(lp);
                }
            }

            @Override
            public void onChangeComplete() {
                if (mState == STATE_ACTIVE){
                    //处于激活态的时候定位到滑动处
                    mThumbLineView.seekTo((int) mHeadView.getPosition());
                }
                if (mOnSelectedDurationChangeListener != null) {
                    mOnSelectedDurationChangeListener.onDurationChange(mHeadView.getPosition(), mDuration);
                }
            }
        });

        mTailView.setOnPositionChangedListener(new ThumbLineHandleView.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float offset) {
                if (mState == STATE_FIX) {
                    return;
                }
                long offsetDuration = mThumbLineView.distance2Duration(offset);
                if (offsetDuration > 0 && mTailView.getPosition() + offsetDuration > mMaxDurationMs) {
                    offsetDuration = mMaxDurationMs - mTailView.getPosition();
                } else if (offsetDuration < 0 && mTailView.getPosition() + offsetDuration - mHeadView.getPosition() < mMinDurationMs) {
                    offsetDuration = mHeadView.getPosition() + mMinDurationMs - mTailView.getPosition();
                }
                if (offsetDuration == 0) {
                    return;
                }
                mDuration += offsetDuration;

                ViewGroup.LayoutParams lp = mSelectedMiddleView.getLayoutParams();
                lp.width = mThumbLineView.duration2Distance(mDuration);
                mSelectedMiddleView.setLayoutParams(lp);
                mTailView.changePosition(offsetDuration);
            }

            @Override
            public void onChangeComplete() {
                if (mState == STATE_ACTIVE){
                    //处于激活态的时候定位到滑动处
                    mThumbLineView.seekTo((int) mTailView.getPosition());
                }
                if (mOnSelectedDurationChangeListener != null) {
                    mOnSelectedDurationChangeListener.onDurationChange(mHeadView.getPosition(), mDuration);
                }
            }
        });
    }
}
