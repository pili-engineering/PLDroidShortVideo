package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.FrameSelectorView;
import com.qiniu.pili.droid.shortvideo.demo.view.ObservableHorizontalScrollView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoFrameActivity.DATA_EXTRA_JUMP;
import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoFrameActivity.DATA_EXTRA_PATHS;
import static com.qiniu.pili.droid.shortvideo.demo.utils.Config.VIDEO_STORAGE_DIR;

public class VideoDivideActivity extends Activity {
    private static final String TAG = "VideoDivideActivity";

    private PLShortVideoTrimmer mShortVideoTrimmer;
    private PLMediaFile mMediaFile;

    private CustomProgressDialog mProcessingDialog;
    private VideoView mPreview;
    private RecyclerView mFrameList;
    private ViewGroup mFrameListParent;
    private ObservableHorizontalScrollView mScrollView;
    private FrameLayout mScrollViewParent;
    private ImageButton mPlaybackButton;
    private FrameSelectorView mCurSelectorView;

    private long mDurationMs;
    private long mShowFrameIntervalMs;
    private String mSrcVideoPath;

    private int mFrameWidth;
    private int mFrameHeight;
    private int mCurTrimNum;

    private final ArrayList<SectionItem> mSectionList = new ArrayList<>();
    private final ArrayList<String> mPathList = new ArrayList<>();

