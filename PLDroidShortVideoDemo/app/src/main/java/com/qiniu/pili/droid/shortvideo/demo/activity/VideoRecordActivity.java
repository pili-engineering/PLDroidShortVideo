package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLCaptureFrameListener;
import com.qiniu.pili.droid.shortvideo.PLDraft;
import com.qiniu.pili.droid.shortvideo.PLDraftBox;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLFocusListener;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.FocusIndicator;
import com.qiniu.pili.droid.shortvideo.demo.view.SectionProgressBar;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter.ConfigViewSeekBar;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter.FilterCellView;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter.FilterConfigSeekbar;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter.FilterConfigView;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter.FilterListView;
import com.qiniu.pili.droid.shortvideo.demo.view.tusdk.sticker.StickerListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lasque.tusdk.api.video.preproc.filter.TuSDKFilterEngine;
import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.seles.SelesParameters;
import org.lasque.tusdk.core.seles.tusdk.FilterWrap;
import org.lasque.tusdk.core.struct.TuSdkSize;
import org.lasque.tusdk.core.utils.ThreadHelper;
import org.lasque.tusdk.core.utils.hardware.CameraConfigs;
import org.lasque.tusdk.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdk.core.utils.image.ImageOrientation;
import org.lasque.tusdk.core.utils.json.JsonHelper;
import org.lasque.tusdk.core.view.recyclerview.TuSdkTableView;
import org.lasque.tusdk.core.view.widget.button.TuSdkTextButton;
import org.lasque.tusdk.impl.view.widget.TuSeekBar;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings.RECORD_SPEED_ARRAY;

public class VideoRecordActivity extends Activity implements PLRecordStateListener, PLVideoSaveListener, PLFocusListener {
    private static final String TAG = "VideoRecordActivity";

    public static final String PREVIEW_SIZE_RATIO = "PreviewSizeRatio";
    public static final String PREVIEW_SIZE_LEVEL = "PreviewSizeLevel";
    public static final String ENCODING_MODE = "EncodingMode";
    public static final String ENCODING_SIZE_LEVEL = "EncodingSizeLevel";
    public static final String ENCODING_BITRATE_LEVEL = "EncodingBitrateLevel";
    public static final String AUDIO_CHANNEL_NUM = "AudioChannelNum";
    public static final String DRAFT = "draft";

    /**
     * NOTICE: TUSDK needs extra cost
     */
    private static final boolean USE_TUSDK = true;

    private PLShortVideoRecorder mShortVideoRecorder;

    private SectionProgressBar mSectionProgressBar;
    private CustomProgressDialog mProcessingDialog;
    private View mRecordBtn;
    private View mDeleteBtn;
    private View mConcatBtn;
    private View mSwitchCameraBtn;
    private View mSwitchFlashBtn;
    private FocusIndicator mFocusIndicator;
    private SeekBar mAdjustBrightnessSeekBar;

    private boolean mFlashEnabled;
    private boolean mIsEditVideo = false;

    private GestureDetector mGestureDetector;

    private PLCameraSetting mCameraSetting;
    private PLMicrophoneSetting mMicrophoneSetting;
    private PLRecordSetting mRecordSetting;
    private PLVideoEncodeSetting mVideoEncodeSetting;
    private PLAudioEncodeSetting mAudioEncodeSetting;
    private PLFaceBeautySetting mFaceBeautySetting;
    private ViewGroup mBottomControlPanel;

    private int mFocusIndicatorX;
    private int mFocusIndicatorY;

    private double mRecordSpeed;
    private TextView mSpeedTextView;

    private Stack<Long> mDurationRecordStack = new Stack();

