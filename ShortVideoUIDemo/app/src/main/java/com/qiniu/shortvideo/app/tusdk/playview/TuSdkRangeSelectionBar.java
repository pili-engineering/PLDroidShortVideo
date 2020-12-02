package com.qiniu.shortvideo.app.tusdk.playview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.tusdk.playview.rangeselect.TuSdkMovieGrayView;

import org.lasque.tusdk.core.utils.ThreadHelper;

/**
 * 视频区间选择控件
 * @author MirsFang
 */
public class TuSdkRangeSelectionBar extends RelativeLayout {
    private static final String TAG = "RangeSelectionBar";
    private TuSdkMovieGrayView mLeftGrayView;
    private TuSdkMovieGrayView mRightGrayView;
    private FrameLayout mCenterFrame;
    private OnSelectRangeChangedListener mSelectRangeChangedListener;
    private OnExceedCriticalValueListener mExceedValueListener;
    private OnTouchSelectBarListener onTouchSelectBarListener;
    private int oldX = 0;
    private int touchType = 0;
    private int mTotalWidth = 0;
    private boolean enableTouchCenter;
    /**
     * 最后一次开始的进度
     **/
    private float mLastStarPercent;
    /**
     * 最后一次结束的进度
     **/
    private float mLastEndPercent;
    /**
     * 最小选择区间
     **/
    private float mMinPercent = .10f;
    /**
     * 最大选择区间
     **/
    private float mMaxPercent = 1f;

    /** 总时长 **/
    public static long mTotalTimeUs;
    /** 最小时长 **/
    public static long mMinTimeUs;
    private boolean isEnable = true;

    public static interface OnSelectRangeChangedListener {
        /**
         * 选择区间进度改变回调
         *
         * @param leftPercent   左边进度
         * @param rightPerchent 右边进度
         * @param type          当前进度改变类型  0左   1右   2 全部
         */
        void onSelectRangeChanged(float leftPercent, float rightPerchent, int type);
    }

    /** 触摸选择控件 **/
    public static interface OnTouchSelectBarListener {
        void onTouchBar(float leftPercent, float rightPerchent, int type);
    }

    /**
     * 超过临界值回调
     **/
    public static interface OnExceedCriticalValueListener {
        /**
         * 超过最大值
         **/
        void onMaxValueExceed();

        /**
         * 超过最小值
         **/
        void onMinValueExceed();
    }


    public TuSdkRangeSelectionBar(Context context) {
        super(context);
    }

    public TuSdkRangeSelectionBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftGrayView  =  findViewById(R.id.lsq_left_bar);
        mRightGrayView =  findViewById(R.id.lsq_right_bar);
        mCenterFrame   =  findViewById(R.id.lsq_center);
    }
    private Object mLock = new Object();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //0 左边 1 右边 2中间
        if(!isEnable) return false;
        synchronized (mLock) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return handleActionDownEvent(event);
                case MotionEvent.ACTION_MOVE:
                    return handleActionMoveEvent(event);
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (touchType == 0) {
                        ThreadHelper.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onTouchSelectBarListener != null) {
                                    onTouchSelectBarListener.onTouchBar(mLastStarPercent, mLastEndPercent, touchType);
                                }
                            }
                        });
                    } else if (touchType == 1) {
                        ThreadHelper.post(new Runnable() {
                            @Override
                            public void run() {
                                float stopX = mCenterFrame.getX() + mCenterFrame.getWidth() - mRightGrayView.getBarWidth();
                                if (onTouchSelectBarListener != null) {
                                    onTouchSelectBarListener.onTouchBar(mLastStarPercent, mLastEndPercent, touchType);
                                }
                            }
                        });

                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTotalWidth = MeasureSpec.getSize(widthMeasureSpec) - mLeftGrayView.getBarWidth() * 2;
    }

    /** 设置进度改变回调 */
    public void setSelectRangeChangedListener(OnSelectRangeChangedListener changedListener) {
        this.mSelectRangeChangedListener = changedListener;
    }

    /**  设置临界值回调  */
    public void setExceedCriticalValueListener(OnExceedCriticalValueListener mExceedValueListener) {
        this.mExceedValueListener = mExceedValueListener;
    }

    public void setOnTouchSelectBarListener(OnTouchSelectBarListener onTouchSelectBarListener){
        this.onTouchSelectBarListener = onTouchSelectBarListener;
    }

    /**
     * 处理移动的事件
     **/
    private boolean handleActionMoveEvent(MotionEvent event) {
        int newX = (int) event.getX();
        int diffX = (newX - oldX);
        if(Math.abs(diffX) < 5)return false;
        oldX = newX;
        final ViewParent parent = getParent();
        switch (touchType) {
            case 0:
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                if (calcPercentRange(touchType,true,diffX)) {
                    moveLeftBar(diffX);
                    calcPercentRange(touchType,true);
                }
                return true;
            case 1:
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                if (calcPercentRange(touchType,true,diffX)){
                    moveRightBar(diffX);
                    calcPercentRange(touchType,true);
                }
                return true;
            case 2:
//                if (!enableTouchCenter) return false;
//                if (calcPercentRange(touchType,true,diffX)) {
//                    moveLeftBar(diffX);
//                    moveRightBar(diffX);
//                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 计算当前进度区间
     *
     * @param touchType
     * @return 是否能继续执行
     */
    private boolean calcPercentRange(int touchType,boolean notifiy) {
        float starX = mCenterFrame.getX() - mLeftGrayView.getBarWidth();
        float stopX = mCenterFrame.getX() + mCenterFrame.getWidth() - mRightGrayView.getBarWidth();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;
        if(endPercent > 1) endPercent = 1f;

        mLastStarPercent = startPercent;
        mLastEndPercent = endPercent;

        return true;
    }

    private boolean calcPercentRange(int touchType,boolean notifiy,float diffX) {
        //- mLeftGrayView.getBarWidth();  - mRightGrayView.getBarWidth();;\
        float starX = mCenterFrame.getLeft();
        float stopX = mCenterFrame.getRight();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;
        float maxLength = mTotalWidth * mMaxPercent;
        float minLength = mTotalWidth * mMinPercent;

        if(endPercent > 1) endPercent = 1f;

        //超过最大值
        if (mCenterFrame.getWidth() >= maxLength) {
            return handleMax(touchType, diffX);
        }
        //超过最小值
        if (mCenterFrame.getWidth() <= minLength) {
            return handleMin(touchType, diffX);
        }

        mLastStarPercent = startPercent;
        mLastEndPercent = endPercent;



        return true;
    }

    /** 是否超过最大值判断 **/
    private boolean handleMin(int touchType, float diffX) {
        if(touchType == 0){
            if(diffX < 0)return true;
        }else if(touchType == 1)
        {
            if(diffX > 0)return true;
        }
        if (mExceedValueListener != null) mExceedValueListener.onMinValueExceed();
        return false;
    }

    /** 是否超过最小值判断 **/
    private boolean handleMax(int touchType, float diffX) {

        if(touchType == 0){
            if(diffX > 0)return true;
        }else if(touchType == 1)
        {
            if(diffX < 0)return true;
        }
        if (mExceedValueListener != null) mExceedValueListener.onMaxValueExceed();
        return false;
    }


    /**
     * 处理按下的事件
     *
     * @param event
     */
    private boolean handleActionDownEvent(MotionEvent event) {
        oldX = (int) event.getX();
        //触摸左边视图
        boolean isTouchLeftBarRight = 0 <= Math.abs(oldX - mLeftGrayView.getRight()) && 40 >= Math.abs(oldX - mLeftGrayView.getRight());
        boolean isTouchLeftBarLeft = 0 <= Math.abs(oldX - (mLeftGrayView.getRight() - mLeftGrayView.getBarWidth())) && 40 >= Math.abs(oldX - (mLeftGrayView.getRight() - mLeftGrayView.getBarWidth()));
        if (isTouchLeftBarRight && isTouchLeftBarLeft) {
            touchType = 0;
            return true;
        }
        //触摸的中间视图

        boolean isTouchRightBarRight = 0 <= Math.abs(oldX - mRightGrayView.getLeft()) && 40 >= Math.abs(oldX - mRightGrayView.getLeft());
        boolean isTouchRightBarLeft = 0 <= Math.abs(oldX -  (mRightGrayView.getLeft() + mRightGrayView.getBarWidth())) && 40 >= Math.abs(oldX -  (mRightGrayView.getLeft() + mRightGrayView.getBarWidth()));
        //触摸的右边视图
        if (isTouchRightBarLeft&& isTouchRightBarRight) {
            touchType = 1;
            return true;
        }

        return false;
    }

    /**
     * 移动右边的Bar
     *
     * @param distance 距离
     */
    private int mLastCenterFrameWidth = -1;
    private void moveRightBar(int distance) {
        LayoutParams rightLayoutParams = (LayoutParams) mRightGrayView.getLayoutParams();
        if(mLastCenterFrameWidth < 0)mLastCenterFrameWidth = mCenterFrame.getWidth();
        if (rightLayoutParams.width - distance >= mRightGrayView.getBarWidth()) {
            int minLength = (int) Math.ceil(mTotalWidth * mMinPercent);

            if(mLastCenterFrameWidth + distance < minLength){
                distance = -(mLastCenterFrameWidth - minLength);
            }

            int maxLength = (int) Math.ceil(mTotalWidth * mMaxPercent);
            if(mLastCenterFrameWidth + distance > maxLength){
                distance = maxLength - mLastCenterFrameWidth;
            }

            mLastCenterFrameWidth = mLastCenterFrameWidth + distance;

            rightLayoutParams.width = (int) (rightLayoutParams.width - distance);
        }
        mRightGrayView.setLayoutParams(rightLayoutParams);
        invalidate();

        float starX = mCenterFrame.getX() - mLeftGrayView.getBarWidth();
        float stopX = mCenterFrame.getX() + mLastCenterFrameWidth - mRightGrayView.getBarWidth();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;

        if (mSelectRangeChangedListener != null)
            mSelectRangeChangedListener.onSelectRangeChanged(getFormatPerchent(startPercent), getFormatPerchent(endPercent), 1);

    }

    /**
     *
     * @param distance
     * @param isNotifyChanged 是否需要通知
     */
    private void moveRightBarTo(float distance,final boolean isNotifyChanged) {
        LayoutParams rightLayoutParams = (LayoutParams) mRightGrayView.getLayoutParams();
        rightLayoutParams.width = (int) (distance);
        mRightGrayView.setLayoutParams(rightLayoutParams);
        invalidate();
        mLastCenterFrameWidth = -1;
        float starX = mCenterFrame.getX() - mLeftGrayView.getBarWidth();
        float stopX = mCenterFrame.getX() + mCenterFrame.getWidth() - mRightGrayView.getBarWidth();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;
        if (mSelectRangeChangedListener != null && isNotifyChanged)
            mSelectRangeChangedListener.onSelectRangeChanged(getFormatPerchent(startPercent), getFormatPerchent(endPercent), 1);

    }

    /**
     * 移动左边的Bar
     *
     * @param distance
     */
    private void moveLeftBar(int distance) {
        LayoutParams leftLayoutParams = (LayoutParams) mLeftGrayView.getLayoutParams();
        if(mLastCenterFrameWidth < 0)mLastCenterFrameWidth = mCenterFrame.getWidth();
        if (leftLayoutParams.width + distance >= mLeftGrayView.getBarWidth()) {
            int minLength = (int) Math.ceil(mTotalWidth * mMinPercent);
            if(mLastCenterFrameWidth - distance < minLength){
                distance = mLastCenterFrameWidth - minLength;
            }

            int maxLength = (int) Math.ceil(mTotalWidth * mMaxPercent);
            if(mLastCenterFrameWidth - distance > maxLength){
                distance = -(maxLength - mLastCenterFrameWidth);
            }

            mLastCenterFrameWidth = mLastCenterFrameWidth - distance;
            leftLayoutParams.width = (int) (leftLayoutParams.width + distance);
        }
        mLeftGrayView.setLayoutParams(leftLayoutParams);
        invalidate();

        float starX = mCenterFrame.getX() - mLeftGrayView.getBarWidth();
        float stopX = mCenterFrame.getX() + mLastCenterFrameWidth - mRightGrayView.getBarWidth();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;
        if (mSelectRangeChangedListener != null)
            mSelectRangeChangedListener.onSelectRangeChanged(getFormatPerchent(startPercent), getFormatPerchent(endPercent), 0);

    }

    /**
     *
     * @param distance
     * @param isNotifyChanged 是否需要通知
     */
    private void moveLeftBarTo(float distance,final boolean isNotifyChanged){
        LayoutParams leftLayoutParams = (LayoutParams) mLeftGrayView.getLayoutParams();
//        if (leftLayoutParams.width + distance >= mLeftGrayView.getBarWidth())
            leftLayoutParams.width = (int) distance;
        mLeftGrayView.setLayoutParams(leftLayoutParams);
        invalidate();
        mLastCenterFrameWidth = -1;
        float starX = mCenterFrame.getX() - mLeftGrayView.getBarWidth();
        float stopX = mCenterFrame.getX() + mCenterFrame.getWidth() - mRightGrayView.getBarWidth();
        float startPercent = starX / mTotalWidth;
        float endPercent = stopX / mTotalWidth;
        if (mSelectRangeChangedListener != null && isNotifyChanged)
            mSelectRangeChangedListener.onSelectRangeChanged(getFormatPerchent(startPercent), getFormatPerchent(endPercent), 0);

    }

    /**
     * 获取进度
     **/
    private float getFormatPerchent(float percent) {
//        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//        float p = Float.valueOf(decimalFormat.format(percent));
        return percent;
    }

    /**
     * 获取bar的宽度
     **/
    public int getBarWidth() {
        return mLeftGrayView.getBarWidth();
    }

    /**
     * 最小区间占比
     **/
    public void setMinWidth(float minPercent) {
        this.mMinPercent = minPercent;
    }

    /**
     * 最大区间占比
     **/
    public void setMaxWidth(float maxPercent) {
        this.mMaxPercent = maxPercent;
    }

    /**
     * 移动左边Bar的位置
     **/
    public void setLeftBarPosition(float percent) {
//        if (calcPercentRange(0,false)) {
            int totalWidth = getMeasuredWidth() - (getBarWidth() * 2);
            moveLeftBarTo((totalWidth * percent) + getBarWidth(),false);
//        }
    }

    /**
     * 移动右边Bar的位置
     **/
    public void setRightBarPosition(float percent){
//        if(calcPercentRange(1,false)){
            int totalWidth = getMeasuredWidth() - (getBarWidth() * 2);
            moveRightBarTo(totalWidth * (1f - percent) + getBarWidth(),false);
//        }
    }

    /** 获取左边Bar的进度 **/
    public float getLeftBarPercent(){
        return mLastStarPercent;
    }

    /** 获取右边Bar的进度 **/
    public float getRightBarPercent(){
        return mLastEndPercent;
    }

    /** 是否启用 **/
    public void setEnable(boolean isEnable){
        this.isEnable = isEnable;
    }
}
