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

public class Transition3 extends TransitionBase {
    private static final int MOVE_DISTANCE = 100;

    private static final String TEXT_DISPLAY = "村上春树1949年1月12日出生在日本京都市伏见区，为国语教师村上千秋、村上美幸夫妇的长子。出生不久，家迁至兵库县西宫市夙川。村上春树1949年1月12日出生在日本京都市伏见区，为国语教师村上千秋、村上美幸夫妇的长子。出生不久，家迁至兵库县西宫市夙川。";

    private PLImageView mImageView;

    public Transition3(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
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
        mTitle.setText(TEXT_DISPLAY);
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setTextColor(Color.parseColor("#339900"));
        mTitle.setTextSize(16);

        mImageView = new PLImageView(mContext);
        mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.green_quot));

        addViews();
        setViewsVisible(View.INVISIBLE);
    }

    private void initTransitions() {
        PLPositionTransition positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mTitle.getX(), (int) mTitle.getY(), (int) mTitle.getX(), (int) mTitle.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mTitle, positionTransition);
        PLFadeTransition fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mTitle, fadeTransition);

        positionTransition = new PLPositionTransition(0, DURATION / 2, (int) mImageView.getX(), (int) mImageView.getY(), (int) mImageView.getX(), (int) mImageView.getY() - MOVE_DISTANCE);
        mTransitionMaker.addTransition(mImageView, positionTransition);
        fadeTransition = new PLFadeTransition(0, DURATION / 2, 0, 1);
        mTransitionMaker.addTransition(mImageView, fadeTransition);

        mTransitionMaker.play();
        setViewsVisible(View.VISIBLE);
    }

    private void initPosition() {
        int titleY = mHeight / 2 - mTitle.getHeight() / 2 + MOVE_DISTANCE;
        mTitle.setTranslationX(0);
        mTitle.setTranslationY(titleY);

        int imageY = titleY - mImageView.getHeight();
        mImageView.setTranslationY(imageY);
        mImageView.setTranslationX(0);
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
