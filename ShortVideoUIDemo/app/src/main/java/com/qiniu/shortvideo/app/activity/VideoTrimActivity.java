package com.qiniu.shortvideo.app.activity;

import static com.qiniu.shortvideo.app.activity.VideoMixRecordActivity.VIDEO_PATH;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.LoadFrameTask;
import com.qiniu.shortvideo.app.utils.MediaUtils;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.shortvideo.app.view.CustomProgressDialog;
import com.qiniu.shortvideo.app.view.VideoFrameListView;
import com.qiniu.shortvideo.app.view.crop.CropVideoView;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 视频剪辑模块，包含了视频剪辑、区域剪裁以及旋转等功能
 *
 * 旋转功能由于 SDK 目前不支持预览，因此需要结合可以旋转的播放器来进行预览，最终保存时再带入角度信息
 */
public class VideoTrimActivity extends AppCompatActivity implements
        VideoFrameListView.OnVideoRangeChangedListener,
        PLOnPreparedListener,
        PLOnVideoSizeChangedListener,
        PLOnInfoListener,
        PLOnErrorListener,
        PLOnCompletionListener {
    private static final String TAG = "VideoTrimActivity";
    private static final int FRAME_COUNT = 10;

    // 用于视频的剪辑
    private PLShortVideoTrimmer mShortVideoTrimmer;
    // 用于视频的区域剪裁和旋转
    private PLShortVideoTranscoder mShortVideoTranscoder;
    private PLMediaFile mMediaFile;

    private PLVideoTextureView mPreview;
    private CropVideoView mCropVideoView;
    private VideoFrameListView mVideoFrameListView;
    private RelativeLayout mPreviewLayout;
    private TextView mStartTimeText;
    private TextView mDurationText;
    private TextView mEndTimeText;
    private CustomProgressDialog mProcessingDialog;

    private long mSelectedBeginMs;
    private long mSelectedEndMs;

    private int mPreviewLayoutWidth;
    private int mPreviewLayoutHeight;
    private int mRealVideoWidth;
    private int mRealVideoHeight;
    private int mRotation = 0;

    private Handler mHandler = new Handler();

    private LoadFrameTask mLoadFrameTask;

    private boolean mIsTrimmingVideo = false;
    private boolean mIsTranscodingVideo = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trim);

        init(getIntent().getStringExtra(VIDEO_PATH));
    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.pause();
        stopTrackPlayProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadFrameTask != null && mLoadFrameTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadFrameTask.cancel(true);
            mLoadFrameTask = null;
        }
        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
        }
        if (mMediaFile != null) {
            mMediaFile.release();
        }
        mPreview.stopPlayback();
    }

    public void onClickRotate(View v) {
        mRotation = (mRotation + 90) % 360;
        mPreview.setDisplayOrientation(mRotation == 0 ? 0 : (360 - mRotation));
        updateLayoutParams();
    }

    public void onClickTrim(View v) {
        mProcessingDialog.show();

        mIsTrimmingVideo = true;
        mShortVideoTrimmer.trim(mSelectedBeginMs, mSelectedEndMs, PLShortVideoTrimmer.TRIM_MODE.FAST, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(final String path) {
                if (path.equals(Config.TRIM_FILE_PATH)) {
                    MediaUtils.storeVideo(VideoTrimActivity.this, new File(path), Config.MIME_TYPE_VIDEO);
                }
                mIsTrimmingVideo = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCroppingVideo() || mRotation != 0) {
                            mShortVideoTranscoder = new PLShortVideoTranscoder(VideoTrimActivity.this, path, Config.TRANSCODE_FILE_PATH);

                            int dstX = 0;
                            int dstY = 0;
                            int dstWidth = mMediaFile.getVideoWidth();
                            int dstHeight = mMediaFile.getVideoHeight();
                            if (isCroppingVideo()) {
                                if (!isNormalOrientation()) {
                                    dstWidth = mMediaFile.getVideoHeight();
                                    dstHeight = mMediaFile.getVideoWidth();
                                }

                                Rect cropRect = mCropVideoView.getCropRect();

                                // 转换视频截取区域的像素尺寸到分辨率对应的尺寸
                                int x = cropRect.left * dstWidth / mRealVideoWidth;
                                int y = cropRect.top * dstHeight / mRealVideoHeight;
                                int width = cropRect.width() * dstWidth / mRealVideoWidth;
                                int height = cropRect.height() * dstHeight / mRealVideoHeight;

                                // 剪裁 + 旋转同时操作的情况下，会先进行剪裁再进行旋转，因此，剪裁的区域坐标应该换算
                                // 成 rotation = 0 的场景下的坐标，以防剪裁后的视频出现拉伸的现象
                                int videoWidth = mMediaFile.getVideoWidth();
                                int videoHeight = mMediaFile.getVideoHeight();
                                if (mMediaFile.getVideoRotation() == 90 || mMediaFile.getVideoRotation() == 270) {
                                    videoWidth = mMediaFile.getVideoHeight();
                                    videoHeight = mMediaFile.getVideoWidth();
                                }
                                if (mRotation == 0) {
                                    dstX = x;
                                    dstY = y;
                                    dstWidth = width;
                                    dstHeight = height;
                                } else if (mRotation == 90) {
                                    dstX = y;
                                    dstY = videoHeight - x - width;
                                    dstWidth = height;
                                    dstHeight = width;
                                } else if (mRotation == 180) {
                                    dstX = videoWidth - x - width;
                                    dstY = videoHeight - y - height;
                                    dstWidth = width;
                                    dstHeight = height;
                                } else if (mRotation == 270) {
                                    dstX = videoWidth - y - height;
                                    dstY = x;
                                    dstWidth = height;
                                    dstHeight = width;
                                }
                                mShortVideoTranscoder.setClipArea(dstX, dstY, dstWidth, dstHeight);
                                if (mMediaFile.getVideoRotation() == 90 || mMediaFile.getVideoRotation() == 270) {
                                    int swap = dstWidth;
                                    dstWidth = dstHeight;
                                    dstHeight = swap;
                                }
                            } else {
                                if ((mMediaFile.getVideoRotation() + mRotation) % 90 % 2 == 1) {
                                    int swap = dstWidth;
                                    dstWidth = dstHeight;
                                    dstHeight = swap;
                                }
                            }

                            mIsTranscodingVideo = true;
                            mShortVideoTranscoder.setDisplayMode(null);
                            mShortVideoTranscoder.transcode(dstWidth, dstHeight, mMediaFile.getVideoBitrate(), mRotation, new PLVideoSaveListener() {

                                @Override
                                public void onSaveVideoSuccess(final String path) {
                                    mIsTranscodingVideo = false;
                                    MediaUtils.storeVideo(VideoTrimActivity.this, new File(path), Config.MIME_TYPE_VIDEO);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProcessingDialog.dismiss();
                                            VideoEditActivity.start(VideoTrimActivity.this, path);
                                        }
                                    });
                                }

                                @Override
                                public void onSaveVideoFailed(int i) {
                                    mIsTranscodingVideo = false;
                                }

                                @Override
                                public void onSaveVideoCanceled() {
                                    mIsTranscodingVideo = false;
                                    mProcessingDialog.dismiss();
                                }

                                @Override
                                public void onProgressUpdate(final float percentage) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProcessingDialog.setProgress((int) (50 + 100 * percentage / 2));
                                        }
                                    });
                                }
                            });
                        } else {
                            mProcessingDialog.dismiss();
                            VideoEditActivity.start(VideoTrimActivity.this, path);
                        }
                    }
                });
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                mIsTrimmingVideo = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProcessingDialog.dismiss();
                        ToastUtils.toastErrorCode(VideoTrimActivity.this, errorCode);
                    }
                });
            }

            @Override
            public void onSaveVideoCanceled() {
                mIsTrimmingVideo = false;
                mProcessingDialog.dismiss();
            }

            @Override
            public void onProgressUpdate(final float percentage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProcessingDialog.setProgress((int) ((isCroppingVideo() || mRotation != 0) ? 100 * percentage / 2 : 100 * percentage));
                    }
                });
            }
        });
    }

    public void onClickBack(View v) {
        finish();
    }

    private void init(String videoPath) {
        mStartTimeText = findViewById(R.id.start_time_text);
        mEndTimeText = findViewById(R.id.end_time_text);
        mDurationText = findViewById(R.id.duration_text);
        mPreviewLayout = findViewById(R.id.preview_layout);
        mPreview = findViewById(R.id.preview);
        mCropVideoView = findViewById(R.id.crop_video_view);
        mVideoFrameListView = findViewById(R.id.video_frames_view);
        mVideoFrameListView.setOnVideoRangeChangedListener(this);
        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mIsTrimmingVideo) {
                    mShortVideoTrimmer.cancelTrim();
                }
                if (mIsTranscodingVideo) {
                    mShortVideoTranscoder.cancelTranscode();
                }
            }
        });

        mShortVideoTrimmer = new PLShortVideoTrimmer(this, videoPath, Config.TRIM_FILE_PATH);

        mMediaFile = new PLMediaFile(videoPath);

        mStartTimeText.setText(formatTime(0));
        mEndTimeText.setText(formatTime(mMediaFile.getDurationMs()));
        mDurationText.setText(formatTime(mMediaFile.getDurationMs()));

        mSelectedEndMs = mMediaFile.getDurationMs();

        mPreview.setOnPreparedListener(this);
        mPreview.setOnInfoListener(this);
        mPreview.setOnCompletionListener(this);
        mPreview.setOnVideoSizeChangedListener(this);
        mPreview.setOnErrorListener(this);

        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
        options.setInteger(AVOptions.KEY_LOG_LEVEL, 5);
        mPreview.setAVOptions(options);

        mPreview.setLooping(true);
        mPreview.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_FIT_PARENT);
        mPreview.setVideoPath(videoPath);

        mPreviewLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPreviewLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPreviewLayoutWidth = mPreviewLayout.getMeasuredWidth();
                mPreviewLayoutHeight = mPreviewLayout.getMeasuredHeight();
                updateLayoutParams();
            }
        });

        mVideoFrameListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoFrameListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                final int sliceEdge = mVideoFrameListView.getWidth() / FRAME_COUNT;

                mLoadFrameTask = new LoadFrameTask(VideoTrimActivity.this, mMediaFile, FRAME_COUNT, sliceEdge, sliceEdge, new LoadFrameTask.OnLoadFrameListener() {
                    @Override
                    public void onFrameReady(Bitmap bitmap) {
                        if (bitmap != null) {
                            mVideoFrameListView.addBitmap(bitmap);
                        }
                    }
                });
                mLoadFrameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        mVideoFrameListView.init(this, mMediaFile, FRAME_COUNT);
    }

    private void play() {
        if (mPreview != null) {
            mPreview.seekTo((int) mSelectedBeginMs);
            mPreview.start();
            startTrackPlayProgress();
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

    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    private void updateRangeText(long startTime, long endTime) {
        mStartTimeText.setText(formatTime(startTime));
        mEndTimeText.setText(formatTime(endTime));
        mDurationText.setText(formatTime(endTime - startTime));
    }

    /**
     * 初始化以及旋转时更新播放器和剪裁控件的 LayoutParams
     */
    private void updateLayoutParams() {
        float previewLayoutRatio = (float) mPreviewLayoutWidth / mPreviewLayoutHeight;
        int videoWidth = isNormalOrientation() ? mMediaFile.getVideoWidth() : mMediaFile.getVideoHeight();
        int videoHeight = isNormalOrientation() ? mMediaFile.getVideoHeight() : mMediaFile.getVideoWidth();
        float videoRatio = videoWidth * 1.0f / videoHeight;
        if (previewLayoutRatio < videoRatio) {
            mRealVideoWidth = mPreviewLayoutWidth;
            mRealVideoHeight = mRealVideoWidth * videoHeight / videoWidth;
        } else {
            mRealVideoHeight = mPreviewLayoutHeight;
            mRealVideoWidth = mRealVideoHeight * videoWidth / videoHeight;
        }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mRealVideoWidth, mRealVideoHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPreview.setLayoutParams(lp);
        mCropVideoView.setLayoutParams(lp);
    }

    private boolean isNormalOrientation() {
        return (mRotation + mMediaFile.getVideoRotation()) / 90 % 2 == 0;
    }

    private boolean isCroppingVideo() {
        Rect cropRect = mCropVideoView.getCropRect();
        return cropRect.width() != mRealVideoWidth || cropRect.height() != mRealVideoHeight;
    }

    @Override
    public void onVideoRangeChangeKeyDown() {

    }

    @Override
    public void onVideoRangeChanged(long startTime, long endTime) {
        mSelectedBeginMs = startTime;
        mSelectedEndMs = endTime;
        updateRangeText(mSelectedBeginMs, mSelectedEndMs);
        play();
    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onPrepared(int preparedTime) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {

    }

    @Override
    public boolean onError(int errorCode, Object extraData) {
        return false;
    }

    @Override
    public void onInfo(int what, int extra, Object extraData) {

    }
}
