package com.qiniu.shortvideo.app.tusdk.playview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.tusdk.playview.rangeselect.TuSdkMovieColorGroupView;
import com.qiniu.shortvideo.app.tusdk.playview.rangeselect.TuSdkMovieColorRectView;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.utils.ThreadHelper;

/**
 * 滚动根视图
 *
 * @author MirsFang
 */
public class TuSdkMovieScrollContent extends RelativeLayout {
    private static final String TAG = "TuSdkMovieScrollContent";
    private TuSdkMovieCoverListView mCoverListView;
    private TuSdkRangeSelectionBar mSelectRange;
    protected TuSdkMovieColorGroupView mColorGroupView;
    private OnPlayProgressChangeListener progressChangeListener;
    private ImageView mCursorView;
    private boolean isAddedCoverList;
    private boolean isMeasureBarWidth;
    private boolean isNeedShowCursor = false;
    /** 是否正在触摸中 **/
    private boolean isTouching = false;

    private int mType = 0;
    private boolean isEnable = true;

    public interface OnPlayProgressChangeListener{
        void onProgressChange(float percent);
    }

    public TuSdkMovieScrollContent(Context context) {
        super(context);
        init();
    }

    public TuSdkMovieScrollContent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setProgressChangeListener(OnPlayProgressChangeListener progressChangeListener){
        this.progressChangeListener = progressChangeListener;
    }

    private void init() {
        mCoverListView = new TuSdkMovieCoverListView(getContext());
        mSelectRange = (TuSdkRangeSelectionBar) LayoutInflater.from(getContext()).inflate(R.layout.lsq_range_selection, null);
        mColorGroupView = new TuSdkMovieColorGroupView(getContext());
        mCursorView = new ImageView(getContext());
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = getWidth();
                int height = getHeight();
                if( isAddedCoverList &&  isMeasureBarWidth && mType == 0)return;
                if (!isAddedCoverList) {
                    // 添加封面控件
                    LayoutParams layoutParams0 = new LayoutParams(width, height);
                    if (mType == 1) layoutParams0.leftMargin = TuSdkContext.dip2px(15);
                    if (isNeedShowCursor)layoutParams0.rightMargin = TuSdkContext.dip2px(15);
                    addView(mCoverListView, layoutParams0);

                    //添加画色控件
                    LayoutParams layoutParams2 = new LayoutParams(width, height);
                    if (mType == 1) layoutParams2.leftMargin = TuSdkContext.dip2px(15);
                    if (isNeedShowCursor)layoutParams2.rightMargin = TuSdkContext.dip2px(15);
                    addView(mColorGroupView, layoutParams2);
                    isAddedCoverList = true;

                    //添加选区控件
                    if (mType == 1) {
                        LayoutParams layoutParams1 = new LayoutParams(width , height);
                        addView(mSelectRange, layoutParams1);
                    }

                    if(isNeedShowCursor){
                        LayoutParams layoutParams3 = new LayoutParams(10, height);
                        layoutParams3.leftMargin = TuSdkContext.dip2px(15);
                        mCursorView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        addView(mCursorView,layoutParams3);
                    }
                }


                /** 第一测量getBarWidth()为0 **/
                if (mSelectRange.getBarWidth() != 0 && mType == 1 && !isMeasureBarWidth) {

                    LayoutParams mCoverLayoutParams = (LayoutParams) mCoverListView.getLayoutParams();
                    mCoverLayoutParams.leftMargin = mSelectRange.getBarWidth();
                    mCoverListView.setLayoutParams(mCoverLayoutParams);

                    LayoutParams mSelectLayoutParams = (LayoutParams) mSelectRange.getLayoutParams();
                    mSelectLayoutParams.width = mSelectLayoutParams.width + mSelectRange.getBarWidth() * 2;
                    mSelectRange.setLayoutParams(mSelectLayoutParams);

                    LayoutParams mColorLayoutParams = (LayoutParams) mColorGroupView.getLayoutParams();
                    mColorLayoutParams.leftMargin = mSelectRange.getBarWidth();
                    mColorGroupView.setLayoutParams(mColorLayoutParams);

                    isMeasureBarWidth = true;
                }

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /** 是否需要展示游标 **/
    public void setNeedShowCursor(boolean needShowCursor){
        this.isNeedShowCursor = needShowCursor;
    }

    /**
     * 添加图片
     */
    public void addBitmap(Bitmap bitmap) {
        mCoverListView.addBitmap(bitmap);
    }

    /**
     * 获取总的宽度
     */
    public int getTotalWidth() {
        return mCoverListView.getTotalWidth();
    }

    /**
     * 更新宽度
     **/
    public void updateScrollPercent(float percent) {
        TuSdkMovieColorRectView rectView = mColorGroupView.getLastColorRect();
        if (rectView == null) return;
        int distance = (int) (getTotalWidth() * (percent - rectView.getStartPercent()));
        rectView.setEndPercent(percent);
        mColorGroupView.updateLastWidth(distance);
    }

    /**
     * 添加一个颜色区块
     **/
    public void addColorRect(TuSdkMovieColorRectView rectView) {
        mColorGroupView.addColorRect(rectView);
    }

    /**
     * 删除一个颜色区块
     **/
    public TuSdkMovieColorRectView deletedColorRect() {
        return mColorGroupView.removeLastColorRect();
    }

    public void deletedColorRect(TuSdkMovieColorRectView rectView) {
        mColorGroupView.removeColorRect(rectView);
    }

    /**
     * 设置类型
     **/
    public void setType(int type) {
        this.mType = type;
    }

    /** 设置Bar改变的监听 **/
    public void setSelectRangeChangedListener(TuSdkRangeSelectionBar.OnSelectRangeChangedListener changedListener) {
        mSelectRange.setSelectRangeChangedListener(changedListener);
    }

    /** 设置Bar的临界值监听 **/
    public void setExceedCriticalValueListener(TuSdkRangeSelectionBar.OnExceedCriticalValueListener exceedValueListener) {
        mSelectRange.setExceedCriticalValueListener(exceedValueListener);
    }

    public void setOnTouchSelectBarListener(TuSdkRangeSelectionBar.OnTouchSelectBarListener onTouchSelectBarListener){
        mSelectRange.setOnTouchSelectBarListener(onTouchSelectBarListener);
    }

    /** 设置颜色选择监听 **/
    public void setOnSelectColorRectListener(TuSdkMovieColorGroupView.OnSelectColorRectListener onSelectColorRectListener){
        mColorGroupView.setOnSelectColorRectListener(onSelectColorRectListener);
    }

    /**
     * 最小区间占比
     **/
    public void setMinWidth(float minPercent) {
        mSelectRange.setMinWidth(minPercent);
    }

    /**
     * 最大区间占比
     **/
    public void setMaxWidth(float maxPercent) {
        mSelectRange.setMaxWidth(maxPercent);
    }

    /**
     * 移动左边Bar的位置
     **/
    public void setLeftBarPosition(float percent) {
        mSelectRange.setLeftBarPosition(percent);
    }

    /**
     * 移动右边Bar的位置
     **/
    public void setRightBarPosition(float percent) {
        mSelectRange.setRightBarPosition(percent);
    }

    /** 获取左边Bar的进度 **/
    public float getLeftBarPercent() {
        return mSelectRange.getLeftBarPercent();
    }

    /** 获取右边Bar的进度 **/
    public float getRightBarPercent() {
        return mSelectRange.getRightBarPercent();
    }

    /**
     * 是否正在显示选择控件
     *
     * @return
     */
    public boolean isShowSelectBar() {
        return mSelectRange.getVisibility() == VISIBLE;
    }

    /**
     * 设置是否显示选择控件
     *
     * @param showSelectBar
     */
    public void setShowSelectBar(boolean showSelectBar) {
        mSelectRange.setVisibility(showSelectBar ? VISIBLE : INVISIBLE);
    }

    public boolean isContain(TuSdkMovieColorRectView rectView){
        return mColorGroupView.isContain(rectView);
    }

    /** 清楚当前所有颜色区域 **/
    public void clearAllColorRect() {
        mColorGroupView.clearAllColorRect();
    }

    /** 设置游标进度 **/
    public void setPercent(final float percent){
        if(isTouching)return;
        ThreadHelper.post(new Runnable() {
            @Override
            public void run() {
                mCursorView.setX(mCoverListView.getWidth()*percent + TuSdkContext.dip2px(15));
            }
        });
    }

    private float startX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnable)return false;
        if(isTouchPointInView(mCursorView, event.getRawX()) || startX > 0){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isTouching = true;
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mCursorView.getX() < mCoverListView.getLeft() || event.getX() < mCoverListView.getLeft()){
                        mCursorView.setX(mCoverListView.getLeft());
                        return false;
                    }

