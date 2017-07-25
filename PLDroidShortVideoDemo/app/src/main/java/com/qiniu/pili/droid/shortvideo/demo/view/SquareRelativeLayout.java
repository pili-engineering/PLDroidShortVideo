package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class SquareRelativeLayout extends RelativeLayout {
    private static final String TAG = "SquareRelativeLayout";

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        Log.i(TAG, "specify width mode:" + View.MeasureSpec.toString(widthMeasureSpec) + " size:" + width);
        Log.i(TAG, "specify height mode:" + View.MeasureSpec.toString(heightMeasureSpec) + " size:" + height);

        setMeasuredDimension(width, width);
    }
}
