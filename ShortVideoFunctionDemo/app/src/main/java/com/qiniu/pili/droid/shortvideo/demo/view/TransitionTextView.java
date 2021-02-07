package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.util.AttributeSet;

import com.qiniu.pili.droid.shortvideo.PLTextView;

public class TransitionTextView extends PLTextView {
    public TransitionTextView(Context context) {
        this(context, null);
    }

    public TransitionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setClickable(false);
        setPadding(0, 0, 0, 0);
    }
}
