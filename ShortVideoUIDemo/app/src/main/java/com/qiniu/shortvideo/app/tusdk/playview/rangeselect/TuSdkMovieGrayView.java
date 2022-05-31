package com.qiniu.shortvideo.app.tusdk.playview.rangeselect;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 区间选择Bar
 * @author MirsFang
 */
public class TuSdkMovieGrayView extends FrameLayout {
    //当前Gray的类型  0 左边的   1 右边的
    private int mBarWidth = 0;

    public TuSdkMovieGrayView(@NonNull Context context) {
        super(context);
    }

    public TuSdkMovieGrayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mBarWidth = getChildAt(1).getMeasuredWidth();
    }

    /**
     * 获取当期那bar的宽度
     * @return
     */
    public int getBarWidth() {
        return mBarWidth;
    }

    @Override
    public void setBackgroundColor(int color) {
        getChildAt(0).setBackgroundColor(color);
    }
}
