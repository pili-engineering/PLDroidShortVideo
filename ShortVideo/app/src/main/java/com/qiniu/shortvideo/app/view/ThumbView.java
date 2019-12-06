package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

/**
 * 剪辑模块时间段选择游标控件
 */
public class ThumbView extends View {

    private static final int EXTEND_TOUCH_SLOP = 15;

    private final int mExtendTouchSlop;

    private Drawable mThumbDrawable;

    private boolean mPressed;

    private int mThumbWidth;
    private int mTickIndex;

    public ThumbView(Context context, int thumbWidth, Drawable drawable) {
        super(context);
        mThumbWidth = thumbWidth;
        mThumbDrawable = drawable;
        mExtendTouchSlop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                EXTEND_TOUCH_SLOP, context.getResources().getDisplayMetrics());
        setBackground(mThumbDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(mThumbWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));

        mThumbDrawable.setBounds(0, 0, mThumbWidth, getMeasuredHeight());
    }

    public void setThumbWidth(int thumbWidth) {
        mThumbWidth = thumbWidth;
    }

    public void setThumbDrawable(Drawable thumbDrawable) {
        mThumbDrawable = thumbDrawable;
        setBackground(mThumbDrawable);
    }

    public boolean inInTarget(int x, int y) {
        Rect rect = new Rect();
        getHitRect(rect);
        rect.left -= mExtendTouchSlop;
        rect.right += mExtendTouchSlop;
        rect.top -= mExtendTouchSlop;
        rect.bottom += mExtendTouchSlop;
        return rect.contains(x, y);
    }

    public int getRangeIndex() {
        return mTickIndex;
    }

    public void setTickIndex(int tickIndex) {
        mTickIndex = tickIndex;
    }

    @Override
    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void setPressed(boolean pressed) {
        mPressed = pressed;
    }
}
