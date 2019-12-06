package com.qiniu.shortvideo.app.tusdk.playview.rangeselect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.Collections;
import java.util.LinkedList;

/**
 * 颜色矩形父控件
 * @author MirsFang
 */
public class TuSdkMovieColorGroupView extends FrameLayout {
    private static final String TAG = "ColorGroupView";
    /**
     * 颜色集合
     **/
    private LinkedList<TuSdkMovieColorRectView> mColorRectList = new LinkedList<>();
    private OnSelectColorRectListener onSelectColorRectListener;

    //添加模式  0是长按添加特效模式
    private int mAddMode = 0;

    /** 选择一个ColorRect **/
    public interface OnSelectColorRectListener{
        void onSelectColorRect(TuSdkMovieColorRectView rectView);
    }

    public TuSdkMovieColorGroupView(@NonNull Context context) {
        super(context);
    }

    public TuSdkMovieColorGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            TuSdkMovieColorRectView rectView = (TuSdkMovieColorRectView) getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) rectView.getLayoutParams();
            layoutParams.height = height;
            rectView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            TuSdkMovieColorRectView rectView = (TuSdkMovieColorRectView) getChildAt(i);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            int startPosition = (int) (getMeasuredWidth() * rectView.getStartPercent());

            if (rectView.getDrawDirection() == 0) {
                int diff = 0;
                if(i>0 && getChildAt(i-1) != null && getChildAt(i-1).getVisibility() == VISIBLE){
                    TuSdkMovieColorRectView preRectView = (TuSdkMovieColorRectView) getChildAt(i - 1);
                    if(startPosition - preRectView.getRight() < 5 && startPosition - preRectView.getRight() >=0){
                        diff = startPosition - preRectView.getRight();
                        startPosition = preRectView.getRight();
                    }
                }
                rectView.layout(startPosition, top, startPosition + rectView.getMeasuredWidth() + diff, bottom);
            } else {
                int diff = 0;
                if(i>0 && getChildAt(i-1) != null && getChildAt(i-1).getVisibility() == VISIBLE){
                    TuSdkMovieColorRectView preRectView = (TuSdkMovieColorRectView) getChildAt(i - 1);
                    if(preRectView.getLeft() - startPosition  < 5 && preRectView.getLeft() - startPosition >= 0){
                        diff = preRectView.getLeft() - startPosition;
                        startPosition = preRectView.getLeft();
                    }
                }
                rectView.layout(startPosition - rectView.getMeasuredWidth() - diff, top, startPosition, bottom);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float touchX = event.getRawX();
                float touchY = event.getRawY();
                TuSdkMovieColorRectView rectView = getPointColorRect(touchX,touchY);
                if(onSelectColorRectListener != null)onSelectColorRectListener.onSelectColorRect(rectView);
                break;
        }
        return super.onTouchEvent(event);
    }

    public TuSdkMovieColorRectView getPointColorRect(float pointX,float pointY){
        LinkedList<TuSdkMovieColorRectView> reverList = (LinkedList<TuSdkMovieColorRectView>) mColorRectList.clone();
        Collections.reverse(reverList);
        for (TuSdkMovieColorRectView rectView : reverList) {
            if(isTouchPointInView(rectView,pointX,pointY)){
                return rectView;
            }
        }
        return null;
    }

    private boolean isTouchPointInView(View view, float x, float y) {
        if (view == null || view.getVisibility() == GONE) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (x >= left && x <= right) {
            return true;
        }
        return false;
    }

    /** 设置颜色选择监听 **/
    public void setOnSelectColorRectListener(OnSelectColorRectListener onSelectColorRectListener){
        this.onSelectColorRectListener = onSelectColorRectListener;
    }

    /**
     * 添加颜色区块View
     **/
    public void addColorRect(TuSdkMovieColorRectView rectView) {

        LayoutParams layoutParams = (LayoutParams) rectView.getLayoutParams();
        if(layoutParams != null) {
            this.addView(rectView, layoutParams.width, rectView.getHeight());
        }
        else {
            this.addView(rectView, rectView.getWidth(), rectView.getHeight());
        }

        if(mColorRectList.size() > 0){
            TuSdkMovieColorRectView last = mColorRectList.getLast();
            if(last.getDrawDirection() == rectView.getDrawDirection() && Math.abs(rectView.getStartPercent() - last.getEndPercent()) <= 0.004){
                rectView.setStartPercent(last.getEndPercent());
            }
        }

        mColorRectList.add(rectView);

    }

    /**
     * 移除一个颜色区块
     **/
    public TuSdkMovieColorRectView removeColorRect(int index) {
        if (index >= mColorRectList.size() || mColorRectList.size() == 0) {
            Log.e(TAG, "Invalid remove index");
            return null;
        }
        this.removeViewAt(index);
        return mColorRectList.remove(index);
    }


    public void removeColorRect(TuSdkMovieColorRectView rectView) {
        if (rectView == null|| mColorRectList.size() == 0) {
            Log.e(TAG, "Invalid remove index");
            return;
        }
        mColorRectList.remove(rectView);
        removeView(rectView);
    }


    /**
     * 移除最后一个颜色区块
     **/
    public TuSdkMovieColorRectView removeLastColorRect() {
        return this.removeColorRect(mColorRectList.size() - 1);
    }

    /**
     * 获取最后一个颜色画块
     * @return
     */
    public TuSdkMovieColorRectView getLastColorRect(){
        if(mColorRectList == null || mColorRectList.size() == 0)return null;
        return mColorRectList.get(mColorRectList.size() -1 );
    }

    /**
     * 更新最后一个色块的颜色
     **/
    public void updateLastWidth(int distance) {
        if (mColorRectList.size() == 0) return;
        mColorRectList.get(mColorRectList.size() - 1).setWidth(Math.abs(distance));
    }

    /** 是否存在色块 **/
    public boolean isContain(TuSdkMovieColorRectView rectView){
        if(rectView == null || !mColorRectList.contains(rectView)) return false;
        return mColorRectList.contains(rectView);
    }

    public void clearAllColorRect() {
        removeAllViews();
        mColorRectList.clear();
    }
}
