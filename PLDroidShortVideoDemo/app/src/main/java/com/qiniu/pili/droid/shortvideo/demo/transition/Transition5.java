package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLFadeTransition;
import com.qiniu.pili.droid.shortvideo.PLPositionTransition;
import com.qiniu.pili.droid.shortvideo.PLTextView;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class Transition5 extends TransitionBase {
    private PLTextView mTitleTip;
    private PLTextView mSubtitleTip;

    private static final int MOVE_DISTANCE = 100;

    public Transition5(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
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
        //title transitions
        PLFadeTransition fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mTitle, fadeTransition);
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mTitle.getX(), (int) mTitle.getY(), (int) mTitle.getX(), (int) mTitle.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mTitle, positionTransition);

        //subtitle transitions
        fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mSubtitle, fadeTransition);
        positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mSubtitle.getX(), (int) mSubtitle.getY(), (int) mSubtitle.getX(), (int) mSubtitle.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mSubtitle, positionTransition);

        //title tip transitions
        fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mTitleTip, fadeTransition);
        positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mTitleTip.getX(), (int) mTitleTip.getY(), (int) mTitleTip.getX(), (int) mTitleTip.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mTitleTip, positionTransition);

        //subtitle tip transitions
        fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mSubtitleTip, fadeTransition);
        positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mSubtitleTip.getX(), (int) mSubtitleTip.getY(), (int) mSubtitleTip.getX(), (int) mSubtitleTip.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mSubtitleTip, positionTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    private void initPosition() {
        int titleX = mWidth / 2 - mTitle.getWidth() / 2;
        int titleY = mHeight / 2 - mTitle.getHeight() + MOVE_DISTANCE;
        mTitle.setTranslationX(titleX);
        mTitle.setTranslationY(titleY);

        int titleTipX = mWidth / 2 - mTitleTip.getWidth() / 2;
        int titleTipY = titleY - mTitleTip.getHeight();
        mTitleTip.setTranslationX(titleTipX);
        mTitleTip.setTranslationY(titleTipY);

        int subtitleTipX = mWidth / 2 - mSubtitleTip.getWidth() / 2;
        int subtitleTipY = mHeight / 2 + MOVE_DISTANCE;
        mSubtitleTip.setTranslationX(subtitleTipX);
        mSubtitleTip.setTranslationY(subtitleTipY);

        int subtitleX = mWidth / 2 - mSubtitle.getWidth() / 2;
        int subtitleY = subtitleTipY + mSubtitleTip.getHeight();
        mSubtitle.setTranslationX(subtitleX);
        mSubtitle.setTranslationY(subtitleY);
    }


    @Override
    protected void initViews() {
        mTitle = new TransitionTextView(mContext);
        mTitle.setText("七牛");
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.parseColor("#FFCC99"));
        mTitle.setTextSize(16);

        mTitleTip = new TransitionTextView(mContext);
        mTitleTip.setText("DIRECTOR");
        mTitleTip.setPadding(0, 0, 0, 0);
        mTitleTip.setTextColor(Color.parseColor("#FFFFFF"));
        mTitleTip.setTextSize(16);

        mSubtitleTip = new TransitionTextView(mContext);
        mSubtitleTip.setText("DATE&LOCATION");
        mSubtitleTip.setPadding(0, 0, 0, 0);
        mSubtitleTip.setTextColor(Color.parseColor("#FFFFFF"));
        mSubtitleTip.setTextSize(16);

        mSubtitle = new TransitionTextView(mContext);
        mSubtitle.setText("2018.1.1 上海");
        mSubtitle.setPadding(0, 0, 0, 0);
        mSubtitle.setTextColor(Color.parseColor("#FFCC99"));
        mSubtitle.setTextSize(16);

        addViews();
        setViewsVisible(View.INVISIBLE);
    }

    @Override
    protected void addViews() {
        super.addViews();
        mTransitionMaker.addText(mTitleTip);
        mTransitionMaker.addText(mSubtitleTip);
    }

    @Override
    protected void setViewsVisible(int visible) {
        super.setViewsVisible(visible);
        mTitleTip.setVisibility(visible);
        mSubtitleTip.setVisibility(visible);
    }
}
