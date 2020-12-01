package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

import static com.qiniu.pili.droid.shortvideo.demo.utils.Config.VIDEO_STORAGE_DIR;

public class FrameListView extends FrameLayout {
    public static int SELECTOR_VIEW = 1;
    public static int RECT_VIEW = 2;

    private Context mContext;
    private RecyclerView mFrameList;
    private ObservableHorizontalScrollView mScrollView;
    private FrameSelectorView mCurSelectorView;
    private String mVideoPath;

    private PLMediaFile mMediaFile;
    private long mDurationMs;
    private long mShowFrameIntervalMs;

    private int mFrameWidth;
    private int mFrameHeight;
    private FrameLayout mScrollViewParent;
    private ViewGroup mFrameListParent;

    private HashMap<SectionItem, View> mSectionsMap = new HashMap<>();

    private FrameListAdapter mFrameListAdapter;
    private OnVideoFrameScrollListener mOnVideoFrameScrollListener;

    public FrameListView(@NonNull Context context) {
        super(context);
    }

    public FrameListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.frame_list_view, this);
        mFrameList = (RecyclerView) view.findViewById(R.id.recycler_frame_list);
        mScrollView = (ObservableHorizontalScrollView) view.findViewById(R.id.scroll_view);
        mScrollViewParent = (FrameLayout) findViewById(R.id.scroll_view_parent);
        mFrameListParent = (ViewGroup) findViewById(R.id.recycler_parent);
    }

    private void initFrameList() {
        mFrameListAdapter = new FrameListAdapter();
        mFrameList.setAdapter(mFrameListAdapter);
        mFrameList.setItemViewCacheSize(getShowFrameCount());
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mFrameList.setLayoutManager(layoutManager);

        mScrollView.setOnScrollListener(new OnViewScrollListener());
    }

    public void setVideoPath(String path) {
        mVideoPath = path;
        mMediaFile = new PLMediaFile(mVideoPath);
        mDurationMs = mMediaFile.getDurationMs();
        // if the duration time >= 10s, the interval time is 3s, else is 1s
        mShowFrameIntervalMs = (mDurationMs >= 1000 * 10) ? 3000 : 1000;
        // if the duration time >= 500s, the interval time is duration / 15 to avoid too much frame to show.
        mShowFrameIntervalMs = (mDurationMs >= 1000 * 500) ? mDurationMs / 15 : mShowFrameIntervalMs;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mFrameWidth = mFrameHeight = wm.getDefaultDisplay().getWidth() / 6;

        initFrameList();
    }

    private int getTotalScrollLength() {
        return getShowFrameCount() * mFrameWidth;
    }

    private int getShowFrameCount() {
        return (int) Math.ceil((float) mDurationMs / mShowFrameIntervalMs);
    }

    private int getScrollLengthByTime(long time) {
        return (int) ((float) getTotalScrollLength() * time / mDurationMs);
    }

    public void scrollToTime(long time) {
        int scrollLength = getScrollLengthByTime(time);
        mScrollView.smoothScrollTo(scrollLength, 0);
    }

    public FrameSelectorView addSelectorView() {
        mCurSelectorView = new FrameSelectorView(mContext);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mFrameHeight);
        layoutParams.leftMargin = (mScrollViewParent.getWidth() - mCurSelectorView.getWidth()) / 2;
        mCurSelectorView.setLayoutParams(layoutParams);
        mScrollViewParent.addView(mCurSelectorView, layoutParams);
        return mCurSelectorView;
    }

    private int getHalfGroupWidth() {
        return mFrameWidth * 3;
    }

    public View addSelectedRect(View view) {
        mCurSelectorView = (FrameSelectorView) view;
        if (mCurSelectorView == null) {
            return null;
        }
        int leftBorder = mCurSelectorView.getBodyLeft();
        int rightBorder = mCurSelectorView.getBodyRight();
        int width = mCurSelectorView.getBodyWidth();

        boolean outOfLeft = leftBorder <= getHalfGroupWidth() - mScrollView.getScrollX();
        boolean outOfRight = rightBorder >= getHalfGroupWidth() + (getTotalScrollLength() - mScrollView.getScrollX());

        if (outOfLeft && !outOfRight) {
            leftBorder = getHalfGroupWidth() - mScrollView.getScrollX();
            width = rightBorder - leftBorder;
        } else if (!outOfLeft && outOfRight) {
            width = width - (rightBorder - getHalfGroupWidth() - (getTotalScrollLength() - mScrollView.getScrollX()));
        } else if (outOfLeft && outOfRight) {
            leftBorder = getHalfGroupWidth() - mScrollView.getScrollX();
            width = getTotalScrollLength();
        }

        if (width <= 0) {
            mCurSelectorView.setVisibility(View.GONE);
            return null;
        }

        final View rectView = new View(mContext);
        rectView.setBackground(getResources().getDrawable(R.drawable.frame_selector_rect));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, mFrameListParent.getHeight());

        int leftPosition = leftBorder + mScrollView.getScrollX();
        int rightPosition = leftPosition + width;

        layoutParams.leftMargin = leftPosition;
        mFrameListParent.addView(rectView, layoutParams);

        mCurSelectorView.setVisibility(View.GONE);

        SectionItem item = addSection(leftPosition, rightPosition);
        rectView.setTag(item);
        mSectionsMap.put(item, rectView);

        //todo
        mCurSelectorView = null;

        return rectView;
    }

    public SectionItem getSectionByRectView(View view) {
        return (SectionItem) view.getTag();
    }

    public void showSelectorByRectView(FrameSelectorView selectorView, View rectView) {
        selectorView.setVisibility(VISIBLE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) (rectView.getLayoutParams());
        int leftPosition = (int) (rectView.getX() - mScrollView.getScrollX() - selectorView.getLeftHandlerWidth());
        selectorView.setBodyLeft(leftPosition);
        selectorView.setBodyWidth(rectView.getWidth());
    }

    public void removeRectView(View view) {
        Iterator<HashMap.Entry<SectionItem, View>> it = mSectionsMap.entrySet().iterator();

        while (it.hasNext()) {
            HashMap.Entry<SectionItem, View> entry = it.next();
            if (entry.getValue() == view) {
                View rectView = entry.getValue();
                mFrameListParent.removeView(rectView);
                it.remove();
            }
        }
    }

    public void removeSelectorView(FrameSelectorView selectorView) {
        mScrollViewParent.removeView(selectorView);
    }

    private long getTimeByPosition(int position) {
        position = position - getHalfGroupWidth();
        return (long) ((float) mDurationMs * position / getTotalScrollLength());
    }

    private SectionItem addSection(int leftPosition, int rightPosition) {
        String path = VIDEO_STORAGE_DIR + "pl-trim-" + System.currentTimeMillis() + ".mp4";
        long startTime = getTimeByPosition(leftPosition);
        long endTime = getTimeByPosition(rightPosition);

        SectionItem sectionItem = new SectionItem(startTime, endTime, path);
        return sectionItem;
    }

    private class OnViewScrollListener implements ObservableHorizontalScrollView.OnScrollListener {
        @Override
        public void onScrollChanged(ObservableHorizontalScrollView scrollView, final int x, int y, int oldX, int oldY, boolean dragScroll) {
            if (dragScroll) {
                if (mOnVideoFrameScrollListener != null) {
                    int timeMs = (int) (x * mDurationMs / (getShowFrameCount() * mFrameWidth));
                    mOnVideoFrameScrollListener.onVideoFrameScrollChanged(timeMs);
                }
            }
        }
    }

    public interface OnVideoFrameScrollListener {
        void onVideoFrameScrollChanged(long timeMs);
    }

    public void setOnVideoFrameScrollListener(OnVideoFrameScrollListener onVideoFrameScrollListener) {
        mOnVideoFrameScrollListener = onVideoFrameScrollListener;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }

    private class FrameListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View contactView = inflater.inflate(R.layout.item_devide_frame, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, final int position) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mFrameWidth, mFrameHeight);
            params.width = mFrameWidth;
            holder.mImageView.setLayoutParams(params);

            // there are 6 dark frames in begin and end sides
            if (position == 0 ||
                    position == 1 ||
                    position == 2 ||
                    position == getShowFrameCount() + 3 ||
                    position == getShowFrameCount() + 4 ||
                    position == getShowFrameCount() + 5) {
                return;
            }

            long frameTime = (position - 3) * mShowFrameIntervalMs;
            new ImageViewTask(holder.mImageView, frameTime, mFrameWidth, mFrameHeight, mMediaFile).execute();
        }

        @Override
        public int getItemCount() {
            return getShowFrameCount() + 6;
        }
    }

    private static class ImageViewTask extends AsyncTask<Void, Void, PLVideoFrame> {
        private WeakReference<ImageView> mImageViewWeakReference;
        private long mFrameTime;
        private int mFrameWidth;
        private int mFrameHeight;
        private PLMediaFile mMediaFile;

        ImageViewTask(ImageView imageView, long frameTime, int frameWidth, int frameHeight, PLMediaFile mediaFile) {
            mImageViewWeakReference = new WeakReference<>(imageView);
            mFrameTime = frameTime;
            mFrameWidth = frameWidth;
            mFrameHeight = frameHeight;
            mMediaFile = mediaFile;
        }

        @Override
        protected PLVideoFrame doInBackground(Void... v) {
            PLVideoFrame frame = mMediaFile.getVideoFrameByTime(mFrameTime, false, mFrameWidth, mFrameHeight);
            return frame;
        }

        @Override
        protected void onPostExecute(PLVideoFrame frame) {
            super.onPostExecute(frame);
            ImageView mImageView = mImageViewWeakReference.get();
            if (mImageView == null) {
                return;
            }
            if (frame != null) {
                int rotation = frame.getRotation();
                Bitmap bitmap = frame.toBitmap();
                mImageView.setImageBitmap(bitmap);
                mImageView.setRotation(rotation);
            }
        }
    }

    public class SectionItem {
        long mStartTime;
        long mEndTime;
        String mVideoPath;

        public SectionItem(long startTime, long endTime, String videoPath) {
            mStartTime = startTime;
            mEndTime = endTime;
            mVideoPath = videoPath;
        }

        public long getStartTime() {
            return mStartTime;
        }

        public long getEndTime() {
            return mEndTime;
        }
    }
}
