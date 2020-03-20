package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

public class VideoMixGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "VideoMixGLSurfaceView";
    private static final float VIDEO_MIX_RATIO = 16f / 9f;

    public VideoMixGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        Log.i(TAG, "specify width mode:" + MeasureSpec.toString(widthMeasureSpec) + " size:" + width);
        Log.i(TAG, "specify height mode:" + MeasureSpec.toString(heightMeasureSpec) + " size:" + height);

        setMeasuredDimension(width, (int) (width * VIDEO_MIX_RATIO));
    }
}
