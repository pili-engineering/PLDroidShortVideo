package com.faceunity.view.seekbar.internal.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Simple {@link  StateDrawable} implementation
 * to draw circles/ovals
 *
 * @hide
 */
public class TrackOvalDrawable extends StateDrawable {
    private RectF mRectF = new RectF();

    public TrackOvalDrawable(@NonNull ColorStateList tintStateList) {
        super(tintStateList);
    }

    @Override
    void doDraw(Canvas canvas, Paint paint) {
        mRectF.set(getBounds());
        canvas.drawOval(mRectF, paint);
    }

}
