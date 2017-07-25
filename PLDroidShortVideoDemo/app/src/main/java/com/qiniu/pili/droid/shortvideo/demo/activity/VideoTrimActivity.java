package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoTrimActivity extends Activity {
    private static final String TAG = "VideoTrimActivity";

    private static final int SLICE_COUNT = 8;

    private PLShortVideoTrimmer mShortVideoTrimmer;

    private LinearLayout mFrameListView;
    private View mHandlerLeft;
    private View mHandlerRight;

    private ProgressDialog mProcessingDialog;
    private VideoView mPreview;

    private long mSelectedBeginMs;
    private long mSelectedEndMs;
    private long mDurationMs;

    private int mKeyFrameCount;
    private int mSlicesTotalLength;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mProcessingDialog = new ProgressDialog(this);
        mProcessingDialog.setMessage("处理中...");
        mProcessingDialog.setCancelable(false);

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        play(mSelectedBeginMs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrackPlayProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
        }
    }

    private void startTrackPlayProgress() {
        stopTrackPlayProgress();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPreview.getCurrentPosition() >= mSelectedEndMs) {
                    mPreview.seekTo((int) mSelectedBeginMs);
                }
                mHandler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void stopTrackPlayProgress() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void play(long seekTo) {
        if (mPreview != null) {
            mPreview.seekTo((int) seekTo);
            mPreview.start();
            startTrackPlayProgress();
        }
    }

    private void init(String videoPath) {
        setContentView(R.layout.activity_trim);
        TextView duration = (TextView) findViewById(R.id.duration);
        mPreview = (VideoView) findViewById(R.id.preview);

        mShortVideoTrimmer = new PLShortVideoTrimmer(this, videoPath, Config.TRIM_FILE_PATH);

        mSelectedEndMs = mDurationMs = mShortVideoTrimmer.getSrcDurationMs();
        duration.setText("时长: " + formatTime(mDurationMs));
        Log.i(TAG, "video duration: " + mDurationMs);

        mKeyFrameCount = mShortVideoTrimmer.getKeyFrameCount();
        Log.i(TAG, "video key frame count: " + mKeyFrameCount);

        mPreview.setVideoPath(videoPath);
        mPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play(mSelectedBeginMs);
            }
        });

        initKeyFrameList();
    }

    private void initKeyFrameList() {
        mFrameListView = (LinearLayout) findViewById(R.id.key_frames);
        mHandlerLeft = findViewById(R.id.handler_left);
        mHandlerRight = findViewById(R.id.handler_right);

        mHandlerLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerLeftPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mHandlerRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerRightPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mFrameListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFrameListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int sliceEdge = mFrameListView.getWidth() / SLICE_COUNT;
                Log.i(TAG, "slice edge: " + sliceEdge);

                int keyFrameInterval = mKeyFrameCount / SLICE_COUNT;
                if (keyFrameInterval == 0) {
                    keyFrameInterval = 1;
                }
                Log.i(TAG, "key frame interval: " + keyFrameInterval);
                int keyFrameIndex = 0;

                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                for (int i = 0; i < SLICE_COUNT; ++i) {
                    PLVideoFrame frame = mShortVideoTrimmer.getKeyFrame(keyFrameIndex);

                    if (frame != null) {
                        View root = LayoutInflater.from(VideoTrimActivity.this).inflate(R.layout.frame_item, null);

                        int rotation = frame.getRotation();
                        ImageView thumbnail = (ImageView) root.findViewById(R.id.thumbnail);
                        thumbnail.setImageBitmap(frame.toBitmap());
                        thumbnail.setRotation(rotation);
                        FrameLayout.LayoutParams thumbnailLP = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();
                        if (rotation == 90 || rotation == 270) {
                            thumbnailLP.leftMargin = thumbnailLP.rightMargin = (int) px;
                        } else {
                            thumbnailLP.topMargin = thumbnailLP.bottomMargin = (int) px;
                        }
                        thumbnail.setLayoutParams(thumbnailLP);

                        LinearLayout.LayoutParams rootLP = new LinearLayout.LayoutParams(sliceEdge, sliceEdge);
                        mFrameListView.addView(root, rootLP);
                    }

                    keyFrameIndex += keyFrameInterval;
                    if (keyFrameIndex == mKeyFrameCount) {
                        break;
                    }
                }

                int usedSliceCount = mFrameListView.getChildCount();
                mSlicesTotalLength = usedSliceCount * sliceEdge;
                Log.i(TAG, "used slice count: " + usedSliceCount + " total slices length: " + mSlicesTotalLength);

                updateHandlerRightPosition(mFrameListView.getWidth());
                calculateRange();
            }
        });
    }

    private void updateHandlerLeftPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerLeft.getLayoutParams();
        if ((movedPosition + mHandlerLeft.getWidth()) > mHandlerRight.getX()) {
            lp.leftMargin = (int) (mHandlerRight.getX() - mHandlerLeft.getWidth());
        } else if (movedPosition < 0) {
            lp.leftMargin = 0;
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerLeft.setLayoutParams(lp);
    }

    private void updateHandlerRightPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerRight.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (movedPosition < (mHandlerLeft.getX() + mHandlerLeft.getWidth())) {
            lp.leftMargin = (int) (mHandlerLeft.getX() + mHandlerLeft.getWidth());
        } else if ((movedPosition + (mHandlerRight.getWidth() / 2)) > (mFrameListView.getX() + mSlicesTotalLength)) {
            lp.leftMargin = (int) ((mFrameListView.getX() + mSlicesTotalLength) - (mHandlerRight.getWidth() / 2));
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerRight.setLayoutParams(lp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                init(selectedFilepath);
            }
        } else {
            finish();
        }
    }

    private void calculateRange() {
        float beginPercent = 1.0f * (mHandlerLeft.getX() - mFrameListView.getX()) / mSlicesTotalLength;
        float endPercent = 1.0f * (mHandlerRight.getX() - mFrameListView.getX()) / mSlicesTotalLength;
        if (beginPercent < 0) {
            beginPercent = 0;
        }
        if (beginPercent > 1) {
            beginPercent = 1;
        }

        if (endPercent < 0) {
            endPercent = 0;
        }
        if (endPercent > 1) {
            endPercent = 1;
        }

        Log.i(TAG, "begin percent: " + beginPercent + " end percent: " + endPercent);

        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);

        Log.i(TAG, "new range: " + mSelectedBeginMs + "-" + mSelectedEndMs);
        updateRangeText();
        play(mSelectedBeginMs);
    }

    public void onDone(View v) {
        Log.i(TAG, "trim to file path: " + Config.TRIM_FILE_PATH + " range: " + mSelectedBeginMs + " - " + mSelectedEndMs);
        mProcessingDialog.show();
        mShortVideoTrimmer.trim(mSelectedBeginMs, mSelectedEndMs, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String path) {
                mProcessingDialog.dismiss();
                VideoEditActivity.start(VideoTrimActivity.this, path);
            }

            @Override
            public void onSaveVideoFailed(int errorCode) {
                mProcessingDialog.dismiss();
                Log.e(TAG, "trim video failed, error code: " + errorCode);
            }
        });
    }

    public void onBack(View v) {
        finish();
    }

    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    private void updateRangeText() {
        TextView range = (TextView) findViewById(R.id.range);
        range.setText("剪裁范围: " + formatTime(mSelectedBeginMs) + " - " + formatTime(mSelectedEndMs));
    }
}
