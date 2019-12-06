package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.VideoFrameAdapter;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;

/**
 * 剪辑模块缩略图显示控件
 */
public class VideoFrameListView extends RelativeLayout implements RangeSlider.OnRangeChangeListener {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private RangeSlider mRangeSlider;
    private VideoFrameAdapter mVideoFrameAdapter;

    private long mVideoDuration;
    private long mRangeStartTime;
    private long mRangeEndTime;
    private long mVideoStartTime;
    private long mVideoEndTime;
    private long mStartTime;

    private int mAllThumbnailWidth;

    private float mCurrentScroll;

    private OnVideoRangeChangedListener mOnVideoRangeChangedListener;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    onTimeChanged();
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    if (mOnVideoRangeChangedListener != null) {
                        mOnVideoRangeChangedListener.onVideoRangeChangeKeyDown();
                    }
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:

                    break;
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            mCurrentScroll = mCurrentScroll + dx;
            float rate = mCurrentScroll / mAllThumbnailWidth;
            mStartTime = (int) (rate * mVideoDuration);
        }
    };

    public interface OnVideoRangeChangedListener {
        void onVideoRangeChangeKeyDown();

        /**
         * @param startTime
         * @param endTime
         */
        void onVideoRangeChanged(long startTime, long endTime);
    }

    public VideoFrameListView(Context context) {
        super(context);
    }

    public VideoFrameListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context, PLMediaFile mediaFile, int frameCount) {
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_video_trim, this, true);

        mRangeSlider = findViewById(R.id.range_slider);
        mRangeSlider.setRangeChangeListener(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mVideoFrameAdapter = new VideoFrameAdapter();
        mRecyclerView.setAdapter(mVideoFrameAdapter);

        mVideoDuration = mediaFile.getDurationMs();
        mRangeStartTime = 0;
        mRangeEndTime = mVideoDuration;
        mVideoStartTime = 0;
        mVideoEndTime = mVideoDuration;
        mStartTime = 0;

        setLayoutParams();

        mVideoFrameAdapter.setFrameWidth(mAllThumbnailWidth / (frameCount + 1));
    }

    public void setOnVideoRangeChangedListener(OnVideoRangeChangedListener listener) {
        mOnVideoRangeChangedListener = listener;
    }

    public void addBitmap(Bitmap bitmap) {
        mVideoFrameAdapter.add(bitmap);
    }

    public long getVideoStartTime() {
        return mVideoStartTime;
    }

    public long getVideoEndTime() {
        return mVideoEndTime;
    }

    private void setLayoutParams() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        layoutParams.width = screenWidth - 2 * resources.getDimensionPixelOffset(R.dimen.video_frame_list_margin);
        setLayoutParams(layoutParams);
        mAllThumbnailWidth = layoutParams.width;
    }

    private void onTimeChanged() {
        mVideoStartTime = mStartTime + mRangeStartTime;
        mVideoEndTime = mStartTime + mRangeEndTime;

        if (mOnVideoRangeChangedListener != null) {
            mOnVideoRangeChangedListener.onVideoRangeChanged(mVideoStartTime, mVideoEndTime);
        }
    }

    @Override
    public void onKeyDown(int type) {

    }

    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        mRangeStartTime = (int) (mVideoDuration * leftPinIndex / 100); //ms
        mRangeEndTime = (int) (mVideoDuration * rightPinIndex / 100);

        onTimeChanged();
    }
}
