package com.qiniu.shortvideo.app.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

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
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.model.MediaFile;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.RecordSettings;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.shortvideo.app.view.CustomProgressDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_INVALID_ARG;
import static com.qiniu.shortvideo.app.activity.VideoMixRecordActivity.VIDEO_PATH;

public class VideoPuzzleActivity extends AppCompatActivity {
    public static final String VIDEO_LIST = "videoList";
    private static final int TIMER_TICK_INTERVAL = 50;
    private static final int PLAY_MODE_TOGETHER = 1;
    private static final int PLAY_MODE_ONE_BY_ONE = 2;

    private PLShortVideoMixer mShortVideoMixer;
    private PLVideoEncodeSetting mVideoEncodeSetting;

    private CustomProgressDialog mProcessingDialog;
    private ProgressBar mProgressBar;

    private RelativeLayout mPreviewLayout;
    private FrameLayout mPlayer1Layout;
    private FrameLayout mPlayer2Layout;
    private FrameLayout mPlayer3Layout;
    private FrameLayout mPlayer4Layout;
    private PLVideoTextureView mPlayer1;
    private PLVideoTextureView mPlayer2;
    private PLVideoTextureView mPlayer3;
    private PLVideoTextureView mPlayer4;
    private ImageView mCover1;
    private ImageView mCover2;
    private ImageView mCover3;
    private ImageView mCover4;

    private LinearLayout mVolumeLayout;
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private SeekBar mSeekBar3;
    private SeekBar mSeekBar4;
    private LinearLayout mVolumeSeekBar3;
    private LinearLayout mVolumeSeekBar4;

    private Button mBtnPlayTogether;
    private Button mBtnPlayOneByOne;

    private PLVideoMixItem mVideoMixItem1;
    private PLVideoMixItem mVideoMixItem2;
    private PLVideoMixItem mVideoMixItem3;
    private PLVideoMixItem mVideoMixItem4;

    private Point mEncodeSize;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private volatile long mCurTime;
    private long mPlayTogetherDuration;
    private long mPlayOneByOneDuration;

    private volatile int mPlayMode = PLAY_MODE_TOGETHER;
    private volatile boolean mTimerPause;

    private ArrayList<MediaFile> mMediaFiles;
    private int mVideoCount;
    private volatile boolean mIsPlayer1Playing;
    private volatile boolean mIsPlayer2Playing;
    private volatile boolean mIsPlayer3Playing;
    private volatile boolean mIsPlayer4Playing;

