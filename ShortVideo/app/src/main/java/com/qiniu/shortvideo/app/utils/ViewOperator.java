package com.qiniu.shortvideo.app.utils;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.qiniu.shortvideo.app.view.BaseBottomView;

/**
 * 视图动画操作的工具类
 */
public class ViewOperator {
    private static final String TAG = "ViewOperator";
    private float SCALE_SIZE = 0.6f;

    private RelativeLayout mRootView;
    private ViewGroup mTitleView;
    // 底部功能选择视图
    private View mBottomRootView;
    // 具体的功能视图
    private BaseBottomView mBottomEditorView;
    private ViewGroup mPlayerView;

    private int mRootViewHeight;
    private int mPlayerViewWidth;
    private int mPlayerViewHeight;
    private int mBottomEditorViewHeight;

    private int mPlayerViewMarginTop;
    private int mAnimateMoveLength;

    public ViewOperator(RelativeLayout rootView, ViewGroup titleView, View bottomRootView, ViewGroup playerView) {
        mRootView = rootView;
        mTitleView = titleView;
        mBottomRootView = bottomRootView;
        mPlayerView = playerView;
    }

    public void showBottomView(BaseBottomView bottomEditorView) {
        if (mBottomEditorView != null) {
            return;
        }
        mBottomRootView.setVisibility(View.GONE);

        ViewGroup.LayoutParams lp = mPlayerView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            mPlayerViewMarginTop = ((ViewGroup.MarginLayoutParams) lp).topMargin;
        } else {
            mPlayerViewMarginTop = 0;
        }

        mRootViewHeight = mRootView.getHeight();
        mPlayerViewWidth = mPlayerView.getWidth();
        mPlayerViewHeight = mPlayerView.getHeight();
        mBottomEditorViewHeight = bottomEditorView.getCalculateHeight();
        mAnimateMoveLength = Math.abs(Utils.dip2px(mRootView.getContext(), 10) - mPlayerViewMarginTop);

        if (!bottomEditorView.isTitleBarVisible()) {
            startDisappearAnimOnTop(mTitleView);
            mTitleView.setVisibility(View.INVISIBLE);
        }
        int count = mTitleView.getChildCount();
        for (int i = 0; i < count; i++) {
            mTitleView.getChildAt(i).setClickable(false);
        }

        if (bottomEditorView.isPlayerNeedZoom()) {
            int height = mRootViewHeight - mBottomEditorViewHeight - Utils.dip2px(mRootView.getContext(), 20);
            SCALE_SIZE = (float) height / mPlayerViewHeight;
            if (SCALE_SIZE >= 0.95f) {
                SCALE_SIZE = 0.95f;
            }
            ValueAnimator playerAnimator = ValueAnimator.ofFloat(1f, SCALE_SIZE);
            playerAnimator.setDuration(250);
            playerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = mPlayerView.getLayoutParams();
                    lp.width = (int) (mPlayerViewWidth * animatedValue);
                    lp.height = (int) (mPlayerViewHeight * animatedValue);

                    ViewGroup.MarginLayoutParams marginLayoutParams;
                    if (lp instanceof ViewGroup.MarginLayoutParams) {
                        marginLayoutParams = (ViewGroup.MarginLayoutParams) lp;
                    } else {
                        marginLayoutParams = new ViewGroup.MarginLayoutParams(lp);
                    }
                    int marginTop = (int) Math.abs(mPlayerViewMarginTop - mAnimateMoveLength * (1 - animatedValue) / (1 - SCALE_SIZE));
                    marginLayoutParams.setMargins(0, marginTop, 0, 0);
                    mPlayerView.setLayoutParams(marginLayoutParams);
                }
            });
            playerAnimator.start();
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mRootView.addView(bottomEditorView, layoutParams);
        startAppearAnimY(bottomEditorView);
        mBottomEditorView = bottomEditorView;
    }

    public void hideBottomView() {
        if (mBottomEditorView == null) {
            return;
        }

        startAppearAnimY(mBottomRootView);
        mBottomRootView.setVisibility(View.VISIBLE);

        if (!mBottomEditorView.isTitleBarVisible()) {
            startAppearAnimOnTop(mTitleView);
            mTitleView.setVisibility(View.VISIBLE);
        }
        int count = mTitleView.getChildCount();
        for (int i = 0; i < count; i++) {
            mTitleView.getChildAt(i).setClickable(true);
        }

        if (mBottomEditorView.isPlayerNeedZoom()) {
            ValueAnimator animator = ValueAnimator.ofFloat(SCALE_SIZE, 1.0f);
            animator.setDuration(250);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = mPlayerView.getLayoutParams();
                    lp.width = (int) (mPlayerViewWidth * animatedValue);
                    lp.height = (int) (mPlayerViewHeight * animatedValue);
                    ViewGroup.MarginLayoutParams marginLayoutParams;
                    if (lp instanceof ViewGroup.MarginLayoutParams) {
                        marginLayoutParams = (ViewGroup.MarginLayoutParams) lp;
                    } else {
                        marginLayoutParams = new ViewGroup.MarginLayoutParams(lp);
                    }
                    int marginTop = (int) Math.abs(mPlayerViewMarginTop - mAnimateMoveLength * (1 - animatedValue) / (1 - SCALE_SIZE));
                    marginLayoutParams.setMargins(0, marginTop, 0, 0);
                    mPlayerView.setLayoutParams(marginLayoutParams);
                }
            });
            animator.start();
        }
        startDisappearAnimY(mBottomEditorView);
        mBottomEditorView.removeSelf();
        mBottomEditorView = null;
    }

    private void startAppearAnimOnTop(final View view) {
        final TranslateAnimation showAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(250);
        view.startAnimation(showAnim);
    }

    private void startDisappearAnimOnTop(final View view) {
        final TranslateAnimation hideAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        hideAnim.setDuration(250);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (view != null){
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(hideAnim);
    }

    public static void startAppearAnimY(View view) {
        TranslateAnimation showAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(250);
        view.startAnimation(showAnim);
    }

    public static void startDisappearAnimY(View view) {
        final TranslateAnimation hideAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        hideAnim.setDuration(250);
        view.startAnimation(hideAnim);
    }
}
