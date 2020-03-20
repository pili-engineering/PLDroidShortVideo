package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLFadeTransition;
import com.qiniu.pili.droid.shortvideo.PLImageView;
import com.qiniu.pili.droid.shortvideo.PLPositionTransition;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class Transition2 extends TransitionBase {
    private PLImageView mImageView;

    public Transition2(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
        super(viewGroup, setting);
    }

    @Override
    protected void initPosAndTrans() {
        //you should init positions and transitions in post runnable , because the view has been layout at that moment.
        mImageView.post(new Runnable() {
            @Override
            public void run() {
                initPosition();
                initTransitions();
            }
        });
    }

    @Override
    protected void initViews() {
        mTitle = new TransitionTextView(mContext);
        mTitle.setText("第二章 暖暖");
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTextSize(22);

        mImageView = new PLImageView(mContext);
        mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pink_line));

        addViews();
        setViewsVisible(View.INVISIBLE);
    }

    private void initPosition() {
        int titleY = mHeight / 2 - mTitle.getHeight();
        mTitle.setTranslationX(0);
        mTitle.setTranslationY(titleY);

        mImageView.setTranslationY(mHeight / 2 - mTitle.getHeight() - mImageView.getHeight());
        mImageView.setTranslationX(0);
    }

    private void initTransitions() {
        PLFadeTransition fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mImageView, fadeTransition);
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, -mTitle.getWidth(), (int) mTitle.getY(), (int) mTitle.getX(), (int) mTitle.getY());
        mTransitionMaker.addTransition(mTitle, positionTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    @Override
    protected void addViews() {
        super.addViews();
        mTransitionMaker.addImage(mImageView);
    }

    @Override
    protected void setViewsVisible(int visible) {
        super.setViewsVisible(visible);
        mImageView.setVisibility(visible);
    }
}
