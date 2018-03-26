package com.qiniu.pili.droid.shortvideo.demo.transition;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLTransitionMaker;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionTextView;

public class TransitionBase {
    protected static final int DURATION = 2500;

    private ViewGroup mViewGroup;

    protected PLTransitionMaker mTransitionMaker;
    protected Context mContext;
    protected TransitionTextView mTitle;
    protected TransitionTextView mSubtitle;
    protected int mWidth;
    protected int mHeight;

    public TransitionBase(ViewGroup viewGroup, PLVideoEncodeSetting setting) {
        mViewGroup = viewGroup;
        mContext = viewGroup.getContext();
        mWidth = mViewGroup.getWidth();
        mHeight = mViewGroup.getHeight();
        mTransitionMaker = new PLTransitionMaker(mViewGroup, setting);
        init();
    }

    public TransitionTextView getTitle() {
        return mTitle;
    }

    public TransitionTextView getSubtitle() {
        return mSubtitle;
    }

    public void init() {
        mTransitionMaker.setDuration(DURATION);
        mTransitionMaker.setBackgroundColor(Color.BLACK);

        initViews();
        initPosAndTrans();
    }

    public void save(String dstFilePath, PLVideoSaveListener saveListener) {
        mTransitionMaker.save(dstFilePath, saveListener);
    }

    public void setVisibility(int visibility) {
        mViewGroup.setVisibility(visibility);
    }

    public void play() {
        mTransitionMaker.play();
    }

    public void stop() {
        mTransitionMaker.stop();
    }

    public void destroy() {
        mTransitionMaker.destroy();
    }

    public void cancelSave() {
        mTransitionMaker.cancelSave();
    }

    public void updateTransitions() {
        mTransitionMaker.removeAllResource();
        addViews();
        initPosAndTrans();
    }

    protected void initViews() {
    }

    protected void initPosAndTrans() {
    }

    protected void addViews() {
        if (mTitle != null) {
            mTransitionMaker.addText(mTitle);
        }
        if (mSubtitle != null) {
            mTransitionMaker.addText(mSubtitle);
        }
    }

    protected void setViewsVisible(int visible) {
        if (mTitle != null) {
            mTitle.setVisibility(visible);
        }
        if (mSubtitle != null) {
            mSubtitle.setVisibility(visible);
        }
    }
}