package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.widget.PLVideoView;
import com.qiniu.pili.droid.shortvideo.PLDisplayMode;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoMixer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoMixItem;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_INVALID_ARG;

public class VideoMixActivity extends Activity {
    private static final String TAG = "VideoMixActivity";

    private static final int UPDATE_PROGRESS = 10000;
    private static final int PLAY_MODE_TOGETHER = 1;
    private static final int PLAY_MODE_ONE_BY_ONE = 2;

    private int mVideoNum;
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private CustomProgressDialog mProcessingDialog;
    private ProgressBar mProgressBar;
    private Button mBtnPlayTogether;
    private Button mBtnPlayOneByOne;
    private TextView mTipTextView;

    private PLShortVideoMixer mShortVideoMixer;
    private PLVideoMixItem mVideoMixItem1;
    private PLVideoMixItem mVideoMixItem2;
    private PLMediaFile mMediaFile1;
    private PLMediaFile mMediaFile2;

    private long mPlayTogetherDuration;
    private long mPlayOneByOneDuration;

    private PLVideoTextureView mPlayer1;
    private PLVideoTextureView mPlayer2;
    private ImageView mCover1;
    private ImageView mCover2;

    private volatile int mCurrentPlayMode = PLAY_MODE_TOGETHER;
    private boolean mPlayer1Completed;
    private boolean mPlayer2Completed;
    private boolean mPlayer1Playing;
    private boolean mPlayer2Playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_mix);
        initViews();
        chooseVideo();
    }

    private void initViews() {
        mSeekBar1 = findViewById(R.id.seekBar1);
        mSeekBar1.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mSeekBar2 = findViewById(R.id.seekBar2);
        mSeekBar2.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mTipTextView = findViewById(R.id.tipTextView);
        mTipTextView.setOnClickListener((v) -> chooseVideo());
        mCover1 = findViewById(R.id.cover1);
        mCover2 = findViewById(R.id.cover2);

        mPlayer1 = findViewById(R.id.player1);
        mPlayer1.setBufferingIndicator(new View(this));
        mPlayer1.setCoverView(mCover1);
        mPlayer1.setOnCompletionListener(mPlayer1CompletionListener);
        mPlayer2 = findViewById(R.id.player2);
        mPlayer2.setBufferingIndicator(new View(this));
        mPlayer2.setCoverView(mCover2);
        mPlayer2.setOnCompletionListener(mPlayer2CompletionListener);

        mProgressBar = findViewById(R.id.progressBar);
        mBtnPlayTogether = findViewById(R.id.btnPlayTogether);
        mBtnPlayTogether.setSelected(true);
        mBtnPlayTogether.setOnClickListener((v) -> playTogether());
        mBtnPlayOneByOne = findViewById(R.id.btnPlayOneByOne);
        mBtnPlayOneByOne.setOnClickListener((v) -> playOneByOne());

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortVideoMixer.cancel());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                addVideo(selectedFilepath);
            }
        } else {
            finish();
        }
    }

    public void onBack(View v) {
        finish();
    }

    public void playTogether() {
        if (!isVideoReady()) {
            return;
        }
        if (mBtnPlayTogether.isSelected()) {
            return;
        }

        mBtnPlayTogether.setSelected(true);
        mBtnPlayOneByOne.setSelected(false);
        mProgressBar.setMax((int) mPlayTogetherDuration);

        mVideoMixItem2.setStartTimeMs(0);
        mCurrentPlayMode = PLAY_MODE_TOGETHER;

        mPlayer1.seekTo(0);
        mPlayer1.start();

        mPlayer2.seekTo(0);
        mPlayer2.start();
        mCover2.setVisibility(View.GONE);
    }

    public void playOneByOne() {
        if (!isVideoReady()) {
            return;
        }
        if (mBtnPlayOneByOne.isSelected()) {
            return;
        }

        mBtnPlayTogether.setSelected(false);
        mBtnPlayOneByOne.setSelected(true);
        mProgressBar.setMax((int) mPlayOneByOneDuration);

        mVideoMixItem2.setStartTimeMs((int) mMediaFile1.getDurationMs());
        mCurrentPlayMode = PLAY_MODE_ONE_BY_ONE;

        mPlayer1.seekTo(0);
        mPlayer1.start();

        mPlayer2.pause();
        mPlayer2.seekTo(0);
        mCover2.setVisibility(View.VISIBLE);
    }

    private Bitmap getFirstFrame(String path) {
        PLMediaFile mediaFile = new PLMediaFile(path);
        PLVideoFrame videoFrame = mediaFile.getVideoFrameByTime(0, true);
        mediaFile.release();
        if (videoFrame != null) {
            return videoFrame.toBitmap();
        }
        return null;
    }

    private void addVideo(String path) {
        if (mVideoNum == 0) {
            mVideoNum++;
            mVideoMixItem1 = new PLVideoMixItem();
            mVideoMixItem1.setVideoPath(path);
            mVideoMixItem1.setVideoRect(new Rect(0, 0, 480, 240));
            mVideoMixItem1.setDisplayMode(PLDisplayMode.FIT);
            mVideoMixItem1.setStartTimeMs(0);

            mMediaFile1 = new PLMediaFile(path);

            Bitmap bitmap = getFirstFrame(path);
            if (bitmap != null) {
                mCover1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mCover1.setImageBitmap(bitmap);
            }
            mPlayer1.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT);
            mPlayer1.setAVOptions(getAVOptions());
            mPlayer1.setVideoPath(path);
        } else {
            mVideoMixItem2 = new PLVideoMixItem();
            mVideoMixItem2.setVideoPath(path);
            mVideoMixItem2.setVideoRect(new Rect(0, 240, 480, 480));
            mVideoMixItem2.setDisplayMode(PLDisplayMode.FIT);
            mVideoMixItem2.setStartTimeMs(0);

            mMediaFile2 = new PLMediaFile(path);

            mTipTextView.setVisibility(View.GONE);

            Bitmap bitmap = getFirstFrame(path);
            if (bitmap != null) {
                mCover2.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mCover2.setImageBitmap(bitmap);
            }
            mPlayer2.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT);
            mPlayer2.setAVOptions(getAVOptions());
            mPlayer2.setVideoPath(path);

            mPlayOneByOneDuration = mMediaFile1.getDurationMs() + mMediaFile2.getDurationMs();
            mPlayTogetherDuration = Math.max(mMediaFile1.getDurationMs(), mMediaFile2.getDurationMs());
            mProgressBar.setMax((int) mPlayTogetherDuration);

            // initTimerTask();
            mPlayer2.start();
            mPlayer1.start();
            mUpdateBarHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
        }
    }

    public void onDone(View v) {
        mShortVideoMixer = new PLShortVideoMixer(this, Config.VIDEO_MIX_PATH, mCurrentPlayMode == PLAY_MODE_TOGETHER ? mPlayTogetherDuration : mPlayOneByOneDuration);
        PLVideoEncodeSetting videoEncodeSetting = new PLVideoEncodeSetting(this);
        videoEncodeSetting.setEncodingSizeLevel(PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_1);
        videoEncodeSetting.setEncodingBitrate(2000 * 1000);
        videoEncodeSetting.setHWCodecEnabled(true);
        videoEncodeSetting.setConstFrameRateEnabled(true);
        mShortVideoMixer.setVideoEncodeSetting(videoEncodeSetting);

        mProcessingDialog.show();
        mProcessingDialog.setProgress(0);

        List<PLVideoMixItem> items = new LinkedList<>();
        items.add(mVideoMixItem1);
        items.add(mVideoMixItem2);

        mShortVideoMixer.mix(items, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String destFile) {
                MediaStoreUtils.storeVideo(VideoMixActivity.this, new File(destFile), "video/mp4");
                mProcessingDialog.dismiss();
                PlaybackActivity.start(VideoMixActivity.this, destFile);
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                runOnUiThread(() -> {
                    mProcessingDialog.dismiss();
                    String errorMsg = "" + errorCode;
                    if (errorCode == ERROR_INVALID_ARG) {
                        errorMsg = "参数错误！";
                    }
                    ToastUtils.showShortToast("拼接拼图失败: " + errorMsg);
                });
            }

            @Override
            public void onSaveVideoCanceled() {

            }

            @Override
            public void onProgressUpdate(final float percentage) {
                runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
            }
        });
    }

    private boolean isVideoReady() {
        return mVideoMixItem1 != null && mVideoMixItem2 != null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideoReady()) {
            mPlayer1Playing = mPlayer1.isPlaying();
            mPlayer1.pause();
            mPlayer2Playing = mPlayer2.isPlaying();
            mPlayer2.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isVideoReady()) {
            if (mPlayer1Playing) {
                mPlayer1.start();
            }
            if (mPlayer2Playing) {
                mPlayer2.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateBarHandler.removeCallbacksAndMessages(null);
        mPlayer1.stopPlayback();
        mPlayer2.stopPlayback();
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
    }

    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!isVideoReady()) {
                return;
            }
            float volume = 1f * progress / 100;
            switch (seekBar.getId()) {
                case R.id.seekBar1:
                    mVideoMixItem1.setVolume(volume);
                    mPlayer1.setVolume(volume, volume);
                    break;
                case R.id.seekBar2:
                    mVideoMixItem2.setVolume(volume);
                    mPlayer2.setVolume(volume, volume);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private AVOptions getAVOptions() {
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 0);
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO);
        boolean disableLog = getIntent().getBooleanExtra("disable-log", true);
        options.setInteger(AVOptions.KEY_LOG_LEVEL, disableLog ? 5 : 0);
        return options;
    }

    private PLOnCompletionListener mPlayer1CompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            mPlayer1Completed = true;
            if (mCurrentPlayMode == PLAY_MODE_ONE_BY_ONE) {
                mCover2.setVisibility(View.GONE);
                mPlayer2.seekTo(0);
                mPlayer2.start();
                mPlayer1Completed = false;
            } else if (mCurrentPlayMode == PLAY_MODE_TOGETHER) {
                if (mPlayer2Completed) {
                    mPlayer1.seekTo(0);
                    mPlayer1.start();
                    mPlayer1Completed = false;

                    mPlayer2.seekTo(0);
                    mPlayer2.start();
                    mPlayer2Completed = false;
                }
            }
        }
    };

    private PLOnCompletionListener mPlayer2CompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            mPlayer2Completed = true;
            if (mCurrentPlayMode == PLAY_MODE_ONE_BY_ONE) {
                mCover2.setVisibility(View.VISIBLE);
                mPlayer1.seekTo(0);
                mPlayer1.start();
                mPlayer2Completed = false;
            } else if (mCurrentPlayMode == PLAY_MODE_TOGETHER) {
                if (mPlayer1Completed) {
                    mPlayer1.seekTo(0);
                    mPlayer1.start();
                    mPlayer1Completed = false;

                    mPlayer2.seekTo(0);
                    mPlayer2.start();
                    mPlayer2Completed = false;
                }
            }
        }
    };

    private Handler mUpdateBarHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    if (mCurrentPlayMode == PLAY_MODE_ONE_BY_ONE) {
                        long progress = mPlayer1.getCurrentPosition() + mPlayer2.getCurrentPosition();
                        mProgressBar.setProgress((int) progress);
                    } else if (mCurrentPlayMode == PLAY_MODE_TOGETHER) {
                        long progress = Math.max(mPlayer1.getCurrentPosition(), mPlayer2.getCurrentPosition());
                        mProgressBar.setProgress((int) progress);
                    }
                    mUpdateBarHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                    break;
                default:
                    break;
            }
        }
    };
}
