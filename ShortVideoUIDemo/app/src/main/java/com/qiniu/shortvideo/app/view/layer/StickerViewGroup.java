package com.qiniu.shortvideo.app.view.layer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴纸的容器
 */
public class StickerViewGroup extends FrameLayout implements View.OnClickListener{
    private final String TAG = "StickerViewGroup";
    private List<StickerImageView> mStickerImageViews;
    private int mLastSelectedPos = -1;
    private boolean mEnableChildSingleClick = true;
    private boolean mEnableChildDoubleClick = false;

    long mLastTime=0;
    long mCurTime=0;

    public StickerViewGroup(Context context) {
        super(context);
        init();
    }

    public StickerViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStickerImageViews = new ArrayList<StickerImageView>();
    }

    public void addOperationView(StickerImageView view) {
        mStickerImageViews.add(view);
        selectOperationView(mStickerImageViews.size() - 1);
        addView(view);
        view.setOnClickListener(this);
    }

    public void removeOperationView(StickerImageView view) {
        int viewIndex = mStickerImageViews.indexOf(view);
        mStickerImageViews.remove(view);
        mLastSelectedPos = -1;
        removeView(view);
        view.setOnClickListener(null);
    }

    public StickerImageView getOperationView(int index) {
        return mStickerImageViews.get(index);
    }

    public void selectOperationView(int pos) {
        if (pos < mStickerImageViews.size() && pos >= 0) {
            if (mLastSelectedPos != -1) {
                mStickerImageViews.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            }
            mStickerImageViews.get(pos).setEditable(true);//显示编辑的边框
            mLastSelectedPos = pos;
        }
    }

    private void unSelectOperationView(int pos) {
        if (pos < mStickerImageViews.size() && mLastSelectedPos != -1) {
            mStickerImageViews.get(mLastSelectedPos).setEditable(false);//不显示编辑的边框
            mLastSelectedPos = -1;
        }
    }

    public StickerImageView getSelectedLayerOperationView() {
        if (mLastSelectedPos < 0 || mLastSelectedPos >= mStickerImageViews.size()) return null;
        return mStickerImageViews.get(mLastSelectedPos);
    }

    public int getSelectedViewIndex() {
        return mLastSelectedPos;
    }

    public int getChildCount() {
        return mStickerImageViews.size();
    }

    public void enableChildSingleClick(boolean enable){
        mEnableChildSingleClick = enable;
    }

    public void enableDoubleChildClick(boolean enable){
        mEnableChildDoubleClick = enable;
    }

    @Override
    public void onClick(View v) {
        mLastTime = mCurTime;
        mCurTime = System.currentTimeMillis();
        if (mCurTime - mLastTime < 300) {//双击事件
            mCurTime =0;
            mLastTime = 0;
            if (mEnableChildDoubleClick) {
                onItemClick(v);
            }
        } else {//单击事件
            if (mEnableChildSingleClick) {
                onItemClick(v);
            }
        }
    }

    private void onItemClick(View v){
        StickerImageView stickerImageView = (StickerImageView) v;
        int pos = mStickerImageViews.indexOf(stickerImageView);
        int lastPos = mLastSelectedPos;
        selectOperationView(pos); //选中编辑
        if (mListener != null) {
            mListener.onStickerItemClicked(stickerImageView, lastPos, pos);
        }
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onStickerItemClicked(StickerImageView view, int lastSelectedPos, int currentSelectedPos);
    }

}
