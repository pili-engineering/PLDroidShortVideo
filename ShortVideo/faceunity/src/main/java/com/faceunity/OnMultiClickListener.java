package com.faceunity;

import android.view.View;

/**
 * @author LiuQiang on 2018.11.09
 * 防止控件快速点击
 */
public abstract class OnMultiClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private long mLastClickTime;
    private int mViewId = View.NO_ID;

    /**
     * 处理后的点击事件
     *
     * @param v
     */
    protected abstract void onMultiClick(View v);

    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        int viewId = v.getId();
        if (mViewId == viewId) {
            if ((curClickTime - mLastClickTime) >= MIN_CLICK_DELAY_TIME) {
                mLastClickTime = curClickTime;
                onMultiClick(v);
            }
        } else {
            mViewId = viewId;
            mLastClickTime = curClickTime;
            onMultiClick(v);
        }
    }
}
