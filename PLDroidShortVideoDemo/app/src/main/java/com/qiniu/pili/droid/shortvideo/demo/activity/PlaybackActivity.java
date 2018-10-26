package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnAudioFrameListener;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.qiniu.pili.droid.shortvideo.PLShortVideoUploader;
import com.qiniu.pili.droid.shortvideo.PLUploadProgressListener;
import com.qiniu.pili.droid.shortvideo.PLUploadResultListener;
import com.qiniu.pili.droid.shortvideo.PLUploadSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.MediaController;
import com.qiniu.pili.droid.shortvideo.demo.view.MediaController.OnClickSpeedAdjustListener;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaybackActivity extends Activity implements
        PLUploadResultListener,
        PLUploadProgressListener {

    private static final String TAG = "PlaybackActivity";
    private static final String MP4_PATH = "MP4_PATH";
    private static final String PREVIOUS_ORIENTATION = "PREVIOUS_ORIENTATION";

    private PLVideoTextureView mVideoView;
    private Button mUploadBtn;
    private PLShortVideoUploader mVideoUploadManager;
    private ProgressBar mProgressBarDeterminate;
    private boolean mIsUpload = false;
    private String mVideoPath;
    private int mPreviousOrientation;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playback);

        PLUploadSetting uploadSetting = new PLUploadSetting();

        mVideoUploadManager = new PLShortVideoUploader(getApplicationContext(), uploadSetting);
        mVideoUploadManager.setUploadProgressListener(this);
        mVideoUploadManager.setUploadResultListener(this);

        mUploadBtn = (Button) findViewById(R.id.upload_btn);
        mUploadBtn.setText(R.string.upload);
        mUploadBtn.setOnClickListener(new UploadOnClickListener());
        mProgressBarDeterminate = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBarDeterminate.setMax(100);
        mVideoView = (PLVideoTextureView) findViewById(R.id.video);
        mVideoPath = getIntent().getStringExtra(MP4_PATH);
        mPreviousOrientation = getIntent().getIntExtra(PREVIOUS_ORIENTATION, 1);
        mVideoView.setLooping(true);
        mVideoView.setAVOptions(new AVOptions());
        mVideoView.setVideoPath(mVideoPath);
        MediaController mediaController = new MediaController(this, true, false);
        mediaController.setOnClickSpeedAdjustListener(mOnClickSpeedAdjustListener);
        mVideoView.setMediaController(mediaController);

        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnVideoFrameListener(mOnVideoFrameListener);
        mVideoView.setOnAudioFrameListener(mOnAudioFrameListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
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
        mVideoView.stopPlayback();
    }

    public class UploadOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mIsUpload) {
                mVideoUploadManager.startUpload(mVideoPath, Config.TOKEN);
                mProgressBarDeterminate.setVisibility(View.VISIBLE);
                mUploadBtn.setText(R.string.cancel_upload);
                mIsUpload = true;
            } else {
                mVideoUploadManager.cancelUpload();
                mProgressBarDeterminate.setVisibility(View.INVISIBLE);
                mUploadBtn.setText(R.string.upload);
                mIsUpload = false;
            }
        }
    }

    @Override
    public void onUploadProgress(String fileName, double percent) {
        mProgressBarDeterminate.setProgress((int) (percent * 100));
        if (1.0 == percent) {
            mProgressBarDeterminate.setVisibility(View.INVISIBLE);
        }
    }

    public void copyToClipboard(String filePath) {
        ClipData clipData = ClipData.newPlainText("VideoFilePath", filePath);
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clipData);
    }

    @Override
    public void onUploadVideoSuccess(JSONObject response) {
        try {
            final String filePath = "http://" + Config.DOMAIN + "/" + response.getString("key");
            copyToClipboard(filePath);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.l(PlaybackActivity.this, "文件上传成功，" + filePath + "已复制到粘贴板");
                }
            });
            mUploadBtn.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUploadVideoFailed(final int statusCode, final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.l(PlaybackActivity.this, "Upload failed, statusCode = " + statusCode + " error = " + error);
            }
        });
    }

    private OnClickSpeedAdjustListener mOnClickSpeedAdjustListener = new OnClickSpeedAdjustListener() {
        @Override
        public void onClickNormal() {
            // 0x0001/0x0001 = 2
            mVideoView.setPlaySpeed(0X00010001);
        }

        @Override
        public void onClickFaster() {
            // 0x0002/0x0001 = 2
            mVideoView.setPlaySpeed(0X00020001);
        }

        @Override
        public void onClickSlower() {
            // 0x0001/0x0002 = 0.5
            mVideoView.setPlaySpeed(0X00010002);
        }
    };

    private PLOnVideoFrameListener mOnVideoFrameListener = new PLOnVideoFrameListener() {
        @Override
        public void onVideoFrameAvailable(byte[] data, int size, int width, int height, int format, long ts) {
            Log.i(TAG, "onVideoFrameAvailable: " + size + ", " + width + " x " + height + ", " + format + ", " + ts);
        }
    };

    private PLOnAudioFrameListener mOnAudioFrameListener = new PLOnAudioFrameListener() {
        @Override
        public void onAudioFrameAvailable(byte[] data, int size, int samplerate, int channels, int datawidth, long ts) {
            Log.i(TAG, "onAudioFrameAvailable: " + size + ", " + samplerate + ", " + channels + ", " + datawidth + ", " + ts);
        }
    };

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, final int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.s(PlaybackActivity.this, "first video render time: " + extra + "ms");
                        }
                    });
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    Log.i(TAG, "FPS: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation Changed: " + extra);
                    mVideoView.setDisplayOrientation(360 - extra);
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            final String errorTip;
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    Log.e(TAG, "IO Error!");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    errorTip = "failed to open player !";
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    errorTip = "failed to seek !";
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

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
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

    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            Log.i(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
        }
    };
}