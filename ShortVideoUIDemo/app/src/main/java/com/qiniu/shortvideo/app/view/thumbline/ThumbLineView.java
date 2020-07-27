package com.qiniu.shortvideo.app.view.thumbline;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.ThumbLineViewAdapter;
import com.qiniu.shortvideo.app.utils.Utils;
import com.qiniu.shortvideo.app.utils.ThumbLineViewSettings;

import java.util.ArrayList;
import java.util.List;

import static com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar.STATE_ACTIVE;
import static com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar.STATE_FIX;

/**
 * 编辑模块视频缩略图展示界面控件
 *
 * 包含了缩略图随视频播放而滚动，特效添加效果处理等
 */
public class ThumbLineView extends FrameLayout {
    private static final String TAG = "ThumbLineView";

    private static final int WHAT_THUMBNAIL_VIEW_AUTO_MOVE = 1;
    private static final int WHAT_TIMELINE_ON_SEEK = 2;
    private static final int WHAT_TIMELINE_FINISH_SEEK = 3;

    private static final String KEY_SEEK_PERCENT = "seek_percent";
    private static final String KEY_POSITION = "position";
    private static final String KEY_NEED_CALLBACK = "need_callback";

    private RecyclerView mThumbRecyclerView;
    private ThumbLineViewAdapter mThumbLineViewAdapter;
    private ThumbLineViewSettings mThumbLineViewSettings;
    private OnThumbLineSeekListener mThumbLineSeekListener;
    private boolean mIsTouching = false;
    private int mIndicatorMargin;
    protected volatile int mCurrentPosition;
    protected float mThumbLineViewWidth;
    protected int mScrollState;
    protected float mCurrScroll;
    private int mCurrentActiveIndex = -1;

    private List<ThumbLineRangeBar> mOverlayList = new ArrayList<>();

    /**
     * 滑动时的监听器
     */
    public interface OnThumbLineSeekListener {
        /**
         * 正在滑动时的监听
         *
         * @param duration 时间
         */
        void onThumbLineSeek(int duration);