    private volatile boolean mIsPlayer1Completed;
    private volatile boolean mIsPlayer2Completed;
    private volatile boolean mIsPlayer3Completed;
    private volatile boolean mIsPlayer4Completed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_video);

        initView();
        configVideoEncodeSetting();

        mEncodeSize = new Point(mVideoEncodeSetting.getVideoEncodingWidth(), mVideoEncodeSetting.getVideoEncodingHeight());
        initVideoMixItems();
        mPreviewLayout.post(new Runnable() {
            @Override
            public void run() {
                configPlayerViews(mPreviewLayout.getMeasuredWidth(), mPreviewLayout.getMeasuredHeight());
                initTimerTask();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimerPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsPlayer1Playing) {
            playPlayer1(false);
        }
        if (mIsPlayer2Playing) {
            playPlayer2(false);
        }
        if (mVideoCount >= 3 && mIsPlayer3Playing) {
            playPlayer3(false);
        }
        if (mVideoCount == 4 && mIsPlayer4Playing) {
            playPlayer4(false);
        }
        mTimerPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer1.stopPlayback();
        mPlayer2.stopPlayback();
        if (mPlayer3 != null) {
            mPlayer3.stopPlayback();
        }
        if (mPlayer4 != null) {
            mPlayer4.stopPlayback();
        }
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

    public void onClickPlayTogether(View v) {
        if (mBtnPlayTogether.isSelected()) {
            return;
        }
        mIsPlayer1Completed = false;
        mIsPlayer2Completed = false;
        mIsPlayer3Completed = false;
        mIsPlayer4Completed = false;

        mProgressBar.setMax((int) mPlayTogetherDuration);
        mBtnPlayOneByOne.setSelected(mBtnPlayTogether.isSelected());
        mBtnPlayTogether.setSelected(!mBtnPlayTogether.isSelected());

        if (mBtnPlayTogether.isSelected()) {
            mVideoMixItem2.setStartTimeMs(0);

            mPlayMode = PLAY_MODE_TOGETHER;
            mCurTime = 0;

            mPlayer1.seekTo(0);

            //避免重复 seekTo
            if (mIsPlayer2Playing) {
                mPlayer2.seekTo(0);
            }

            if (mVideoCount >= 3) {
                mVideoMixItem3.setStartTimeMs(0);
                if (mIsPlayer3Playing) {
                    mPlayer3.seekTo(0);
                }
            }

            if (mVideoCount >= 4) {
                mVideoMixItem4.setStartTimeMs(0);
                if (mIsPlayer4Playing) {
                    mPlayer4.seekTo(0);
                }
            }
        }
    }

    public void onClickPlayOneByOne(View v) {
        if (mBtnPlayOneByOne.isSelected()) {
            return;
        }

        mIsPlayer1Completed = false;
        mIsPlayer2Completed = false;
        mIsPlayer3Completed = false;
        mIsPlayer4Completed = false;

        mProgressBar.setMax((int) mPlayOneByOneDuration);
        mBtnPlayTogether.setSelected(mBtnPlayOneByOne.isSelected());
        mBtnPlayOneByOne.setSelected(!mBtnPlayOneByOne.isSelected());

        if (mBtnPlayOneByOne.isSelected()) {
            mVideoMixItem2.setStartTimeMs((int) mMediaFiles.get(0).getDuration());

            mPlayMode = PLAY_MODE_ONE_BY_ONE;
            mCurTime = 0;

            mPlayer1.seekTo(0);

            if (mIsPlayer2Playing) {
                playPlayer2(false);
            }
            mPlayer2.seekTo(0);
            mCover2.setVisibility(View.VISIBLE);

            if (mVideoCount >= 3) {
                mVideoMixItem3.setStartTimeMs((int) mMediaFiles.get(0).getDuration() + (int) mMediaFiles.get(1).getDuration());
                if (mIsPlayer3Playing) {
                    playPlayer3(false);
                }
                mPlayer3.seekTo(0);
                mCover3.setVisibility(View.VISIBLE);
            }

            if (mVideoCount == 4) {
                mVideoMixItem4.setStartTimeMs((int) (mPlayOneByOneDuration - mMediaFiles.get(3).getDuration()));
                if (mIsPlayer4Playing) {
                    playPlayer4(false);
                }
                mPlayer4.seekTo(0);
                mCover4.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickDone(View v) {
        mShortVideoMixer = new PLShortVideoMixer(this, Config.VIDEO_MIX_PATH,
                mPlayMode == PLAY_MODE_TOGETHER ? mPlayTogetherDuration : mPlayOneByOneDuration);
        mShortVideoMixer.setVideoEncodeSetting(mVideoEncodeSetting);

        mProcessingDialog.show();

        List<PLVideoMixItem> items = new LinkedList<>();
        items.add(mVideoMixItem1);
        items.add(mVideoMixItem2);
        if (mVideoMixItem3 != null) {
            items.add(mVideoMixItem3);
        }
        if (mVideoMixItem4 != null) {
            items.add(mVideoMixItem4);
        }

        mShortVideoMixer.mix(items, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String destFile) {
                mProcessingDialog.dismiss();
                Intent intent = new Intent(VideoPuzzleActivity.this, VideoTrimActivity.class);
                intent.putExtra(VIDEO_PATH, destFile);
                startActivity(intent);
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
                        ToastUtils.s(VideoPuzzleActivity.this, "拼接拼图失败: " + errorMsg);
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

    private void initView() {
        mVolumeLayout = findViewById(R.id.volume_layout);
        mPreviewLayout = findViewById(R.id.preview_layout);
        mPlayer1Layout = findViewById(R.id.player1_layout);
        mPlayer2Layout = findViewById(R.id.player2_layout);
        mPlayer1 = findViewById(R.id.video_player1);
        mPlayer2 = findViewById(R.id.video_player2);
        mCover1 = findViewById(R.id.cover1);
        mCover2 = findViewById(R.id.cover2);

        mSeekBar1 = findViewById(R.id.seek_bar1);
        mSeekBar2 = findViewById(R.id.seek_bar2);

        mSeekBar1.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mSeekBar2.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mBtnPlayTogether = findViewById(R.id.play_together_btn);
        mBtnPlayOneByOne = findViewById(R.id.play_one_by_one_btn);
        mBtnPlayTogether.setSelected(true);

        mPreviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVolumeLayout != null) {
                    if (mVolumeLayout.getVisibility() == View.VISIBLE) {
                        mVolumeLayout.setVisibility(View.INVISIBLE);
                    } else {
                        mVolumeLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mMediaFiles = getIntent().getParcelableArrayListExtra(VIDEO_LIST);
        mVideoCount = mMediaFiles.size();
        Iterator iterator = mMediaFiles.iterator();
        while (iterator.hasNext()) {
            MediaFile mediaFile = (MediaFile) iterator.next();
            mPlayOneByOneDuration += mediaFile.getDuration();
            mPlayTogetherDuration = Math.max(mPlayTogetherDuration, mediaFile.getDuration());
        }

        Bitmap bitmap = getFirstFrame(mMediaFiles.get(0).getPath());
        if (bitmap != null) {
            mCover1.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mCover1.setImageBitmap(bitmap);
        }
        mPlayer1.setCoverView(mCover1);
        mPlayer1.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
        mPlayer1.setAVOptions(getAVOptions());
        mPlayer1.setVideoPath(mMediaFiles.get(0).getPath());
        mPlayer1.setBufferingIndicator(new View(this));
        mPlayer1.setOnCompletionListener(new PLOnCompletionListener() {
            @Override
            public void onCompletion() {
                mIsPlayer1Completed = true;
                mIsPlayer1Playing = false;
            }
        });

        bitmap = getFirstFrame(mMediaFiles.get(1).getPath());
        if (bitmap != null) {
            mCover2.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mCover2.setImageBitmap(bitmap);
        }
        mPlayer2.setCoverView(mCover2);
        mPlayer2.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
        mPlayer2.setAVOptions(getAVOptions());
        mPlayer2.setVideoPath(mMediaFiles.get(1).getPath());
        mPlayer2.setBufferingIndicator(new View(this));
        mPlayer2.setOnCompletionListener(new PLOnCompletionListener() {
            @Override
            public void onCompletion() {
                mIsPlayer2Completed = true;
                mIsPlayer2Playing = false;
            }
        });

        if (mVideoCount >= 3) {
            mPlayer3Layout = findViewById(R.id.player3_layout);
            mVolumeSeekBar3 = findViewById(R.id.volume_seek_bar3);
            mSeekBar3 = findViewById(R.id.seek_bar3);
            mPlayer3 = findViewById(R.id.video_player3);
            mCover3 = findViewById(R.id.cover3);

            mPlayer3Layout.setVisibility(View.VISIBLE);
            mVolumeSeekBar3.setVisibility(View.VISIBLE);
            mSeekBar3.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

            bitmap = getFirstFrame(mMediaFiles.get(2).getPath());
            if (bitmap != null) {
                mCover3.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mCover3.setImageBitmap(bitmap);
            }
            mPlayer3.setCoverView(mCover3);
            mPlayer3.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
            mPlayer3.setAVOptions(getAVOptions());
            mPlayer3.setVideoPath(mMediaFiles.get(2).getPath());
            mPlayer3.setBufferingIndicator(new View(this));
            mPlayer3.setOnCompletionListener(new PLOnCompletionListener() {
                @Override
                public void onCompletion() {
                    mIsPlayer3Completed = true;
                    mIsPlayer3Playing = false;
                }
            });
        }
        if (mVideoCount == 4) {
            mPlayer4Layout = findViewById(R.id.player4_layout);
            mPlayer4 = findViewById(R.id.video_player4);
            mVolumeSeekBar4 = findViewById(R.id.volume_seek_bar4);
            mSeekBar4 = findViewById(R.id.seek_bar4);
            mCover4 = findViewById(R.id.cover4);

            mPlayer4Layout.setVisibility(View.VISIBLE);
            mVolumeSeekBar4.setVisibility(View.VISIBLE);
            mSeekBar4.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

            bitmap = getFirstFrame(mMediaFiles.get(3).getPath());
            if (bitmap != null) {
                mCover4.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mCover4.setImageBitmap(bitmap);
            }
            mPlayer4.setCoverView(mCover4);
            mPlayer4.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
            mPlayer4.setAVOptions(getAVOptions());
            mPlayer4.setVideoPath(mMediaFiles.get(3).getPath());
            mPlayer4.setBufferingIndicator(new View(this));
            mPlayer4.setOnCompletionListener(new PLOnCompletionListener() {
                @Override
                public void onCompletion() {
                    mIsPlayer4Completed = true;
                    mIsPlayer4Playing = false;
                }
            });
        }

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setMax((int) mPlayTogetherDuration);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoMixer.cancel();
            }
        });
    }

    private void configVideoEncodeSetting() {
        mVideoEncodeSetting = new PLVideoEncodeSetting(this);
        mVideoEncodeSetting.setEncodingSizeLevel(RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[ConfigActivity.ENCODING_SIZE_LEVEL_POS]);
        mVideoEncodeSetting.setEncodingBitrate(RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[ConfigActivity.ENCODING_BITRATE_LEVEL_POS]);
        mVideoEncodeSetting.setHWCodecEnabled(true);
        mVideoEncodeSetting.setConstFrameRateEnabled(true);
    }

    /**
     * 根据编码分辨率计算每个 player view 的大小和位置
     *
     * @param previewLayoutWidth 父控件宽
     * @param previewLayoutHeight 父控件高
     */
    private void configPlayerViews(int previewLayoutWidth, int previewLayoutHeight) {
        Point previewLayoutSize = new Point();
        float encodeSizeRatio = (float) mEncodeSize.y / mEncodeSize.x;
        if (encodeSizeRatio == 1) {
            previewLayoutSize.x = previewLayoutWidth;
            previewLayoutSize.y = previewLayoutWidth;
        } else if (encodeSizeRatio == (4 / 3f)) {
            previewLayoutSize.x = previewLayoutWidth;
            previewLayoutSize.y = previewLayoutWidth / 3 * 4;
        } else if (encodeSizeRatio == (16 / 9f)) {
            if (previewLayoutHeight / previewLayoutWidth >= 16 / 9) {
                previewLayoutSize.x = previewLayoutHeight / 16 * 9;
                previewLayoutSize.y = previewLayoutHeight;
            } else {
                previewLayoutSize.x = previewLayoutWidth;
                previewLayoutSize.y = previewLayoutWidth / 9 * 16;
            }
        }
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mPreviewLayout.getLayoutParams();
        layoutParams.width = previewLayoutSize.x;
        layoutParams.height = previewLayoutSize.y;
        mPreviewLayout.setLayoutParams(layoutParams);

        if (mVideoCount == 2) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mPlayer1Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y;
            mPlayer1Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer2Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y;
            lp.addRule(RelativeLayout.END_OF, mPlayer1Layout.getId());
            mPlayer2Layout.setLayoutParams(lp);
        }
        if (mVideoCount == 3) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mPlayer1Layout.getLayoutParams();
            lp.width = previewLayoutSize.x;
            lp.height = previewLayoutSize.y / 3;
            mPlayer1Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer2Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 3 * 2;
            lp.addRule(RelativeLayout.BELOW, mPlayer1Layout.getId());
            lp.addRule(RelativeLayout.ALIGN_PARENT_START);
            mPlayer2Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer3Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 3 * 2;
            lp.addRule(RelativeLayout.BELOW, mPlayer1Layout.getId());
            lp.addRule(RelativeLayout.END_OF, mPlayer2Layout.getId());
            mPlayer3Layout.setLayoutParams(lp);
        }
        if (mVideoCount == 4) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mPlayer1Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 2;
            lp.addRule(RelativeLayout.END_OF, mPlayer1Layout.getId());
            mPlayer1Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer2Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 2;
            lp.addRule(RelativeLayout.END_OF, mPlayer1Layout.getId());
            mPlayer2Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer3Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 2;
            lp.addRule(RelativeLayout.BELOW, mPlayer1Layout.getId());
            lp.addRule(RelativeLayout.ALIGN_PARENT_START);
            mPlayer3Layout.setLayoutParams(lp);

            lp = (RelativeLayout.LayoutParams) mPlayer4Layout.getLayoutParams();
            lp.width = previewLayoutSize.x / 2;
            lp.height = previewLayoutSize.y / 2;
            lp.addRule(RelativeLayout.BELOW, mPlayer2Layout.getId());
            lp.addRule(RelativeLayout.END_OF, mPlayer3Layout.getId());
            mPlayer4Layout.setLayoutParams(lp);
        }
    }

    /**
     * 初始化拼图所需的 PLVideoMixItem
     */
    private void initVideoMixItems() {
        if (mVideoCount == 2) {
            mVideoMixItem1 = new PLVideoMixItem();
            mVideoMixItem1.setVideoPath(mMediaFiles.get(0).getPath());
            mVideoMixItem1.setVideoRect(new Rect(0, 0, mEncodeSize.x / 2, mEncodeSize.y));
            mVideoMixItem1.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem1.setStartTimeMs(0);

            mVideoMixItem2 = new PLVideoMixItem();
            mVideoMixItem2.setVideoPath(mMediaFiles.get(1).getPath());
            mVideoMixItem2.setVideoRect(new Rect(mEncodeSize.x / 2, 0, mEncodeSize.x, mEncodeSize.y));
            mVideoMixItem2.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem2.setStartTimeMs(0);
        }
        if (mVideoCount == 3) {
            mVideoMixItem1 = new PLVideoMixItem();
            mVideoMixItem1.setVideoPath(mMediaFiles.get(0).getPath());
            mVideoMixItem1.setVideoRect(new Rect(0, 0, mEncodeSize.x, mEncodeSize.y / 3));
            mVideoMixItem1.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem1.setStartTimeMs(0);

            mVideoMixItem2 = new PLVideoMixItem();
            mVideoMixItem2.setVideoPath(mMediaFiles.get(1).getPath());
            mVideoMixItem2.setVideoRect(new Rect(0, mEncodeSize.y / 3, mEncodeSize.x / 2, mEncodeSize.y));
            mVideoMixItem2.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem2.setStartTimeMs(0);

            mVideoMixItem3 = new PLVideoMixItem();
            mVideoMixItem3.setVideoPath(mMediaFiles.get(2).getPath());
            mVideoMixItem3.setVideoRect(new Rect(mEncodeSize.x / 2, mEncodeSize.y / 3, mEncodeSize.x, mEncodeSize.y));
            mVideoMixItem3.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem3.setStartTimeMs(0);
        }
        if (mVideoCount == 4) {
            mVideoMixItem1 = new PLVideoMixItem();
            mVideoMixItem1.setVideoPath(mMediaFiles.get(0).getPath());
            mVideoMixItem1.setVideoRect(new Rect(0, 0, mEncodeSize.x / 2, mEncodeSize.y / 2));
            mVideoMixItem1.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem1.setStartTimeMs(0);

            mVideoMixItem2 = new PLVideoMixItem();
            mVideoMixItem2.setVideoPath(mMediaFiles.get(1).getPath());
            mVideoMixItem2.setVideoRect(new Rect(mEncodeSize.x / 2, 0, mEncodeSize.x, mEncodeSize.y / 2));
            mVideoMixItem2.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem2.setStartTimeMs(0);

            mVideoMixItem3 = new PLVideoMixItem();
            mVideoMixItem3.setVideoPath(mMediaFiles.get(2).getPath());
            mVideoMixItem3.setVideoRect(new Rect(0, mEncodeSize.y / 2, mEncodeSize.x / 2, mEncodeSize.y));
            mVideoMixItem3.setDisplayMode(PLDisplayMode.FIT);
            mVideoMixItem3.setStartTimeMs(0);

            mVideoMixItem4 = new PLVideoMixItem();
            mVideoMixItem4.setVideoPath(mMediaFiles.get(3).getPath());
            mVideoMixItem4.setVideoRect(new Rect(mEncodeSize.x / 2, mEncodeSize.y / 2, mEncodeSize.x, mEncodeSize.y));
            mVideoMixItem4.setDisplayMode(PLDisplayMode.FULL);
            mVideoMixItem4.setStartTimeMs(0);
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

                            if (mIsPlayer1Completed && mIsPlayer2Completed
                                    && (mVideoMixItem3 == null || mIsPlayer3Completed)
                                    && (mVideoMixItem4 == null || mIsPlayer4Completed)) {
                                mCurTime = 0;
                                mPlayer1.seekTo(0);
                                mIsPlayer1Completed = false;

                                if (mPlayMode == PLAY_MODE_ONE_BY_ONE) {
                                    mPlayer2.seekTo(0);
                                    mCover2.setVisibility(View.VISIBLE);
                                    mIsPlayer2Completed = false;

                                    if (mVideoCount >= 3) {
                                        mPlayer3.seekTo(0);
                                        mCover3.setVisibility(View.VISIBLE);
                                        mIsPlayer3Completed = false;
                                    }
                                    if (mVideoCount == 4) {
                                        mPlayer4.seekTo(0);
                                        mCover4.setVisibility(View.VISIBLE);
                                        mIsPlayer4Completed = false;
                                    }
                                } else {
                                    mPlayer2.seekTo(0);
                                    mIsPlayer2Completed = false;
                                    if (mVideoCount >= 3) {
                                        mPlayer3.seekTo(0);
                                        mIsPlayer3Completed = false;
                                    }
                                    if (mVideoCount == 4) {
                                        mPlayer4.seekTo(0);
                                        mIsPlayer4Completed = false;
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

    private void playPlayer1(boolean play) {
        if (play) {
            mPlayer1.start();
        } else {
            mPlayer1.pause();
        }
        mIsPlayer1Playing = play;
    }

    private void playPlayer2(boolean play) {
        if (play) {
            mPlayer2.start();
        } else {
            mPlayer2.pause();
        }
        mIsPlayer2Playing = play;
    }

    private void playPlayer3(boolean play) {
        if (play) {
            mPlayer3.start();
        } else {
            mPlayer3.pause();
        }
        mIsPlayer3Playing = play;
    }

    private void playPlayer4(boolean play) {
        if (play) {
            mPlayer4.start();
        } else {
            mPlayer4.pause();
        }
        mIsPlayer4Playing = play;
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

    /**
     * 播放器的控制
     */
    private void checkToPlay() {
        if (mPlayMode == PLAY_MODE_TOGETHER) {
            if (!mIsPlayer1Completed && !mIsPlayer1Playing) {
                playPlayer1(true);
            }
            if (!mIsPlayer2Completed && !mIsPlayer2Playing) {
                playPlayer2(true);
            }
            if (mVideoMixItem3 != null && !mIsPlayer3Completed && !mIsPlayer3Playing) {
                playPlayer3(true);
            }
            if (mVideoMixItem4 != null && !mIsPlayer4Completed && !mIsPlayer4Playing) {
                playPlayer4(true);
            }
        } else {
            if (!mIsPlayer1Completed && !mIsPlayer1Playing) {
                playPlayer1(true);
            }
            if (mCurTime > mVideoMixItem2.getStartTimeMs() && !mIsPlayer2Completed && !mIsPlayer2Playing) {
                playPlayer2(true);
            }
            if (mVideoMixItem3 != null && mCurTime > mVideoMixItem3.getStartTimeMs()
                    && !mIsPlayer3Completed && !mIsPlayer3Playing) {
                playPlayer3(true);
            }
            if (mVideoMixItem4 != null && mCurTime > mVideoMixItem4.getStartTimeMs()
                    && !mIsPlayer4Completed && !mIsPlayer4Playing) {
                playPlayer4(true);
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float volume = 1f * progress / 100;
            switch (seekBar.getId()) {
                case R.id.seek_bar1:
                    mVideoMixItem1.setVolume(volume);
                    mPlayer1.setVolume(volume, volume);
                    break;
                case R.id.seek_bar2:
                    mVideoMixItem2.setVolume(volume);
                    mPlayer2.setVolume(volume, volume);
                    break;
                case R.id.seek_bar3:
                    mVideoMixItem3.setVolume(volume);
                    mPlayer3.setVolume(volume, volume);
                    break;
                case R.id.seek_bar4:
                    mVideoMixItem4.setVolume(volume);
                    mPlayer4.setVolume(volume, volume);
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
}