    private TimerTask mScrollTimerTask;
    private Timer mScrollTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortVideoTrimmer.cancelTrim());

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            mSrcVideoPath = GetPathFromUri.getRealPathFromURI(this, data.getData());
            Log.i(TAG, "Select file: " + mSrcVideoPath);
            if (mSrcVideoPath != null && !"".equals(mSrcVideoPath)) {
                init();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScrollTimer != null) {
            mScrollTimer.cancel();
            mScrollTimer = null;
        }
        if (mScrollTimerTask != null) {
            mScrollTimerTask.cancel();
            mScrollTimerTask = null;
        }
    }

    public void onClickBack(View view) {
        finish();
    }

    public void onClickDone(View view) {
        resetData();
        if (mSectionList.size() > 0) {
            trimOnce();
        } else {
            noTrimJump();
        }
    }

    public void onClickAdd(View view) {
        pausePlayback();
        addSelectedRect();
        addSelectorView();
    }

    public void onClickPlayback(View view) {
        if (mPreview.isPlaying()) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    private void init() {
        initView();
        initMediaInfo();
        initVideoPlayer();
        initFrameList();
        initTimerTask();
    }

    private void initView() {
        setContentView(R.layout.activity_video_divide);

        mPreview = findViewById(R.id.preview);
        mPlaybackButton = findViewById(R.id.pause_playback);
        mFrameListParent = findViewById(R.id.recycler_parent);
        mFrameList = findViewById(R.id.recycler_frame_list);
        mScrollViewParent = findViewById(R.id.scroll_view_parent);
        mScrollView = findViewById(R.id.scroll_view);

        ImageView middleLineImage = findViewById(R.id.middle_line_image);
        ViewGroup topGroup = findViewById(R.id.top_group);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mFrameWidth = mFrameHeight = wm.getDefaultDisplay().getWidth() / 6;
        middleLineImage.getLayoutParams().height = mFrameHeight;

        mScrollView.setOnScrollListener(new OnViewScrollListener());
        topGroup.setOnTouchListener(new OnTopViewTouchListener());
    }

    private void initMediaInfo() {
        mMediaFile = new PLMediaFile(mSrcVideoPath);
        mDurationMs = mMediaFile.getDurationMs();
        // if the duration time > 10s, the interval time is 3s, else is 1s
        mShowFrameIntervalMs = (mDurationMs >= 1000 * 10) ? 3000 : 1000;
        Log.i(TAG, "video duration: " + mDurationMs);
        int videoFrameCount = mMediaFile.getVideoFrameCount(false);
        Log.i(TAG, "video frame count: " + videoFrameCount);
    }

    private void initVideoPlayer() {
        mPreview.setVideoPath(mSrcVideoPath);
        mPreview.setOnCompletionListener(mediaPlayer -> play());
        makeUpVideoViewSize();
        play();
    }

    private void initTimerTask() {
        mScrollTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    final int position = mPreview.getCurrentPosition();
                    if (mPreview.isPlaying()) {
                        scrollToTime(position);
                    }
                });
            }
        };
        mScrollTimer = new Timer();
        // scroll fps:20
        mScrollTimer.schedule(mScrollTimerTask, 50, 50);
    }

    private void initFrameList() {
        FrameListAdapter frameListAdapter = new FrameListAdapter();
        mFrameList.setAdapter(frameListAdapter);
        mFrameList.setItemViewCacheSize(getShowFrameCount());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mFrameList.setLayoutManager(layoutManager);
    }

    private void makeUpVideoViewSize() {
        int viewWidth, viewHeight, videoWidth, videoHeight, displayWidth, displayHeight;
        float videoAspectRatio;
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        viewWidth = outMetrics.widthPixels;
        viewHeight = outMetrics.widthPixels;
        Log.i(TAG, "View size: " + viewWidth + " × " + viewHeight);
        if (mMediaFile.getVideoRotation() / 90 % 2 == 1) {
            videoWidth = mMediaFile.getVideoHeight();
            videoHeight = mMediaFile.getVideoWidth();
        } else {
            videoWidth = mMediaFile.getVideoWidth();
            videoHeight = mMediaFile.getVideoHeight();
        }
        videoAspectRatio = (float) videoHeight / videoWidth;
        Log.i(TAG, "Video size: " + videoWidth + " × " + videoHeight);

        // view 的视图为一个正方形，宽高比为 1
        if (1 > videoAspectRatio) {
            displayWidth = viewWidth;
            displayHeight = (int) ((float) viewWidth / videoWidth * videoHeight);
        } else {
            displayWidth = (int) ((float) viewHeight / videoHeight * videoWidth);
            displayHeight = viewHeight;
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPreview.getLayoutParams();
        layoutParams.width = displayWidth;
        layoutParams.height = displayHeight;
        mPreview.setLayoutParams(layoutParams);
    }

    private void resetData() {
        mCurTrimNum = 0;
        mPathList.clear();
    }

    private void trimOnce() {
        if (mCurTrimNum < mSectionList.size()) {
            mProcessingDialog.setMessage("正在处理第 " + (mCurTrimNum + 1) + "/" + mSectionList.size() + " 段视频 ...");
            mProcessingDialog.show();
            mProcessingDialog.setProgress(0);

            SectionItem item = mSectionList.get(mCurTrimNum);
            if (mShortVideoTrimmer != null) {
                mShortVideoTrimmer.destroy();
            }
            mShortVideoTrimmer = new PLShortVideoTrimmer(this, mSrcVideoPath, item.mVideoPath);
            mShortVideoTrimmer.trim(item.mStartTime, item.mEndTime, PLShortVideoTrimmer.TRIM_MODE.ACCURATE, mSaveListener);
            mCurTrimNum += 1;
        } else {
            mProcessingDialog.dismiss();
            jumpToActivity();
            resetData();
        }
    }

    private void noTrimJump() {
        mPathList.add(mSrcVideoPath);
        jumpToActivity();
        resetData();
    }

    private void jumpToActivity() {
        Intent intent = new Intent(this, VideoFrameActivity.class);
        intent.putStringArrayListExtra(DATA_EXTRA_PATHS, mPathList);
        boolean isJumpedTo = getIntent().getBooleanExtra(DATA_EXTRA_JUMP, false);
        if (isJumpedTo) {
            setResult(VideoFrameActivity.DIVIDE_REQUEST_CODE, intent);
        } else {
            startActivity(intent);
        }
        finish();
    }

    private void addSelectorView() {
        mCurSelectorView = new FrameSelectorView(this);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mFrameListParent.getHeight());
        mCurSelectorView.setVisibility(View.INVISIBLE);
        mScrollViewParent.addView(mCurSelectorView, layoutParams);

        mCurSelectorView.post(() -> {
            // put mCurSelectorView to the middle of the horizontal
            layoutParams.leftMargin = (mScrollViewParent.getWidth() - mCurSelectorView.getWidth()) / 2;
            mCurSelectorView.setLayoutParams(layoutParams);
            mCurSelectorView.setVisibility(View.VISIBLE);
        });
    }

    public void addSelectedRect() {
        if (mCurSelectorView == null) {
            return;
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
        } else if (outOfLeft) {
            leftBorder = getHalfGroupWidth() - mScrollView.getScrollX();
            width = getTotalScrollLength();
        }

        if (width <= 0) {
            mCurSelectorView.setVisibility(View.GONE);
            return;
        }

        final View rectView = new View(this);
        rectView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_selector_rect, null));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, mFrameListParent.getHeight());
        rectView.setOnTouchListener(new RectViewTouchListener());

        int leftPosition = leftBorder + mScrollView.getScrollX();
        int rightPosition = leftPosition + width;

        layoutParams.leftMargin = leftPosition;
        mFrameListParent.addView(rectView, layoutParams);

        mCurSelectorView.setVisibility(View.GONE);

        SectionItem item = addSection(leftPosition, rightPosition);
        rectView.setTag(item);
    }

    private SectionItem addSection(int leftPosition, int rightPosition) {
        String path = VIDEO_STORAGE_DIR + "pl-trim-" + System.currentTimeMillis() + ".mp4";
        long startTime = getTimeByPosition(leftPosition);
        long endTime = getTimeByPosition(rightPosition);
        SectionItem sectionItem = new SectionItem(startTime, endTime, path);
        mSectionList.add(sectionItem);
        return sectionItem;
    }

    private long getTimeByPosition(int position) {
        position = position - getHalfGroupWidth();
        return (long) ((float) mDurationMs * position / getTotalScrollLength());
    }

    private int getTotalScrollLength() {
        return getShowFrameCount() * mFrameWidth;
    }

    private int getHalfGroupWidth() {
        return mFrameWidth * 3;
    }

    private int getScrollLengthByTime(long time) {
        return (int) ((float) getTotalScrollLength() * time / mDurationMs);
    }

    private void scrollToTime(long time) {
        int scrollLength = getScrollLengthByTime(time);
        mScrollView.smoothScrollTo(scrollLength, 0);
    }

    private int getShowFrameCount() {
        return (int) Math.ceil((float) mDurationMs / mShowFrameIntervalMs);
    }

    private void startPlayback() {
        mPreview.start();
        mPlaybackButton.setImageResource(R.drawable.btn_pause);
    }

    private void pausePlayback() {
        mPreview.pause();
        mPlaybackButton.setImageResource(R.drawable.btn_play);
    }

    private boolean isInRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getX() < x) && !(ev.getX() > (x + view.getWidth()))
                && !(ev.getY() < y) && !(ev.getY() > (y + view.getHeight()));
    }

    private void play() {
        if (mPreview != null) {
            mPreview.seekTo(0);
            startPlayback();
        }
    }

    private final PLVideoSaveListener mSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(String destFile) {
            MediaStoreUtils.storeVideo(VideoDivideActivity.this, new File(destFile), "video/mp4");
            mPathList.add(destFile);
            runOnUiThread(() -> trimOnce());
        }

        @Override
        public void onSaveVideoFailed(int errorCode) {
        }

        @Override
        public void onSaveVideoCanceled() {
        }

        @Override
        public void onProgressUpdate(final float percentage) {
            runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
        }
    };

    private class RectViewTouchListener implements View.OnTouchListener {
        private View mRectView;
        private final GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                SectionItem item = (SectionItem) mRectView.getTag();
                mSectionList.remove(item);
                mFrameListParent.removeView(mRectView);
                return true;
            }
        };
        private final GestureDetector mGestureDetector = new GestureDetector(VideoDivideActivity.this, mSimpleOnGestureListener);

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mRectView = view;
            if (mGestureDetector.onTouchEvent(event)) {
                return true;
            }
            return true;
        }
    }

    private class OnTopViewTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isInRangeOfView(mScrollViewParent, event)) {
                if (mCurSelectorView != null) {
                    addSelectedRect();
                    mCurSelectorView = null;
                }
            }
            return true;
        }
    }

    private class OnViewScrollListener implements ObservableHorizontalScrollView.OnScrollListener {
        @Override
        public void onScrollChanged(ObservableHorizontalScrollView scrollView, final int x, int y, int oldX, int oldY, boolean dragScroll) {
            if (dragScroll) {
                runOnUiThread(() -> {
                    if (mPreview.isPlaying()) {
                        pausePlayback();
                    }
                    int index = x / mFrameWidth;
                    mPreview.seekTo((int) (index * mShowFrameIntervalMs));
                });
            }
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.thumbnail);
        }
    }

    private class FrameListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View contactView = inflater.inflate(R.layout.item_devide_frame, parent, false);
            return new ItemViewHolder(contactView);
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
        private final WeakReference<ImageView> mImageViewWeakReference;
        private final long mFrameTime;
        private final int mFrameWidth;
        private final int mFrameHeight;
        private final PLMediaFile mMediaFile;

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

    private static class SectionItem {
        long mStartTime;
        long mEndTime;
        String mVideoPath;

        public SectionItem(long startTime, long endTime, String videoPath) {
            mStartTime = startTime;
            mEndTime = endTime;
            mVideoPath = videoPath;
        }
    }
}
