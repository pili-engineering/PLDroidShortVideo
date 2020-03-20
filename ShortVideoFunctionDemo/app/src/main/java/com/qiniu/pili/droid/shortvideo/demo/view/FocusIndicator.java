package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class FocusIndicator extends View {
    private int mState;
    private static final int STATE_IDLE = 0;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_FINISHING = 2;

    private Runnable mDisappear = new Disappear();
    private Runnable mEndAction = new EndAction();

    private static final int SCALING_UP_TIME = 1000;
    private static final int SCALING_DOWN_TIME = 200;
    private static final int DISAPPEAR_TIMEOUT = 200;

    public FocusIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void focus() {
        if (mState == STATE_IDLE) {
            setBackgroundResource(R.drawable.ic_focus_focusing);
            animate().withLayer().setDuration(SCALING_UP_TIME)
                    .scaleX(1.5f).scaleY(1.5f);
            mState = STATE_FOCUSING;
        }
    }

    public void focusSuccess() {
        if (mState == STATE_FOCUSING) {
            setBackgroundResource(R.drawable.ic_focus_focused);
            animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
                    .scaleY(1f).withEndAction(mEndAction);
            mState = STATE_FINISHING;
        }
    }

    public void focusFail() {
        if (mState == STATE_FOCUSING) {
            setBackgroundResource(R.drawable.ic_focus_failed);
            animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
                    .scaleY(1f).withEndAction(mEndAction);
            mState = STATE_FINISHING;
        }
    }

    public void focusCancel() {
        animate().cancel();
        removeCallbacks(mDisappear);
        mDisappear.run();
        setScaleX(1f);
        setScaleY(1f);
    }

    private class EndAction implements Runnable {
        @Override
        public void run() {
            // Keep the focus indicator for some time.
            postDelayed(mDisappear, DISAPPEAR_TIMEOUT);
        }
    }

    private class Disappear implements Runnable {
        @Override
        public void run() {
            setBackgroundDrawable(null);
            mState = STATE_IDLE;
        }
    }
}
