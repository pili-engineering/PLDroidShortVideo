package com.qiniu.shortvideo.app.tusdk.playview.rangeselect;

import android.content.Context;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qiniu.shortvideo.app.R;

/**
 * 颜色矩形控件
 * @author MirsFang
 */
public class TuSdkMovieColorRectView extends View {
    private static final String TAG = "ColorRectView";

    /** 当前色块 **/
    @ColorRes
    private int mColorId = R.color.lsq_color_api_gray;
    /** 方向 0 从左到右  1 从右到左 **/
    private int mDrawDirection;
    /** 开始的位置 **/
    private float mStartPercent;
    /** 结束的位置 **/
    private float mEndPercent;
    /** 当前添加的下标 **/
    private int index;


    public TuSdkMovieColorRectView(Context context) {
        super(context);
    }

    public TuSdkMovieColorRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /** 设置当前画块的颜色 **/
    public void setColorId(@ColorRes int colorId){
        this.mColorId = colorId;
        setBackgroundColor(colorId);
    }

    /** 设置输出的方向 */
    public void setDrawDirection(int direction){
        this.mDrawDirection = direction;
    }

    /** 获取绘制的方向 **/
    public int getDrawDirection(){
        return mDrawDirection;
    }

    /** 设置开始的进度 **/
    public void setStartPercent(float startPercent){
        this.mStartPercent = startPercent;
    }

    /** 获取开始的进度 **/
    public float getStartPercent(){
        return mStartPercent;
    }

    public float getEndPercent() {
        return mEndPercent;
    }

    public void setEndPercent(float mEndPercent) {
        this.mEndPercent = mEndPercent;
    }

    /** 设置宽度 **/
    public void setWidth(int width){
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
    }

}
