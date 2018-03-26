package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLFadeTransition;
import com.qiniu.pili.droid.shortvideo.PLPositionTransition;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class Transition0 extends TransitionBase {
    public Transition0(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
        super(viewGroup, setting);
    }

    @Override
    protected void initPosAndTrans() {
        //you should init positions and transitions in post runnable , because the view has been layout at that moment.
        mTitle.post(new Runnable() {
            @Override
            public void run() {
                initPosition();
                initTransitions();
            }
        });
    }

    private void initTransitions() {
        PLFadeTransition fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mTitle, fadeTransition);
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mTitle.getX(), (int) mTitle.getY(), (int) mTitle.getX(), (int) mTitle.getY() - mTitle.getHeight());
        mTransitionMaker.addTransition(mTitle, positionTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    private void initPosition() {
        int titleX = mWidth / 2 - mTitle.getWidth() / 2;
        int titleY = mHeight / 2 + mTitle.getHeight() / 2;
        mTitle.setTranslationX(titleX);
        mTitle.setTranslationY(titleY);
    }

    @Override
    protected void initViews() {
        mTitle = new TransitionTextView(mContext);
        mTitle.setText("七月与安生");
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTextSize(26);

        addViews();
        setViewsVisible(View.INVISIBLE);
    }
}
