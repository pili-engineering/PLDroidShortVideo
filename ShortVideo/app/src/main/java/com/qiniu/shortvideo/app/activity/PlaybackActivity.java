package com.qiniu.shortvideo.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.ToastUtils;

import java.io.IOException;

public class PlaybackActivity extends Activity implements
        MediaController.MediaPlayerControl {

    private static final String TAG = "PlaybackActivity";
    private static final String MP4_PATH = "MP4_PATH";
    private static final String PREVIOUS_ORIENTATION = "PREVIOUS_ORIENTATION";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;
    private MediaController mMediaController;

    private String mVideoPath;
    private int mPreviousOrientation;
    private int mSeekingPosition = 0;

    public static void start(Activity activity, String mp4Path) {
        Intent intent = new Intent(activity, PlaybackActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        activity.startActivity(intent);
    }

    public static void start(Activity activity, String mp4Path, int previousOrientation) {
        Intent intent = new Intent(activity, PlaybackActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        intent.putExtra(PREVIOUS_ORIENTATION, previousOrientation);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        mVideoPath = getIntent().getStringExtra(MP4_PATH);
        mPreviousOrientation = getIntent().getIntExtra(PREVIOUS_ORIENTATION, 1);

        mMediaPlayer = new MediaPlayer();
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
        } else {
            Log.e(TAG, "creating MediaPlayer instance failed, exit.");
            return;
        }

        mSurfaceView = findViewById(R.id.video);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMediaController.isShowing()) {
                    mMediaController.show(0);
                } else {
                    mMediaController.hide();
                }
            }
        });
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                if (!"".equals(mVideoPath) && !mMediaPlayer.isPlaying()) {
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.setDataSource(mVideoPath);
                        mMediaPlayer.prepare();
                        mMediaPlayer.seekTo(mSeekingPosition);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                makeUpVideoPlayingSize();
                mMediaPlayer.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMediaPlayer.isPlaying()) {
                    mSeekingPosition = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.stop();
                }
            }
        });

        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayer(this);
        mMediaController.setAnchorView(mSurfaceView);
    }

    private void makeUpVideoPlayingSize() {
        int screenWidth, screenHeight, videoWidth, videoHeight, displayWidth, displayHeight;
        float screenAspectRatio, videoAspectRatio;
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        screenAspectRatio = (float)screenHeight / screenWidth;
        Log.i(TAG, "Screen size: " + screenWidth + " × " + screenHeight);
        videoWidth = mMediaPlayer.getVideoWidth();
        videoHeight = mMediaPlayer.getVideoHeight();
        videoAspectRatio = (float)videoHeight / videoWidth;
        Log.i(TAG, "Video size: " + screenWidth + " × " + screenHeight);

        if (screenAspectRatio > videoAspectRatio) {
            displayWidth = screenWidth;
            displayHeight = (int) ((float)screenWidth / videoWidth * videoHeight);
        } else {
            displayWidth = (int) ((float)screenHeight / videoHeight * videoWidth);
            displayHeight = screenHeight;
        }

        mSurfaceHolder.setFixedSize(displayWidth, displayHeight);
    }

    @Override
    public void finish() {
        if (0 == mPreviousOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_UNKNOWN:
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.i(TAG, "video rendering start, ts = " + extra);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.i(TAG, "onInfo: MediaPlayer.MEDIA_INFO_BUFFERING_START");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.i(TAG, "onInfo: MEDIA_INFO_BUFFERING_END");
                    break;
                case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.i(TAG, "onInfo: MEDIA_INFO_BAD_INTERLEAVING");
                    break;
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.i(TAG, "onInfo: MEDIA_INFO_NOT_SEEKABLE");
                    break;
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.i(TAG, "onInfo: MediaPlayer.MEDIA_INFO_METADATA_UPDATE");
                    break;
                case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.i(TAG, "onInfo: MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE");
                    break;
                case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    Log.i(TAG, "onInfo: MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT ");
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "Error happened, errorCode = " + extra);
            final String errorTip;
            switch (extra) {
                case MediaPlayer.MEDIA_ERROR_IO:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    Log.e(TAG, "IO Error!");
                    return false;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    errorTip = "Malformed bitstream!";
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    errorTip = "Unsupported bitstream!";
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    errorTip = "Timeout!";
                    break;
                default:
                    errorTip = "unknown error !";
                    break;
            }
            if (errorTip != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.s(PlaybackActivity.this, errorTip);
                    }
                });
            }

            finish();
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "Play Completed !");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.s(PlaybackActivity.this, "Play Completed !");
                }
            });
            finish();
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.i(TAG, "onBufferingUpdate: " + percent);
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
        }
    };
}
