package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class FrameSelectorView extends RelativeLayout {
    private ImageView mHandlerLeft;
    private ImageView mHandlerRight;
    private View mHandlerBody;
    private FrameLayout.LayoutParams mGroupLayoutParam;

    private float mOriginX;
    private int mOriginWidth;
    private ViewGroup.LayoutParams mOriginParam;
    private int mOriginLeftMargin;

    private boolean mIsTouching;

    public FrameSelectorView(Context context) {
        this(context, null);
    }

    public FrameSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.frame_selector_view, this);

        mHandlerLeft = (ImageView) view.findViewById(R.id.handler_left);
        mHandlerRight = (ImageView) view.findViewById(R.id.handler_right);
        mHandlerBody = view.findViewById(R.id.handler_body);

        mHandlerLeft.setOnTouchListener(new HandlerLeftTouchListener());
        mHandlerRight.setOnTouchListener(new HandlerRightTouchListener());
        mHandlerBody.setOnTouchListener(new HandlerBodyTouchListener());

        post(new Runnable() {
            @Override
            public void run() {
                mGroupLayoutParam = (FrameLayout.LayoutParams) getLayoutParams();
            }
        });
    }

    private class HandlerLeftTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mIsTouching) {
                    return false;
                }
                mOriginX = event.getRawX();
                mOriginWidth = mHandlerBody.getWidth();
                mOriginParam = mHandlerBody.getLayoutParams();
                mOriginLeftMargin = mGroupLayoutParam.leftMargin;
                mIsTouching = true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                int delta = (int) (event.getRawX() - mOriginX);
                mOriginParam.width = mOriginWidth - delta;
                mHandlerBody.setLayoutParams(mOriginParam);
                mGroupLayoutParam.leftMargin = mOriginLeftMargin + delta;
                setLayoutParams(mGroupLayoutParam);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsTouching = false;
            }
            return true;
        }
    }

    private class HandlerRightTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mIsTouching) {
                    return false;
                }
                mOriginX = event.getRawX();
                mOriginWidth = mHandlerBody.getWidth();
                mOriginParam = mHandlerBody.getLayoutParams();
                mIsTouching = true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                int delta = (int) (event.getRawX() - mOriginX);
                mOriginParam.width = mOriginWidth + delta;
                mHandlerBody.setLayoutParams(mOriginParam);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsTouching = false;
            }
            return true;
        }
    }

    private class HandlerBodyTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mIsTouching) {
                    return false;
                }
                mOriginX = event.getRawX();
                mOriginLeftMargin = mGroupLayoutParam.leftMargin;
                mIsTouching = true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                int delta = (int) (event.getRawX() - mOriginX);
                mGroupLayoutParam.leftMargin = mOriginLeftMargin + delta;
                setLayoutParams(mGroupLayoutParam);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsTouching = false;
            }
            return true;
        }
    }

    public int getBodyLeft() {
        if (mGroupLayoutParam == null){
            mGroupLayoutParam = (FrameLayout.LayoutParams) getLayoutParams();
        }
        return mGroupLayoutParam.leftMargin + mHandlerLeft.getWidth();
    }

    public int getLeftHandlerWidth() {
        return mHandlerLeft.getWidth();
    }

    public int getBodyWidth() {
        int width = mHandlerBody.getWidth();
        if (width == 0) {
            float scale = getResources().getDisplayMetrics().density;
            int preferWidth = (int) ((100 * scale) + 0.5f);
            int preferHeight = getLayoutParams().height;

            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(preferWidth, MeasureSpec.EXACTLY);
            int heightMeasureSpec = MeasureSpec.makeMeasureSpec(preferHeight, MeasureSpec.EXACTLY);
            mHandlerBody.measure(widthMeasureSpec, heightMeasureSpec);
            return mHandlerBody.getMeasuredWidth();
        }
        return width;
    }

    public int getBodyRight() {
        return getBodyLeft() + mHandlerBody.getWidth();
    }

    public void setBodyLeft(int left) {
        mGroupLayoutParam.leftMargin = left;
        setLayoutParams(mGroupLayoutParam);
    }

    public void setBodyWidth(int width){
        mOriginParam = mHandlerBody.getLayoutParams();
        mOriginParam.width = width;
        mHandlerBody.setLayoutParams(mOriginParam);
    }
}