        /**
         * 滑动完成时的监听
         *
         * @param duration 时间
         */
        void onThumbLineSeekFinish(int duration);
    }

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int position = msg.getData().getInt(KEY_POSITION);
            switch (msg.what) {
                case WHAT_THUMBNAIL_VIEW_AUTO_MOVE:
                    float seekPercent = msg.getData().getFloat(KEY_SEEK_PERCENT);
                    scroll(seekPercent);
                    break;
                case WHAT_TIMELINE_ON_SEEK:
                    mThumbLineSeekListener.onThumbLineSeek(position);
                    break;
                case WHAT_TIMELINE_FINISH_SEEK:
                    mThumbLineSeekListener.onThumbLineSeekFinish(position);
                    break;
                default:
                    break;
            }
        }
    };

    public ThumbLineView(@NonNull Context context) {
        this(context, null);
    }

    public ThumbLineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbLineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setup(ThumbLineViewSettings thumbLineViewSettings, OnThumbLineSeekListener listener) {
        mThumbLineViewSettings = thumbLineViewSettings;
        mThumbLineSeekListener = listener;
        initLayoutParams();

        mThumbRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIsTouching = true;

                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mIsTouching = false;
                        // TODO : 缩略条的操作监听
                        break;
                }
                return false;
            }
        });

        mThumbRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_FINISH_SEEK);
                        Bundle data = new Bundle();
                        data.putInt(KEY_POSITION, mCurrentPosition);
                        msg.setData(data);
                        mUIHandler.sendMessage(msg);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Log.d(TAG, "onScrollStateChanged : SCROLL_STATE_DRAGGING");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        Log.d(TAG, "onScrollStateChanged : SCROLL_STATE_SETTLING");
                        break;
                    default:
                        break;
                }

                onRecyclerViewScrollStateChanged(newState);
                mScrollState = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mCurrScroll += dx;
                float seekPercent = mCurrScroll / getThumbLineViewWidth();
                mCurrentPosition = (int) (seekPercent * mThumbLineViewSettings.getVideoDuration());
                if (mThumbLineSeekListener != null && (mIsTouching || mScrollState == RecyclerView.SCROLL_STATE_SETTLING)) {
                    Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_ON_SEEK);
                    Bundle data = new Bundle();
                    data.putInt(KEY_POSITION, mCurrentPosition);
                    msg.setData(data);
                    mUIHandler.sendMessage(msg);
                }
                onRecyclerViewScroll(dx, dy);
            }
        });

        if (mThumbLineViewAdapter == null) {
            mThumbLineViewAdapter = new ThumbLineViewAdapter(mThumbLineViewSettings);
            mThumbRecyclerView.setAdapter(mThumbLineViewAdapter);
        }
    }

    public float getThumbLineViewWidth() {
        if (mThumbLineViewWidth == 0) {
            mThumbLineViewWidth = mThumbLineViewSettings.getThumbnailCount() * mThumbLineViewSettings.getThumbnailWidth();
        }
        return mThumbLineViewWidth;
    }

    public void seekTo(int position) {
        mCurrentPosition = position;
        float seekPercent = position * 1.0f / mThumbLineViewSettings.getVideoDuration();
        Message msg = mUIHandler.obtainMessage(WHAT_THUMBNAIL_VIEW_AUTO_MOVE);
        Bundle data = new Bundle();
        data.putFloat(KEY_SEEK_PERCENT, seekPercent);
        data.putInt(KEY_POSITION, position);
        msg.setData(data);
        mUIHandler.sendMessage(msg);
    }

    public void addBitmap(Bitmap bitmap) {
        if (mThumbLineViewAdapter != null) {
            mThumbLineViewAdapter.addBitmap(bitmap);
        }
    }

    /**
     * 判断视频缩略图进度条是否处于滚动状态
     *
     * @return 是或否
     */
    public boolean isScrolling() {
        return mScrollState != RecyclerView.SCROLL_STATE_IDLE;
    }

    /**
     * 是否正在操作缩略图进度条
     *
     * @return 是或否
     */
    public boolean isTouching() {
        return mIsTouching;
    }

    /**
     * 添加 overlay
     *
     * @param overlayView overlayView
     * @param headView    tailView
     * @param overlay     overlay
     */
    public void addOverlayView(final View overlayView, final ThumbLineHandleView headView, final ThumbLineRangeBar overlay) {
        addView(overlayView);
        final View view = headView.getView();

        overlayView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = calculateHeadViewPosition(headView);
                view.requestLayout();
                overlay.setVisibility(true);
            }
        });
    }

    public ThumbLineRangeBar addOverlayRangeBar(long startTime, long duration, ThumbLineRangeBar.ThumbLineRangeBarView view, long minDuration, ThumbLineRangeBar.OnSelectedDurationChangeListener listener) {
        if (startTime < 0) {
            startTime = 0;
        }
        if (!mOverlayList.isEmpty()) {
            mOverlayList.get(mCurrentActiveIndex).switchState(STATE_FIX);
        }
        ThumbLineRangeBar rangeBar = new ThumbLineRangeBar(this, startTime, duration, minDuration, mThumbLineViewSettings.getVideoDuration(),view, listener);
        rangeBar.switchState(STATE_ACTIVE);
        mOverlayList.add(rangeBar);
        mCurrentActiveIndex = mOverlayList.indexOf(rangeBar);
        return rangeBar;
    }

    public ThumbLineRangeBar addOverlayRangeBar(ThumbLineRangeBar rangeBar) {
        if (!mOverlayList.isEmpty() && mCurrentActiveIndex != -1) {
            mOverlayList.get(mCurrentActiveIndex).switchState(STATE_FIX);
        }
        rangeBar.switchState(STATE_ACTIVE);
        mOverlayList.add(rangeBar);
        mCurrentActiveIndex = mOverlayList.indexOf(rangeBar);
        return rangeBar;
    }

    public void removeOverlayRangeBar(ThumbLineRangeBar rangeBar) {
        if (mOverlayList == null) {
            return;
        }
        if (mCurrentActiveIndex == mOverlayList.indexOf(rangeBar)) {
            mCurrentActiveIndex = -1;
        }
        if (rangeBar != null) {
            mOverlayList.remove(rangeBar);
            removeView(rangeBar.getOverlayRangeBar());
        }
    }

    public void removeOverlayRangeBar(int index) {
        if (mOverlayList == null) {
            return;
        }
        ThumbLineRangeBar rangeBar = mOverlayList.get(index);
        if (mCurrentActiveIndex == mOverlayList.indexOf(rangeBar)) {
            mCurrentActiveIndex = -1;
        }
        if (rangeBar != null) {
            mOverlayList.remove(rangeBar);
            removeView(rangeBar.getOverlayRangeBar());
        }
    }

    public void switchRangeBarToActive(ThumbLineRangeBar rangeBar) {
        if (!mOverlayList.contains(rangeBar)) {
            return;
        }
        rangeBar.switchState(STATE_ACTIVE);
        mCurrentActiveIndex = mOverlayList.indexOf(rangeBar);
    }

    public void switchRangeBarToFix() {
        if (mOverlayList == null || mOverlayList.isEmpty() || mCurrentActiveIndex == -1) {
            return;
        }
        mOverlayList.get(mCurrentActiveIndex).switchState(STATE_FIX);
    }

    /**
     * 计算 overlay 尾部 view 左面的 margin 值
     *
     * @param headView view
     * @return int 单位 sp
     */
    public int calculateHeadViewPosition(ThumbLineHandleView headView) {
        if (headView.getView() != null) {
            return (int) (mThumbLineViewSettings.getScreenWidth() / 2 - headView.getView().getMeasuredWidth() + duration2Distance(headView.getPosition()) - mCurrScroll);
        } else {
            return 0;
        }
    }

    /**
     * 时间转为尺寸
     *
     * @param position 时长
     * @return 尺寸 pixel
     */
    public int duration2Distance(long position) {
        float length = getThumbLineViewWidth() * position * 1.0f / mThumbLineViewSettings.getVideoDuration();
        return Math.round(length);
    }

    /**
     * 尺寸转为时间
     *
     * @param distance 尺寸 pixel
     * @return long duration
     */
    public long distance2Duration(float distance) {
        float length = distance / getThumbLineViewWidth() * mThumbLineViewSettings.getVideoDuration();
        return Math.round(length);
    }

    private void initView() {
        mIndicatorMargin = Utils.dip2px(getContext(), 6);
        mThumbRecyclerView = new RecyclerView(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, mIndicatorMargin, 0, mIndicatorMargin);
        mThumbRecyclerView.setLayoutParams(lp);
        mThumbRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 初始化指示器
        View indicator = new View(getContext());
        int indicatorWidth = Utils.dip2px(getContext(), 2);
        LayoutParams params = new LayoutParams(indicatorWidth, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        indicator.setLayoutParams(params);
        indicator.setBackgroundColor(getResources().getColor(R.color.colorWhite));

        addView(mThumbRecyclerView);
        addView(indicator);
    }

    private void scroll(float seekPercent) {
        float scrollBy = seekPercent * getThumbLineViewWidth() - mCurrScroll;
        mThumbRecyclerView.scrollBy((int)scrollBy, 0);
    }

    //初始化布局参数 -> 动态适配缩略图大小
    private void initLayoutParams() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = mThumbLineViewSettings.getThumbnailHeight() + 2 * mIndicatorMargin;
    }

    /**
     * 实现和recyclerView的同步滑动
     * @param dx x的位移量
     * @param dy y的位移量
     */
    private void onRecyclerViewScroll(int dx, int dy) {
        int length = mOverlayList.size();
        for (int i = 0; i < length; i++) {
            mOverlayList.get(i).requestLayout();
        }
    }

    /**
     * 实现和recyclerView的同步滑动
     */
    private void onRecyclerViewScrollStateChanged(int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                for (ThumbLineRangeBar rangeBar : mOverlayList) {
                    rangeBar.requestLayout();
                }
                break;
            default:
                break;
        }
    }
}
