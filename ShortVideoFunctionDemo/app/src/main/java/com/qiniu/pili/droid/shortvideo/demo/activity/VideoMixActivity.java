package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

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
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_INVALID_ARG;

public class VideoMixActivity extends Activity {
    private static final String TAG = "VideoMixActivity";

    private static final int TIMER_TICK_INTERVAL = 50;
    private static final int PLAY_MODE_TOGETHER = 1;
    private static final int PLAY_MODE_ONE_BY_ONE = 2;

    private PLShortVideoMixer mShortVideoMixer;
    private int mVideoNum;
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private CustomProgressDialog mProcessingDialog;
    private ProgressBar mProgressBar;
    private Button mBtnPlayTogether;
    private Button mBtnPlayOneByOne;
    private TextView mTipTextView;
    private PLVideoMixItem mVideoMixItem1;
    private PLVideoMixItem mVideoMixItem2;
    private LinearLayout mPreviewParent;
    private PLMediaFile mMediaFile1;
    private PLMediaFile mMediaFile2;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private volatile long mCurTime;

    private long mPlayTogetherDuration;
    private long mPlayOneByOneDuration;

    private PLVideoTextureView mPlayer1;
    private PLVideoTextureView mPlayer2;
    private ImageView mCover1;
    private ImageView mCover2;

