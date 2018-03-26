package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLFadeTransition;
import com.qiniu.pili.droid.shortvideo.PLPositionTransition;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class Transition1 extends TransitionBase {
    public Transition1(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
        super(viewGroup, setting);
    }

    @Override
    public void init() {
        mTransitionMaker.setDuration(DURATION);
        mTransitionMaker.setBackgroundColor(Color.BLACK);

        initViews();
        initPosAndTrans();
    }

    @Override
    public void updateTransitions() {
        mTransitionMaker.removeAllResource();
        addViews();
        initPosAndTrans();
    }

    @Override
    protected void initViews() {
        mTitle = new TransitionTextView(mContext);
        mTitle.setText("CHAPTER");
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.parseColor("#FFCC99"));
        mTitle.setTextSize(14);

        mSubtitle = new TransitionTextView(mContext);
        mSubtitle.setText("第一章 七年");
        mSubtitle.setPadding(0, 0, 0, 0);
        mSubtitle.setTextColor(Color.WHITE);
        mSubtitle.setTextSize(20);

        addViews();
        setViewsVisible(View.INVISIBLE);
    }

    protected void initPosAndTrans() {
        //you should init positions and transitions in post runnable , because the view has been layout at that moment.
        mSubtitle.post(new Runnable() {
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
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mSubtitle.getX(), (int) mSubtitle.getY(), (int) mSubtitle.getX() - mSubtitle.getWidth(), (int) mSubtitle.getY());
        mTransitionMaker.addTransition(mSubtitle, positionTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    private void initPosition() {
        mTitle.setTranslationX(mWidth - mTitle.getWidth());
        mTitle.setTranslationY(mHeight / 2 - mTitle.getHeight());

        mSubtitle.setTranslationX(mWidth);
        mSubtitle.setTranslationY(mTitle.getY() + mTitle.getHeight());
    }
}