                    if(mCursorView.getX() >= mCoverListView.getRight() || event.getX() >=  mCoverListView.getRight()){
                        mCursorView.setX(mCoverListView.getRight()- mCursorView.getWidth()/2);
                        return false;
                    }

                    mCursorView.setX(event.getX());
                    float percent = mCursorView.getX()/mCoverListView.getWidth();
                    if(percent > 1) percent = 1f;
                    if(percent < 0.06)percent = 0f;
                    if(progressChangeListener != null) progressChangeListener.onProgressChange(percent);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if(mCursorView.getX() < mCoverListView.getLeft() && event.getX() < mCoverListView.getLeft()){
                        mCursorView.setX(mCoverListView.getLeft());
                        isTouching = false;
                        return false;
                    }

                    if(mCursorView.getX() >= mCoverListView.getRight() && event.getX() >=  mCoverListView.getRight()){
                        mCursorView.setX(mCoverListView.getRight()- mCursorView.getWidth()/2);
                        isTouching = false;
                        return false;
                    }

                    float percent1 = mCursorView.getX()/mCoverListView.getWidth();
                    if(percent1 > 1) percent1 = 1f;
                    isTouching = false;
                    startX = -1;
                    break;
                case MotionEvent.ACTION_UP:
                    isTouching = false;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean isTouchPointInView(View view, float x) {
        int diff = 40;
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int right = left + view.getMeasuredWidth();
        if ((x  >= left && x <= right) ||(x  >= left - diff && x <= right+diff)) {
            return true;
        }
        return false;
    }

    public void release(){
        if(mCoverListView != null) mCoverListView.release();
    }

    public void setEnable(boolean isEnable){
        this.isEnable = isEnable;
        if(mSelectRange != null)mSelectRange.setEnable(isEnable);
    }
}