    private volatile int mPlayMode = PLAY_MODE_TOGETHER;
    private volatile boolean mTimerPause;
    private volatile boolean mIsPlayer2Paused;
    private boolean mIsPlayer1Completed;
    private boolean mIsPlayer2Completed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_mix);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoMixer.cancel();
            }
        });

        mPreviewParent = (LinearLayout) findViewById(R.id.previewParent);
        mSeekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mBtnPlayTogether = (Button) findViewById(R.id.btnPlayTogether);
        mBtnPlayOneByOne = (Button) findViewById(R.id.btnPlayOneByOne);
        mTipTextView = (TextView) findViewById(R.id.tipTextView);

        mPlayer1 = (PLVideoTextureView) findViewById(R.id.player1);
        mPlayer2 = (PLVideoTextureView) findViewById(R.id.player2);
        mCover1 = (ImageView) findViewById(R.id.cover1);
        mCover2 = (ImageView) findViewById(R.id.cover2);
        mPlayer1.setBufferingIndicator(new View(this));
        mPlayer2.setBufferingIndicator(new View(this));
        mPlayer1.setCoverView(mCover1);
        mPlayer2.setCoverView(mCover2);

        mPlayer1.setOnCompletionListener(new PLOnCompletionListener() {
            @Override
            public void onCompletion() {
                mIsPlayer1Completed = true;
            }
        });
        mPlayer2.setOnCompletionListener(new PLOnCompletionListener() {
            @Override
            public void onCompletion() {
                mIsPlayer2Completed = true;
            }
        });

        mSeekBar1.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mSeekBar2.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        chooseVideo();
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

    public void onPlayTogether(View view) {
        if (!isVideoReady()) {
            return;
        }
        if (mBtnPlayTogether.isSelected()) {
            return;
        }
        mProgressBar.setMax((int) mPlayTogetherDuration);

        mBtnPlayOneByOne.setSelected(mBtnPlayTogether.isSelected());
        mBtnPlayTogether.setSelected(!mBtnPlayTogether.isSelected());

        if (mBtnPlayTogether.isSelected()) {
            mVideoMixItem2.setStartTimeMs(0);

            mPlayMode = PLAY_MODE_TOGETHER;
            mCurTime = 0;

            mPlayer1.seekTo(0);
            startPlayer1();

            //避免重复 seekTo
            if (!mIsPlayer2Paused) {
                mPlayer2.seekTo(0);
            }
            startPlayer2();
        }
    }

    public void onPlayOneByOne(View view) {
        if (!isVideoReady()) {
            return;
        }
        if (mBtnPlayOneByOne.isSelected()) {
            return;
        }
        mProgressBar.setMax((int) mPlayOneByOneDuration);

        mBtnPlayTogether.setSelected(mBtnPlayOneByOne.isSelected());
        mBtnPlayOneByOne.setSelected(!mBtnPlayOneByOne.isSelected());

        if (mBtnPlayOneByOne.isSelected()) {
            mVideoMixItem2.setStartTimeMs((int) mMediaFile1.getDurationMs());

            mPlayMode = PLAY_MODE_ONE_BY_ONE;
            mCurTime = 0;

            mPlayer1.seekTo(0);
            startPlayer1();

            mPlayer2.pause();
            mIsPlayer2Paused = true;
            mPlayer2.seekTo(0);
            mCover2.setVisibility(View.VISIBLE);
        }
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
            mVideoNum = 1;
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

            mTipTextView.setVisibility(View.INVISIBLE);
            mPreviewParent.setClickable(false);
            mBtnPlayTogether.setSelected(true);

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

            initTimerTask();
        }
    }

    public void onPreview2Click(View v) {
        chooseVideo();
    }

    public void onDone(View v) {
        mShortVideoMixer = new PLShortVideoMixer(this, Config.VIDEO_MIX_PATH,
                mPlayMode == PLAY_MODE_TOGETHER ? mPlayTogetherDuration : mPlayOneByOneDuration);
        PLVideoEncodeSetting videoEncodeSetting = new PLVideoEncodeSetting(this);
        videoEncodeSetting.setEncodingSizeLevel(PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_1);
        videoEncodeSetting.setEncodingBitrate(2000 * 1000);
        videoEncodeSetting.setHWCodecEnabled(true);
        videoEncodeSetting.setConstFrameRateEnabled(true);
        mShortVideoMixer.setVideoEncodeSetting(videoEncodeSetting);

        mProcessingDialog.show();

        List<PLVideoMixItem> items = new LinkedList<>();
        items.add(mVideoMixItem1);
        items.add(mVideoMixItem2);

        mShortVideoMixer.mix(items, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String destFile) {
                mProcessingDialog.dismiss();
                PlaybackActivity.start(VideoMixActivity.this, destFile);
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProcessingDialog.dismiss();
                        String errorMsg = "" + errorCode;
                        if (errorCode == ERROR_INVALID_ARG) {
                            errorMsg = "参数错误！";
                        }
                        ToastUtils.s(VideoMixActivity.this, "拼接拼图失败: " + errorMsg);
                    }
                });
            }

            @Override
            public void onSaveVideoCanceled() {

            }

            @Override
            public void onProgressUpdate(final float percentage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProcessingDialog.setProgress((int) (100 * percentage));
                    }
                });
            }
        });
    }

    private boolean isVideoReady() {
        return mVideoMixItem1 != null && mVideoMixItem2 != null;
    }

    private void startPlayer1() {
        mPlayer1.start();
    }

    private void startPlayer2() {
        mPlayer2.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideoReady()) {
            mPlayer1.pause();
            mPlayer2.pause();
            mIsPlayer2Paused = true;
        }
        mTimerPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isVideoReady()) {
            startPlayer1();
            if (mPlayMode == PLAY_MODE_TOGETHER) {
                startPlayer2();
            }
        }
        mTimerPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    private void initTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTimerPause) {
                            mCurTime += TIMER_TICK_INTERVAL;

                            checkToPlay();

                            if (mIsPlayer1Completed && mIsPlayer2Completed) {
                                mCurTime = 0;
                                mPlayer1.seekTo(0);
                                if (mIsPlayer1Completed) {
                                    mIsPlayer1Completed = false;
                                    startPlayer1();
                                }

                                if (mPlayMode == PLAY_MODE_ONE_BY_ONE) {
                                    mPlayer2.pause();
                                    mIsPlayer2Paused = true;
                                    mPlayer2.seekTo(0);
                                    mCover2.setVisibility(View.VISIBLE);
                                } else {
                                    mPlayer2.seekTo(0);
                                    if (mIsPlayer2Completed) {
                                        mIsPlayer2Completed = false;
                                        startPlayer2();
                                    }
                                }
                            }
                            mProgressBar.setProgress((int) mCurTime);
                        }
                    }
                });
            }
        };
        mTimer = new Timer();
        // scroll fps:20
        mTimer.schedule(mTimerTask, 0, TIMER_TICK_INTERVAL);
    }

    private void checkToPlay() {
        if (mPlayMode == PLAY_MODE_ONE_BY_ONE) {
            if (mCurTime > mVideoMixItem2.getStartTimeMs() && mIsPlayer2Paused) {
                startPlayer2();
                mIsPlayer2Paused = false;
            }
        }
    }

    private void chooseVideo() {
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

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
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
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
        boolean disableLog = getIntent().getBooleanExtra("disable-log", true);
        options.setInteger(AVOptions.KEY_LOG_LEVEL, disableLog ? 5 : 0);
        return options;
    }
}
