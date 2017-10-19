package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.qiniu.pili.droid.shortvideo.PLTextView;

public class StrokedTextView extends PLTextView {
    private int mStrokeColor = Color.TRANSPARENT;
    private float mStrokeWidth;
    private Bitmap mAltBitmap;
    private Canvas mAltCanvas;
    private boolean isDrawing;

    public StrokedTextView(Context context) {
        this(context, null);
    }

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void invalidate() {
        // Ignore invalidate() calls when isDrawing == true
        // (setTextColor(color) calls will trigger them,
        // creating an infinite loop)
        if (isDrawing) return;
        super.invalidate();
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mStrokeWidth > 0) {
            isDrawing = true;
            if (mAltBitmap == null) {
                mAltBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                mAltCanvas = new Canvas(mAltBitmap);
            } else if (mAltCanvas.getWidth() != canvas.getWidth() ||
                    mAltCanvas.getHeight() != canvas.getHeight()) {
                mAltBitmap.recycle();
                mAltBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                mAltCanvas.setBitmap(mAltBitmap);
            }

            //draw the fill part of text
            super.onDraw(canvas);
            //save the text color
            int currentTextColor = getCurrentTextColor();
            //clear alternate canvas
            mAltBitmap.eraseColor(Color.TRANSPARENT);

            Paint p = getPaint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(mStrokeWidth);
            setTextColor(mStrokeColor);

            super.onDraw(mAltCanvas);
            canvas.drawBitmap(mAltBitmap, 0, 0, null);
            //set paint to fill mode (restore)
            setTextColor(currentTextColor);
            p.setStyle(Paint.Style.FILL);

            isDrawing = false;
        } else {
            super.onDraw(canvas);
        }
    }
}