    private OrientationEventListener mOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record);

        initTuSDK();

        mSectionProgressBar = (SectionProgressBar) findViewById(R.id.record_progressbar);
        GLSurfaceView preview = (GLSurfaceView) findViewById(R.id.preview);
        mRecordBtn = findViewById(R.id.record);
        mDeleteBtn = findViewById(R.id.delete);
        mConcatBtn = findViewById(R.id.concat);
        mSwitchCameraBtn = findViewById(R.id.switch_camera);
        mSwitchFlashBtn = findViewById(R.id.switch_flash);
        mFocusIndicator = (FocusIndicator) findViewById(R.id.focus_indicator);
        mAdjustBrightnessSeekBar = (SeekBar) findViewById(R.id.adjust_brightness);
        mBottomControlPanel = (ViewGroup) findViewById(R.id.bottom_control_panel);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoRecorder.cancelConcat();
            }
        });

        mShortVideoRecorder = new PLShortVideoRecorder();
        mShortVideoRecorder.setRecordStateListener(this);
        mShortVideoRecorder.setFocusListener(this);

        mRecordSpeed = RECORD_SPEED_ARRAY[2];
        mSpeedTextView = (TextView) findViewById(R.id.normal_speed_text);

        String draftTag = getIntent().getStringExtra(DRAFT);
        if (draftTag == null) {
            int previewSizeRatioPos = getIntent().getIntExtra(PREVIEW_SIZE_RATIO, 0);
            int previewSizeLevelPos = getIntent().getIntExtra(PREVIEW_SIZE_LEVEL, 0);
            int encodingModePos = getIntent().getIntExtra(ENCODING_MODE, 0);
            int encodingSizeLevelPos = getIntent().getIntExtra(ENCODING_SIZE_LEVEL, 0);
            int encodingBitrateLevelPos = getIntent().getIntExtra(ENCODING_BITRATE_LEVEL, 0);
            int audioChannelNumPos = getIntent().getIntExtra(AUDIO_CHANNEL_NUM, 0);

            mCameraSetting = new PLCameraSetting();
            PLCameraSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
            mCameraSetting.setCameraId(facingId);
            mCameraSetting.setCameraPreviewSizeRatio(RecordSettings.PREVIEW_SIZE_RATIO_ARRAY[previewSizeRatioPos]);
            mCameraSetting.setCameraPreviewSizeLevel(RecordSettings.PREVIEW_SIZE_LEVEL_ARRAY[previewSizeLevelPos]);

            mMicrophoneSetting = new PLMicrophoneSetting();
            mMicrophoneSetting.setChannelConfig(RecordSettings.AUDIO_CHANNEL_NUM_ARRAY[audioChannelNumPos] == 1 ?
                    AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);

            mVideoEncodeSetting = new PLVideoEncodeSetting(this);
            mVideoEncodeSetting.setEncodingSizeLevel(RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[encodingSizeLevelPos]);
            mVideoEncodeSetting.setEncodingBitrate(RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[encodingBitrateLevelPos]);
            mVideoEncodeSetting.setHWCodecEnabled(encodingModePos == 0);

            mAudioEncodeSetting = new PLAudioEncodeSetting();
            mAudioEncodeSetting.setHWCodecEnabled(encodingModePos == 0);
            mAudioEncodeSetting.setChannels(RecordSettings.AUDIO_CHANNEL_NUM_ARRAY[audioChannelNumPos]);

            mRecordSetting = new PLRecordSetting();
            mRecordSetting.setMaxRecordDuration((long) (RecordSettings.DEFAULT_MAX_RECORD_DURATION * mRecordSpeed));
            mRecordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
            mRecordSetting.setVideoFilepath(Config.RECORD_FILE_PATH);

            mFaceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);

            mShortVideoRecorder.prepare(preview, mCameraSetting, mMicrophoneSetting, mVideoEncodeSetting,
                    mAudioEncodeSetting, USE_TUSDK ? null : mFaceBeautySetting, mRecordSetting);
            mSectionProgressBar.setFirstPointTime((long) (RecordSettings.DEFAULT_MIN_RECORD_DURATION * mRecordSpeed));
            onSectionCountChanged(0, 0);
        } else {
            PLDraft draft = PLDraftBox.getInstance(this).getDraftByTag(draftTag);
            if (draft == null) {
                ToastUtils.s(this, getString(R.string.toast_draft_recover_fail));
                finish();
            }

            mCameraSetting = draft.getCameraSetting();
            mMicrophoneSetting = draft.getMicrophoneSetting();
            mVideoEncodeSetting = draft.getVideoEncodeSetting();
            mAudioEncodeSetting = draft.getAudioEncodeSetting();
            mRecordSetting = draft.getRecordSetting();
            mFaceBeautySetting = draft.getFaceBeautySetting();

            if (mShortVideoRecorder.recoverFromDraft(preview, draft)) {
                long draftDuration = 0;
                for (int i = 0; i < draft.getSectionCount(); ++i) {
                    long currentDuration = draft.getSectionDuration(i);
                    draftDuration += draft.getSectionDuration(i);
                    onSectionIncreased(currentDuration, draftDuration, i + 1);
                }
                mSectionProgressBar.setFirstPointTime(draftDuration);
                ToastUtils.s(this, getString(R.string.toast_draft_recover_success));
            } else {
                onSectionCountChanged(0, 0);
                mSectionProgressBar.setFirstPointTime((long) (RecordSettings.DEFAULT_MIN_RECORD_DURATION * mRecordSpeed));
                ToastUtils.s(this, getString(R.string.toast_draft_recover_fail));
            }
        }
        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        mSectionProgressBar.setTotalTime(this, mRecordSetting.getMaxRecordDuration());

        if (USE_TUSDK) {
            findViewById(R.id.btn_camera_effect).setVisibility(View.VISIBLE);

            mShortVideoRecorder.setVideoFilterListener(new PLVideoFilterListener() {

                @Override
                public void onSurfaceCreated() {
                    prepareFilterEngine();
                    mFilterEngine.onSurfaceCreated();
                    changeVideoFilterCode(filterCode);
                }

                @Override
                public void onSurfaceChanged(int width, int height) {
                    mFilterEngine.onSurfaceChanged(width, height);
                }

                @Override
                public void onSurfaceDestroy() {
                    destroyFilterEngine();
                }

                @Override
                public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
                    return mFilterEngine.processFrame(texId, texWidth, texHeight);
                }
            });
        }

        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            private long mSectionBeginTSMs;
            private boolean mSectionBegan;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (!mSectionBegan && mShortVideoRecorder.beginSection()) {
                        mSectionBegan = true;
                        mSectionBeginTSMs = System.currentTimeMillis();
                        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
                        updateRecordingBtns(true);
                    } else {
                        ToastUtils.s(VideoRecordActivity.this, "无法开始视频段录制");
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (mSectionBegan) {
                        long totalDurationMs = (System.currentTimeMillis() - mSectionBeginTSMs) + (mDurationRecordStack.isEmpty() ? 0 : mDurationRecordStack.peek());
                        mDurationRecordStack.push(totalDurationMs);
                        mSectionProgressBar.addBreakPointTime(totalDurationMs);
                        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
                        mShortVideoRecorder.endSection();
                        mSectionBegan = false;
                    }
                }

                return false;
            }
        });
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mFocusIndicatorX = (int) e.getX() - mFocusIndicator.getWidth() / 2;
                mFocusIndicatorY = (int) e.getY() - mFocusIndicator.getHeight() / 2;
                mShortVideoRecorder.manualFocus(mFocusIndicator.getWidth(), mFocusIndicator.getHeight(), (int) e.getX(), (int) e.getY());
                return false;
            }
        });
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = getScreenRotation(orientation);

                //tusdk 相关
                if (mFilterEngine != null) {
                    if (rotation == 0) {
                        mFilterEngine.setInterfaceOrientation(InterfaceOrientation.Portrait);
                    } else if (rotation == 90) {
                        mFilterEngine.setInterfaceOrientation(InterfaceOrientation.LandscapeLeft);
                    } else if (rotation == 180) {
                        mFilterEngine.setInterfaceOrientation(InterfaceOrientation.PortraitUpsideDown);
                    } else if (rotation == 270) {
                        mFilterEngine.setInterfaceOrientation(InterfaceOrientation.LandscapeRight);
                    }
                }
            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    private int getScreenRotation(int orientation) {
        int screenRotation = 0;
        if (orientation >= 315 || orientation < 45) {
            screenRotation = 0;
        } else if (orientation >= 45 && orientation < 135) {
            screenRotation = 90;
        } else if (orientation >= 135 && orientation < 225) {
            screenRotation = 180;
        } else if (orientation >= 225 && orientation < 315) {
            screenRotation = 270;
        }
        return screenRotation;
    }

    private void updateRecordingBtns(boolean isRecording) {
        mSwitchCameraBtn.setEnabled(!isRecording);
        mRecordBtn.setActivated(isRecording);
    }

    public void onScreenRotation(View v) {
        if (mDeleteBtn.isEnabled()) {
            ToastUtils.s(this, "已经开始拍摄，无法旋转屏幕。");
        } else {
            setRequestedOrientation(
                    getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            if (mFrontCamera) {
                mFilterEngine.setInputImageOrientation(ImageOrientation.UpMirrored);
                mFilterEngine.setOutputImageOrientation(ImageOrientation.UpMirrored);
            } else {
                mFilterEngine.setInputImageOrientation(ImageOrientation.Up);
                mFilterEngine.setOutputImageOrientation(ImageOrientation.Up);
            }
        } else {
            if (mFrontCamera) {
                mFilterEngine.setInputImageOrientation(ImageOrientation.LeftMirrored);
                mFilterEngine.setOutputImageOrientation(ImageOrientation.LeftMirrored);
            } else {
                mFilterEngine.setInputImageOrientation(ImageOrientation.Right);
                mFilterEngine.setOutputImageOrientation(ImageOrientation.Left);
            }
        }

        super.onConfigurationChanged(newConfig);
    }

    public void onCaptureFrame(View v) {
        mShortVideoRecorder.captureFrame(new PLCaptureFrameListener() {
            @Override
            public void onFrameCaptured(PLVideoFrame capturedFrame) {
                if (capturedFrame == null) {
                    Log.e(TAG, "capture frame failed");
                    return;
                }

                Log.i(TAG, "captured frame width: " + capturedFrame.getWidth() + " height: " + capturedFrame.getHeight() + " timestamp: " + capturedFrame.getTimestampMs());
                try {
                    FileOutputStream fos = new FileOutputStream(Config.CAPTURED_FRAME_FILE_PATH);
                    capturedFrame.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.s(VideoRecordActivity.this, "截帧已保存到路径：" + Config.CAPTURED_FRAME_FILE_PATH);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mRecordBtn.setEnabled(false);
        mShortVideoRecorder.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateRecordingBtns(false);
        mShortVideoRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShortVideoRecorder.destroy();
        mOrientationListener.disable();
    }

    public void onClickDelete(View v) {
        if (!mShortVideoRecorder.deleteLastSection()) {
            ToastUtils.s(this, "回删视频段失败");
        }
    }

    public void onClickShowTutu(View v) {
        switchTutuPanel(true);
    }

    private void switchTutuPanel(boolean show) {
        if (mIsFilterShow) {
            mBottomControlPanel.setVisibility(View.VISIBLE);
            hideFilterStaff();
        } else {
            mBottomControlPanel.setVisibility(View.GONE);
            hideStickerStaff();
            showSmartBeautyLayout();
        }
    }

    public void onClickConcat(View v) {
        mProcessingDialog.show();
        showChooseDialog();
    }

    public void onClickBrightness(View v) {
        boolean isVisible = mAdjustBrightnessSeekBar.getVisibility() == View.VISIBLE;
        mAdjustBrightnessSeekBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    public void onClickSwitchCamera(View v) {
        mFrontCamera = !mFrontCamera;
        mShortVideoRecorder.switchCamera();
        mFocusIndicator.focusCancel();
    }

    public void onClickSwitchFlash(View v) {
        mFlashEnabled = !mFlashEnabled;
        mShortVideoRecorder.setFlashEnabled(mFlashEnabled);
        mSwitchFlashBtn.setActivated(mFlashEnabled);
    }

    public void onClickAddMixAudio(View v) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        startActivityForResult(Intent.createChooser(intent, "请选择混音文件："), 0);
    }

    public void onClickSaveToDraft(View v) {
        final EditText editText = new EditText(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setView(editText)
                .setTitle(getString(R.string.dlg_save_draft_title))
                .setPositiveButton(getString(R.string.dlg_save_draft_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ToastUtils.s(VideoRecordActivity.this,
                                mShortVideoRecorder.saveToDraftBox(editText.getText().toString()) ?
                                        getString(R.string.toast_draft_save_success) : getString(R.string.toast_draft_save_fail));
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mShortVideoRecorder.setMusicFile(selectedFilepath);
            }
        }
    }

    @Override
    public void onReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwitchFlashBtn.setVisibility(mShortVideoRecorder.isFlashSupport() ? View.VISIBLE : View.GONE);
                mFlashEnabled = false;
                mSwitchFlashBtn.setActivated(mFlashEnabled);
                mRecordBtn.setEnabled(true);
                refreshSeekBar();
                ToastUtils.s(VideoRecordActivity.this, "可以开始拍摄咯");
            }
        });
    }

    @Override
    public void onError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.toastErrorCode(VideoRecordActivity.this, code);
            }
        });
    }

    @Override
    public void onDurationTooShort() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(VideoRecordActivity.this, "该视频段太短了");
            }
        });
    }

    @Override
    public void onRecordStarted() {
        Log.i(TAG, "record start time: " + System.currentTimeMillis());
    }

    @Override
    public void onRecordStopped() {
        Log.i(TAG, "record stop time: " + System.currentTimeMillis());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecordingBtns(false);
            }
        });
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section increased incDuration: " + incDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section decreased decDuration: " + decDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.removeLastBreakPoint();
        mDurationRecordStack.pop();
    }

    @Override
    public void onRecordCompleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(VideoRecordActivity.this, "已达到拍摄总时长");
            }
        });
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

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                ToastUtils.s(VideoRecordActivity.this, "拼接视频段失败: " + errorCode);
            }
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    @Override
    public void onSaveVideoSuccess(final String filePath) {
        Log.i(TAG, "concat sections success filePath: " + filePath);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                if (mIsEditVideo) {
                    VideoEditActivity.start(VideoRecordActivity.this, filePath);
                } else {
                    PlaybackActivity.start(VideoRecordActivity.this, filePath);
                }
            }
        });
    }

    private void refreshSeekBar() {
        final int max = mShortVideoRecorder.getMaxExposureCompensation();
        final int min = mShortVideoRecorder.getMinExposureCompensation();
        boolean brightnessAdjustAvailable = (max != 0 || min != 0);
        Log.e(TAG, "max/min exposure compensation: " + max + "/" + min + " brightness adjust available: " + brightnessAdjustAvailable);

        findViewById(R.id.brightness_panel).setVisibility(brightnessAdjustAvailable ? View.VISIBLE : View.GONE);
        mAdjustBrightnessSeekBar.setOnSeekBarChangeListener(!brightnessAdjustAvailable ? null : new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i <= Math.abs(min)) {
                    mShortVideoRecorder.setExposureCompensation(i + min);
                } else {
                    mShortVideoRecorder.setExposureCompensation(i - max);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mAdjustBrightnessSeekBar.setMax(max + Math.abs(min));
        mAdjustBrightnessSeekBar.setProgress(Math.abs(min));
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeleteBtn.setEnabled(count > 0);
                mConcatBtn.setEnabled(totalTime >= (RecordSettings.DEFAULT_MIN_RECORD_DURATION * mRecordSpeed));
            }
        });
    }

    private PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.if_edit_video));
        builder.setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = true;
                mShortVideoRecorder.concatSections(VideoRecordActivity.this);
            }
        });
        builder.setNegativeButton(getString(R.string.dlg_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = false;
                mShortVideoRecorder.concatSections(VideoRecordActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    public void onSpeedClicked(View view) {
        if (mSectionProgressBar.isRecorded()) {
            ToastUtils.s(this, "已经拍摄视频，无法再设置拍摄倍数！");
            return;
        }

        if (mSpeedTextView != null) {
            mSpeedTextView.setTextColor(getResources().getColor(R.color.speedTextNormal));
        }

        TextView textView = (TextView) view;
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        mSpeedTextView = textView;

        switch (view.getId()) {
            case R.id.super_slow_speed_text:
                mRecordSpeed = RECORD_SPEED_ARRAY[0];
                break;
            case R.id.slow_speed_text:
                mRecordSpeed = RECORD_SPEED_ARRAY[1];
                break;
            case R.id.normal_speed_text:
                mRecordSpeed = RECORD_SPEED_ARRAY[2];
                break;
            case R.id.fast_speed_text:
                mRecordSpeed = RECORD_SPEED_ARRAY[3];
                break;
            case R.id.super_fast_speed_text:
                mRecordSpeed = RECORD_SPEED_ARRAY[4];
                break;
        }

        mRecordSetting.setMaxRecordDuration((long) (RecordSettings.DEFAULT_MAX_RECORD_DURATION * mRecordSpeed));
        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        mSectionProgressBar.setFirstPointTime((long) (RecordSettings.DEFAULT_MIN_RECORD_DURATION * mRecordSpeed));
        mSectionProgressBar.setTotalTime(this, mRecordSetting.getMaxRecordDuration());
    }

    @Override
    public void onManualFocusStart(boolean result) {
        if (result) {
            Log.i(TAG, "manual focus begin success");
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFocusIndicator.getLayoutParams();
            lp.leftMargin = mFocusIndicatorX;
            lp.topMargin = mFocusIndicatorY;
            mFocusIndicator.setLayoutParams(lp);
            mFocusIndicator.focus();
        } else {
            mFocusIndicator.focusCancel();
            Log.i(TAG, "manual focus not supported");
        }
    }

    @Override
    public void onManualFocusStop(boolean result) {
        Log.i(TAG, "manual focus end result: " + result);
        if (result) {
            mFocusIndicator.focusSuccess();
        } else {
            mFocusIndicator.focusFail();
        }
    }

    @Override
    public void onManualFocusCancel() {
        Log.i(TAG, "manual focus canceled");
        mFocusIndicator.focusCancel();
    }

    @Override
    public void onAutoFocusStart() {
        Log.i(TAG, "auto focus start");
    }

    @Override
    public void onAutoFocusStop() {
        Log.i(TAG, "auto focus stop");
    }

    /// ========================= TuSDK 相关 ========================= ///

    // 滤镜 code 列表, 每个 code 代表一种滤镜效果, 具体 code 可在 lsq_tusdk_configs.json 查看 (例如:lsq_filter_SkinNature02 滤镜的 code 为 SkinNature02)
    private static final String[] VIDEOFILTERS = new String[]{"None", "nature", "pink", "jelly",
            "ruddy", "sugar", "honey", "clear", "timber", "whitening", "porcelain"};
    /**
     * 参数调节视图
     */
    protected FilterConfigView mConfigView;
    /**
     * 滤镜底部栏
     */
    private View mFilterBottomView;
    /**
     * 滤镜栏视图
     */
    protected FilterListView mFilterListView;
    // 用于记录焦点位置
    private int mFocusPostion = 1;
    // TuSDK Filter Engine
    private TuSDKFilterEngine mFilterEngine;

    private boolean mFrontCamera = true;
    //贴纸底部栏
    private RecyclerView mStickerBottomView;
    private StickerListAdapter stickerListAdapter;

    //贴纸 tab
    private TuSdkTextButton mStickerTab;

    public void onCloseBottomPanel(View view) {
        hideFilterStaff();
        mBottomControlPanel.setVisibility(View.VISIBLE);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lsq_beauty_btn:
                showBeautySeekBar();
                break;
            case R.id.lsq_filter_btn:
                showFilterLayout();
                break;
            case R.id.lsq_sticker_btn:
                showStickerLayout();
                break;
        }
        setContentVisible(view.getId());
    }

    private void initTuSDK() {
        mIsFirstEntry = true;
        initFilterListView();
        initStickerListView();
        //动态加载版本初始化
        initBottomView();
    }

    /**
     * 准备滤镜引擎
     */
    private void prepareFilterEngine() {
//        if(mFilterEngine != null) return;

        mFilterEngine = new TuSDKFilterEngine(getBaseContext(), false);

        // TuSDKFilterEngine 事件回调
        mFilterEngine.setDelegate(mFilterDelegate);

        // 设置是否输出原始图片朝向 false: 图像被转正后输出
        mFilterEngine.setOutputOriginalImageOrientation(true);

        mFilterEngine.setCameraFacing(mFrontCamera ? CameraConfigs.CameraFacing.Front : CameraConfigs.CameraFacing.Back);

        //根据屏幕方向设置滤镜方向
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mFilterEngine.setInterfaceOrientation(InterfaceOrientation.Portrait);
        } else {
            mFilterEngine.setInterfaceOrientation(InterfaceOrientation.LandscapeRight);
        }

        // 设置输入的图片朝向 如果输入的图片不是原始朝向 该选项必须配置
        mFilterEngine.setInputImageOrientation(ImageOrientation.DownMirrored);
        // 设置是否开启动态贴纸功能
        mFilterEngine.setEnableLiveSticker(true);
    }

    // TuSDKFilterEngine 事件回调
    private TuSDKFilterEngine.TuSDKFilterEngineDelegate mFilterDelegate = new TuSDKFilterEngine.TuSDKFilterEngineDelegate() {
        /**
         * 滤镜更改事件，每次调用 switchFilter 切换滤镜后即触发该事件
         *
         * @param filterWrap
         *            新的滤镜对象
         */
        @Override
        public void onFilterChanged(FilterWrap filterWrap) {
            // 获取滤镜参数列表. 如果开发者希望自定义滤镜栏,可通过 ilter.getParameter().getArgs() 对象获取支持的参数列表。
            if (filterWrap == null) return;

            // 默认滤镜参数调节
            SelesParameters params = filterWrap.getFilterParameter();
            List<SelesParameters.FilterArg> list = params.getArgs();
            for (SelesParameters.FilterArg arg : list) {
                if (arg.equalsKey("smoothing") && mSmoothingProgress != -1.0f)
                    arg.setPrecentValue(mSmoothingProgress);
                else if (arg.equalsKey("smoothing") && mSmoothingProgress == -1.0f)
                    mSmoothingProgress = arg.getPrecentValue();
                else if (arg.equalsKey("mixied") && mMixiedProgress != -1.0f)
                    arg.setPrecentValue(mMixiedProgress);
                else if (arg.equalsKey("mixied") && mMixiedProgress == -1.0f)
                    mMixiedProgress = arg.getPrecentValue();
                else if (arg.equalsKey("eyeSize") && mEyeSizeProgress != -1.0f)
                    arg.setPrecentValue(mEyeSizeProgress);
                else if (arg.equalsKey("chinSize") && mChinSizeProgress != -1.0f)
                    arg.setPrecentValue(mChinSizeProgress);
                else if (arg.equalsKey("eyeSize") && mEyeSizeProgress == -1.0f)
                    mEyeSizeProgress = arg.getPrecentValue();
                else if (arg.equalsKey("chinSize") && mChinSizeProgress == -1.0f)
                    mChinSizeProgress = arg.getPrecentValue();
            }
            filterWrap.setFilterParameter(params);

            mSelesOutInput = filterWrap;

            if (getFilterConfigView() != null)
                getFilterConfigView().setSelesFilter(mSelesOutInput.getFilter());

            if (mIsFirstEntry || (mBeautyLayout != null && mBeautyLayout.getVisibility() == View.VISIBLE)) {
                mIsFirstEntry = false;
                showBeautySeekBar();
            }
        }

        @Override
        public void onPictureDataCompleted(IntBuffer intBuffer, TuSdkSize tuSdkSize) {

        }

        @Override
        public void onPreviewScreenShot(Bitmap bitmap) {

        }
    };

    public void initBottomView() {

        mBeautyTab = (TuSdkTextButton) findViewById(R.id.lsq_beauty_btn);
        mBeautyLayout = (LinearLayout) findViewById(R.id.lsq_beauty_content);

        mFilterTab = (TuSdkTextButton) findViewById(R.id.lsq_filter_btn);
        mFilterLayout = (RelativeLayout) findViewById(R.id.lsq_filter_content);

        mStickerTab = (TuSdkTextButton) findViewById(R.id.lsq_sticker_btn);

        //美颜
        mSmoothingBarLayout = (ConfigViewSeekBar) mBeautyLayout.findViewById(R.id.lsq_dermabrasion_bar);
        mSmoothingBarLayout.getTitleView().setText(R.string.lsq_dermabrasion);
        mSmoothingBarLayout.getSeekbar().setDelegate(mTuSeekBarDelegate);

        mEyeSizeBarLayout = (ConfigViewSeekBar) mBeautyLayout.findViewById(R.id.lsq_big_eyes_bar);
        mEyeSizeBarLayout.getTitleView().setText(R.string.lsq_big_eyes);
        mEyeSizeBarLayout.getSeekbar().setDelegate(mTuSeekBarDelegate);

        mChinSizeBarLayout = (ConfigViewSeekBar) mBeautyLayout.findViewById(R.id.lsq_thin_face_bar);
        mChinSizeBarLayout.getTitleView().setText(R.string.lsq_thin_face);
        mChinSizeBarLayout.getSeekbar().setDelegate(mTuSeekBarDelegate);

        mFilterBottomView = findViewById(R.id.lsq_filter_group_bottom_view);
    }

    /**
     * 拖动条监听事件
     */
    private TuSeekBar.TuSeekBarDelegate mTuSeekBarDelegate = new TuSeekBar.TuSeekBarDelegate() {
        @Override
        public void onTuSeekBarChanged(TuSeekBar seekBar, float progress) {
            if (seekBar == mSmoothingBarLayout.getSeekbar()) {
                mSmoothingProgress = progress;
                applyFilter(mSmoothingBarLayout, "smoothing", progress);
            } else if (seekBar == mEyeSizeBarLayout.getSeekbar()) {
                mEyeSizeProgress = progress;
                applyFilter(mEyeSizeBarLayout, "eyeSize", progress);
            } else if (seekBar == mChinSizeBarLayout.getSeekbar()) {
                mChinSizeProgress = progress;
                applyFilter(mChinSizeBarLayout, "chinSize", progress);
            }
        }
    };

    /**
     * 应用滤镜
     *
     * @param viewSeekBar
     * @param key
     * @param progress
     */
    private void applyFilter(ConfigViewSeekBar viewSeekBar, String key, float progress) {
        if (viewSeekBar == null || mSelesOutInput == null) return;

        viewSeekBar.getConfigValueView().setText((int) (progress * 100) + "%");
        SelesParameters params = mSelesOutInput.getFilterParameter();
        params.setFilterArg(key, progress);
        mSelesOutInput.submitFilterParameter();
    }

    /**
     * 显示贴纸底部栏
     */
    public void showStickerLayout() {
        updateStickerViewStaff(true);

        // 滤镜栏向上动画并显示
        ViewCompat.setTranslationY(mStickerBottomView,
                mStickerBottomView.getHeight());
        ViewCompat.animate(mStickerBottomView).translationY(0).setDuration(200).setListener(mViewPropertyAnimatorListener);

        mBeautyLayout.setVisibility(View.GONE);
        mFilterLayout.setVisibility(View.GONE);

        setTabActive(mStickerTab);
    }

    /**
     * 点击贴纸栏上方的空白区域隐藏贴纸栏
     */
    public void hideStickerStaff() {
        if (mStickerBottomView.getVisibility() == View.GONE) return;

        updateStickerViewStaff(false);

        // 滤镜栏向下动画并隐藏
        ViewCompat.animate(mStickerBottomView)
                .translationY(mStickerBottomView.getHeight()).setDuration(200);
    }

    /**
     * 属性动画监听事件
     */
    private ViewPropertyAnimatorListener mViewPropertyAnimatorListener = new ViewPropertyAnimatorListener() {

        @Override
        public void onAnimationCancel(View view) {

        }

        @Override
        public void onAnimationEnd(View view) {
            ViewCompat.animate(mStickerBottomView).setListener(null);
            ViewCompat.animate(mFilterBottomView).setListener(null);
            mStickerBottomView.clearAnimation();
            mFilterBottomView.clearAnimation();
        }

        @Override
        public void onAnimationStart(View view) {

        }
    };

    /**
     * 更新贴纸栏相关视图的显示状态
     *
     * @param isShow
     */
    private void updateStickerViewStaff(boolean isShow) {
        mStickerBottomView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * 初始化贴纸组视图
     */
    protected void initStickerListView() {

        mStickerBottomView = (RecyclerView) findViewById(R.id.lsq_sticker_list_view);
        mStickerBottomView.setVisibility(View.GONE);

        stickerListAdapter = new StickerListAdapter();
        GridLayoutManager manager = new GridLayoutManager(this, 5);
        mStickerBottomView.setLayoutManager(manager);
        mStickerBottomView.setAdapter(stickerListAdapter);
        stickerListAdapter.setStickerList(getRawStickGroupList());

        stickerListAdapter.setOnItemClickListener(new StickerListAdapter.OnItemClickListener() {

            @Override
            public void onClickItem(StickerGroup itemData, StickerListAdapter.StickerHolder
                    stickerHolder, int position) {
                onStickerGroupSelected(itemData, stickerHolder, position);
            }
        });
    }

    /**
     * 贴纸组选择事件
     *
     * @param itemData
     * @param stickerHolder
     * @param position
     */
    protected void onStickerGroupSelected(StickerGroup itemData, StickerListAdapter.StickerHolder
            stickerHolder, int position) {
        // 设置点击贴纸时呈现或是隐藏贴纸
        if (position == 0) {
            mFilterEngine.removeAllLiveSticker();
            stickerListAdapter.setSelectedPosition(position);
            return;
        }

        // 如果贴纸已被下载到本地
        if (stickerListAdapter.isDownloaded(itemData)) {
            stickerListAdapter.setSelectedPosition(position);
            // 必须重新获取StickerGroup,否则itemData.stickers为null
            itemData = StickerLocalPackage.shared().getStickerGroup(itemData.groupId);
            mFilterEngine.showGroupSticker(itemData);
        } else {
            stickerListAdapter.downloadStickerGroup(itemData, stickerHolder);
        }
    }

    /**
     * 获取本地贴纸列表
     *
     * @return
     */
    public List<StickerGroup> getRawStickGroupList() {
        List<StickerGroup> list = new ArrayList<StickerGroup>();
        try {
            InputStream stream = getResources().openRawResource(R.raw.square_sticker);

            if (stream == null) return null;

            byte buffer[] = new byte[stream.available()];
            stream.read(buffer);
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = JsonHelper.json(json);
            JSONArray jsonArray = jsonObject.getJSONArray("stickerGroups");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                StickerGroup group = new StickerGroup();
                group.groupId = item.optLong("id");
                group.previewName = item.optString("previewImage");
                group.name = item.optString("name");
                list.add(group);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 滤镜
     */

    // 记录是否是首次进入录制页面
    private boolean mIsFirstEntry = true;
    private boolean mIsFilterShow = false;

    // 记录当前滤镜
    private FilterWrap mSelesOutInput;
    // 滤镜Tab
    private TuSdkTextButton mFilterTab;
    // 美颜布局
    private RelativeLayout mFilterLayout;
    // 美颜Tab
    private TuSdkTextButton mBeautyTab;
    // 美颜布局
    private LinearLayout mBeautyLayout;
    // 磨皮调节栏
    private ConfigViewSeekBar mSmoothingBarLayout;
    // 大眼调节栏
    private ConfigViewSeekBar mEyeSizeBarLayout;
    // 瘦脸调节栏
    private ConfigViewSeekBar mChinSizeBarLayout;
    // 用于记录当前调节栏效果系数
    private float mMixiedProgress = -1.0f;
    // 用于记录当前调节栏磨皮系数
    private float mSmoothingProgress = -1.0f;
    // 用于记录当前调节栏大眼系数
    private float mEyeSizeProgress = -1.0f;
    // 用于记录当前调节栏瘦脸系数
    private float mChinSizeProgress = -1.0f;

    /**
     * 初始化滤镜栏视图
     */
    protected void initFilterListView() {
        getFilterListView();

        this.mFilterListView.setModeList(Arrays.asList(VIDEOFILTERS));
        ThreadHelper.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!mIsFirstEntry) return;

                mIsFirstEntry = false;
                changeVideoFilterCode(Arrays.asList(VIDEOFILTERS).get(mFocusPostion));
            }

        }, 1000);
    }

    /**
     * 滤镜栏视图
     *
     * @return
     */
    public FilterListView getFilterListView() {
        if (mFilterListView == null) {
            mFilterListView = (FilterListView) findViewById(R.id.lsq_filter_list_view);
            mFilterListView.loadView();
            mFilterListView.setCellLayoutId(R.layout.filter_list_cell_view);
            mFilterListView.setCellWidth(TuSdkContext.dip2px(62));
            mFilterListView.setItemClickDelegate(mFilterTableItemClickDelegate);
            mFilterListView.reloadData();
            mFilterListView.selectPosition(mFocusPostion);
        }
        return mFilterListView;
    }

    //上次切换滤镜
    private String filterCode;

    /**
     * 切换滤镜
     *
     * @param code
     */
    protected void changeVideoFilterCode(final String code) {
        if (mFilterEngine == null) return;

        filterCode = code;
        // 切换滤镜效果 code 为滤镜代号可在 lsq_tusdk_configs.json 查看

        mFilterEngine.switchFilter(filterCode);
    }

    /**
     * 滤镜组列表点击事件
     */
    private TuSdkTableView.TuSdkTableViewItemClickDelegate<String, FilterCellView> mFilterTableItemClickDelegate = new TuSdkTableView.TuSdkTableViewItemClickDelegate<String, FilterCellView>() {
        @Override
        public void onTableViewItemClick(String itemData,
                                         FilterCellView itemView, int position) {
            onFilterGroupSelected(itemData, itemView, position);
        }
    };

    /**
     * 滤镜组选择事件
     *
     * @param itemData
     * @param itemView
     * @param position
     */
    protected void onFilterGroupSelected(String itemData,
                                         FilterCellView itemView, int position) {
        FilterCellView prevCellView = (FilterCellView) mFilterListView.findViewWithTag(mFocusPostion);
        mFocusPostion = position;
        changeVideoFilterCode(itemData);
        mFilterListView.selectPosition(mFocusPostion);
        deSelectLastFilter(prevCellView);
        selectFilter(itemView, position);
        getFilterConfigView().setVisibility((position == 0) ? View.GONE : View.VISIBLE);
    }

    /**
     * 取消上一个滤镜的选中状态
     *
     * @param lastFilter
     */
    private void deSelectLastFilter(FilterCellView lastFilter) {
        if (lastFilter == null) return;

        updateFilterBorderView(lastFilter, true);
        lastFilter.getTitleView().setBackground(TuSdkContext.getDrawable(R.drawable.tusdk_view_filter_unselected_text_roundcorner));
        lastFilter.getImageView().invalidate();
    }

    /**
     * 设置滤镜单元边框是否可见
     *
     * @param lastFilter
     * @param isHidden
     */
    private void updateFilterBorderView(FilterCellView lastFilter, boolean isHidden) {
        View filterBorderView = lastFilter.getBorderView();
        filterBorderView.setVisibility(isHidden ? View.GONE : View.VISIBLE);
    }

    /**
     * 滤镜选中状态
     *
     * @param itemView
     * @param position
     */
    private void selectFilter(FilterCellView itemView, int position) {
        updateFilterBorderView(itemView, false);
        itemView.setFlag(position);
        TextView titleView = itemView.getTitleView();
        titleView.setBackground(TuSdkContext.getDrawable(R.drawable.tusdk_view_filter_selected_text_roundcorner));
    }

    /**
     * 滤镜配置视图
     *
     * @return
     */
    private FilterConfigView getFilterConfigView() {
        if (mConfigView == null) {
            mConfigView = (FilterConfigView) findViewById(R.id.lsq_filter_config_view);
        }

        return mConfigView;
    }

    /**
     * 显示智能美颜底部栏
     */
    public void showSmartBeautyLayout() {
        updateFilterViewStaff(true);

        // 滤镜栏向上动画并显示
        ViewCompat.setTranslationY(mFilterBottomView,
                mFilterBottomView.getHeight());
        ViewCompat.animate(mFilterBottomView).translationY(0).setDuration(200).setListener(mViewPropertyAnimatorListener);
        showBeautySeekBar();
    }


    /**
     * 隐藏滤镜栏
     */
    public void hideFilterStaff() {
        if (mFilterBottomView.getVisibility() == View.GONE) return;

        updateFilterViewStaff(false);

        // 滤镜栏向下动画并隐藏
        ViewCompat.animate(mFilterBottomView)
                .translationY(mFilterBottomView.getHeight()).setDuration(200);
    }

    /**
     * 更新滤镜栏相关视图的显示状态
     *
     * @param isShow
     */
    private void updateFilterViewStaff(boolean isShow) {
        mIsFilterShow = isShow;
        mFilterBottomView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    private void setContentVisible(int resID) {
        mBeautyLayout.setVisibility(View.GONE);
        mFilterLayout.setVisibility(View.GONE);
        mStickerBottomView.setVisibility(View.GONE);

        switch (resID) {
            case R.id.lsq_beauty_btn:
                mBeautyLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.lsq_filter_btn:
                mFilterLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.lsq_sticker_btn:
                mStickerBottomView.setVisibility(View.VISIBLE);
                break;
        }
    }


    /**
     * 显示美颜调节栏
     */
    private void showBeautySeekBar() {
        if (mIsFirstEntry) {
            changeVideoFilterCode(Arrays.asList(VIDEOFILTERS).get(mFocusPostion));
        }

        if (mBeautyLayout == null || mFilterLayout == null)
            return;

        mBeautyLayout.setVisibility(View.VISIBLE);
        mFilterLayout.setVisibility(View.GONE);

        setTabActive(mBeautyTab);

        if (mSelesOutInput == null) {
            setEnableAllSeekBar(false);
            return;
        }

        // 滤镜参数
        SelesParameters params = mSelesOutInput.getFilterParameter();
        if (params == null) {
            setEnableAllSeekBar(false);
            return;
        }

        List<SelesParameters.FilterArg> list = params.getArgs();
        if (list == null || list.size() == 0) {
            setEnableAllSeekBar(false);
            return;
        }

        for (SelesParameters.FilterArg arg : list) {
            if (arg.equalsKey("smoothing")) {
                setEnableSeekBar(mSmoothingBarLayout, true, arg.getPrecentValue(),
                        R.drawable.tusdk_view_widget_seekbar_drag);
            } else if (arg.equalsKey("eyeSize")) {
                setEnableSeekBar(mEyeSizeBarLayout, true, arg.getPrecentValue(),
                        R.drawable.tusdk_view_widget_seekbar_drag);
            } else if (arg.equalsKey("chinSize")) {
                setEnableSeekBar(mChinSizeBarLayout, true, arg.getPrecentValue(),
                        R.drawable.tusdk_view_widget_seekbar_drag);
            }
        }
    }

    /**
     * 显示滤镜列表
     */
    private void showFilterLayout() {
        if (mBeautyLayout == null || mFilterLayout == null)
            return;

        mFilterLayout.setVisibility(View.VISIBLE);
        mBeautyLayout.setVisibility(View.GONE);

        setTabActive(mFilterTab);

        if (mFocusPostion > 0 && getFilterConfigView() != null && mSelesOutInput != null) {
            getFilterConfigView().post(new Runnable() {

                @Override
                public void run() {
                    getFilterConfigView().setSelesFilter(mSelesOutInput.getFilter());
                    getFilterConfigView().setVisibility(View.VISIBLE);
                }
            });

            getFilterConfigView().setSeekBarDelegate(mConfigSeekBarDelegate);
            getFilterConfigView().invalidate();
        }
    }

    /**
     * 滤镜拖动条监听事件
     */
    private FilterConfigView.FilterConfigViewSeekBarDelegate mConfigSeekBarDelegate = new FilterConfigView.FilterConfigViewSeekBarDelegate() {

        @Override
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
            if (arg == null) return;

            if (arg.equalsKey("smoothing"))
                mSmoothingProgress = arg.getPrecentValue();
            else if (arg.equalsKey("eyeSize"))
                mEyeSizeProgress = arg.getPrecentValue();
            else if (arg.equalsKey("chinSize"))
                mChinSizeProgress = arg.getPrecentValue();
            else if (arg.equalsKey("mixied"))
                mMixiedProgress = arg.getPrecentValue();
        }

    };

    /**
     * 选中下方 Tab
     *
     * @param button
     */
    private void setTabActive(TuSdkTextButton button) {
        //reset the tabs inactive
        updateSmartBeautyTab(mFilterTab, false);
        updateSmartBeautyTab(mBeautyTab, false);
        updateSmartBeautyTab(mStickerTab, false);

        //set the special tab active
        updateSmartBeautyTab(button, true);
    }

    /**
     * 更新Tab按钮
     *
     * @param button
     * @param clickable
     */
    private void updateSmartBeautyTab(TuSdkTextButton button, boolean clickable) {
        int imgId = 0, colorId = 0;

        switch (button.getId()) {
            case R.id.lsq_filter_btn:
                imgId = clickable ? R.drawable.lsq_style_default_btn_filter_selected
                        : R.drawable.lsq_style_default_btn_filter_unselected;
                break;
            case R.id.lsq_beauty_btn:
                imgId = clickable ? R.drawable.lsq_style_default_btn_beauty_selected
                        : R.drawable.lsq_style_default_btn_beauty_unselected;
                break;
            case R.id.lsq_sticker_btn:
                imgId = clickable ? R.drawable.lsq_style_default_btn_filter_selected
                        : R.drawable.lsq_style_default_btn_filter_unselected;
                break;
        }
        colorId = clickable ? R.color.lsq_filter_title_color : R.color.lsq_filter_title_default_color;
        button.setCompoundDrawables(null, TuSdkContext.getDrawable(imgId), null, null);
        button.setTextColor(TuSdkContext.getColor(colorId));
    }

    private void setEnableAllSeekBar(boolean enable) {
        setEnableSeekBar(mSmoothingBarLayout, enable, 0, R.drawable.tusdk_view_widget_seekbar_none_drag);
        setEnableSeekBar(mEyeSizeBarLayout, enable, 0, R.drawable.tusdk_view_widget_seekbar_none_drag);
        setEnableSeekBar(mChinSizeBarLayout, enable, 0, R.drawable.tusdk_view_widget_seekbar_none_drag);
    }

    /**
     * 设置调节栏是否有效
     */
    private void setEnableSeekBar(ConfigViewSeekBar viewSeekBar, boolean enable, float progress, int id) {
        if (viewSeekBar == null) return;

        viewSeekBar.setProgress(progress);
        viewSeekBar.getSeekbar().setEnabled(enable);
        viewSeekBar.getSeekbar().getDragView().setBackgroundResource(id);
    }

    /**
     * 销毁 TuSDKFilterEngine
     */
    private void destroyFilterEngine() {
        if (mFilterEngine != null) {
            mFilterEngine.onSurfaceDestroy();
            mFilterEngine.destroy();
            mFilterEngine = null;
        }
    }
}
