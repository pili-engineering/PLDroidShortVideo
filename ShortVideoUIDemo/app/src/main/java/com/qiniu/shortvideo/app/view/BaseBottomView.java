package com.qiniu.shortvideo.app.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * 编辑模块底部操作视图的基类
 */
public abstract class BaseBottomView extends FrameLayout {
    protected Context mContext;

    public BaseBottomView(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public BaseBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public BaseBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public int getCalculateHeight(){

        int measuredHeight = getMeasuredHeight();
        if (measuredHeight == 0){
            measure(0,0);
            measuredHeight = getMeasuredHeight();
        }
        return measuredHeight;
    }

    /**
     * 是否将 title bar 可见
     *
     * @return true or false
     */
    public boolean isTitleBarVisible() {
        return false;
    }

    protected abstract void init();

    /**
     * 是否需要缩放播放器
     *
     * @return true or false
     */
    public abstract boolean isPlayerNeedZoom();

    public void removeSelf(){
        ViewParent parent = this.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }
}
