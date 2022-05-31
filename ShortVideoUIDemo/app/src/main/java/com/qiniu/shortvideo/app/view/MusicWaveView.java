package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.Utils;

import java.util.Random;

/**
 * 音乐波形图控件
 */
public class MusicWaveView extends View {

    private long mVideoDurationMs;
    private long mMusicDurationMs;
    private float[] mWaveArray;
    private int mWaveHeight;
    private int mScreenWidth;
    private int mSelectBgWidth;

    private static final int WAVE_WIDTH = 6;
    private static final int WAVE_OFFSET = 2;
    private static final float MIN_WAVE_RATE = 0.25f;

    private Paint mPaint = new Paint();
    private Rect mRect = new Rect();

    private int mWidth;
    private int mHeight;

    public MusicWaveView(Context context) {
        super(context);
        init(context);
    }

    public MusicWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MusicWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mWaveHeight = Utils.dip2px(context, 40);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mSelectBgWidth = Utils.dip2px(context, 200);
        setWillNotDraw(false);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
    }

    public void layout(){
        if(mVideoDurationMs != 0 && mMusicDurationMs != 0){
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            params.width = mScreenWidth;
            setLayoutParams(params);
            mWidth = params.width;
            mHeight = params.height;
            generateWaveArray();
            invalidate();
        }
    }

    private void generateWaveArray(){
        int count = mWidth / (WAVE_WIDTH + WAVE_OFFSET);
        mWaveArray = new float[count];
        Random random = new Random();
        random.setSeed(mMusicDurationMs);
        for(int i = 0; i < count; i++){
            mWaveArray[i] = random.nextFloat();
            if(mWaveArray[i] <  MIN_WAVE_RATE){
                mWaveArray[i] += MIN_WAVE_RATE;
            }
        }
    }

    public void setVideoDuration(long videoDuration){
        mVideoDurationMs = videoDuration;
    }

    public void setMusicDuration(long musicDuration){
        mMusicDurationMs = musicDuration;
    }

    public int getMusicLayoutWidth(){
        return mWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getHeight() != 0 && mWaveArray != null){
            for(int i = 0; i < mWaveArray.length; i++){
                int height = (int) (mWaveHeight * mWaveArray[i]);
                int left = i * (WAVE_OFFSET + WAVE_WIDTH);
                int right = left + WAVE_WIDTH;
                int top = (getHeight() -  height)/2;
                int bottom = top + height ;
                mRect.set(left, top, right, bottom);
                canvas.drawRect(mRect, mPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        if (modeW == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (modeW == MeasureSpec.EXACTLY) {
            width = widthMeasureSpec;
        }
        if (modeW == MeasureSpec.UNSPECIFIED) {
            width = mWidth;
        }

        int modeH = MeasureSpec.getMode(height);
        if (modeH == MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (modeH == MeasureSpec.EXACTLY) {
            height = heightMeasureSpec;
        }
        if (modeH == MeasureSpec.UNSPECIFIED) {
            //ScrollView和HorizontalScrollView
            height = mHeight;
        }
        setMeasuredDimension(width, height);
    }
}
