package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLFadeTransition;
import com.qiniu.pili.droid.shortvideo.PLPositionTransition;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class Transition4 extends TransitionBase {
    private static final int MOVE_DISTANCE = 100;

    public Transition4(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
        super(viewGroup, setting);
    }

    @Override
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
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mTitle.getX(), (int) mTitle.getY(), (int) mTitle.getX(), (int) mTitle.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mTitle, positionTransition);

        fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mSubtitle, fadeTransition);
        positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mSubtitle.getX(), (int) mSubtitle.getY(), (int) mSubtitle.getX(), (int) mSubtitle.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mSubtitle, positionTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    private void initPosition() {
        int titleX = mWidth / 2 - mTitle.getWidth() / 2;
        int titleY = mHeight / 2 - mTitle.getHeight() + MOVE_DISTANCE;
        mTitle.setTranslationX(titleX);
        mTitle.setTranslationY(titleY);

        int subtitleX = mWidth / 2 - mSubtitle.getWidth() / 2;
        int subtitleY = titleY + mTitle.getHeight();
        mSubtitle.setTranslationX(subtitleX);
        mSubtitle.setTranslationY(subtitleY);
    }

    @Override
    protected void initViews() {
        mTitle = new TransitionTextView(mContext);
        mTitle.setText("挪威的森林");
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTextSize(22);

        mSubtitle = new TransitionTextView(mContext);
        mSubtitle.setText("- 村上春树 -");
        mSubtitle.setPadding(0, 0, 0, 0);
        mSubtitle.setTextColor(Color.parseColor("#eed2b9"));
        mSubtitle.setTextSize(15);

        addViews();
        setViewsVisible(View.INVISIBLE);
    }
}
