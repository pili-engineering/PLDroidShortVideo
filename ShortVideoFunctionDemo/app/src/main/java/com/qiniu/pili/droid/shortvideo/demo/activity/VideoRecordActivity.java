package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLDraft;
import com.qiniu.pili.droid.shortvideo.PLDraftBox;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLFocusListener;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.PLWatermarkSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.FocusIndicator;
import com.qiniu.pili.droid.shortvideo.demo.view.SectionProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings.RECORD_SPEED_ARRAY;
import static com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings.chooseCameraFacingId;

public class VideoRecordActivity extends Activity implements PLRecordStateListener, PLVideoSaveListener, PLFocusListener {
    private static final String TAG = "VideoRecordActivity";
    public static final String DRAFT = "draft";

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
    private Switch mMuteSwitch;

    private TextView mRecordingPercentageView;

    private boolean mFlashEnabled;
    private boolean mIsEditVideo = false;
    private boolean mMusicLoop = true;

    private PLCameraSetting mCameraSetting;
    private PLMicrophoneSetting mMicrophoneSetting;
    private PLRecordSetting mRecordSetting;
    private PLVideoEncodeSetting mVideoEncodeSetting;
    private PLAudioEncodeSetting mAudioEncodeSetting;
    private PLFaceBeautySetting mFaceBeautySetting;
    private PLWatermarkSetting mWatermarkSetting;

    private int mFocusIndicatorX;
    private int mFocusIndicatorY;
    private int zoomIndex = 0;

    private double mRecordSpeed;
    private TextView mSpeedTextView;

    private OrientationEventListener mOrientationListener;
    private boolean mSectionBegan;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record);

        initView();
        prefs = getApplicationContext().getSharedPreferences(ConfigActivity.SP_NAME, Context.MODE_PRIVATE);
        GLSurfaceView preview = findViewById(R.id.preview);

        String draftTag = getIntent().getStringExtra(DRAFT);
        if (draftTag == null) {
            setCameraSetting();
            setVideoEncodeSetting();
            setMicrophoneSetting();
            setAudioEncodeSetting();
            setRecordSetting();
            setFaceBeautySetting();
            prepareAndSetShortVideoRecorder(preview);
            onSectionCountChanged(0, 0);
        } else {
            PLDraftBox draftBox = PLDraftBox.getInstance(this);
            PLDraft draft = draftBox.getDraftByTag(draftTag);
            if (draft == null) {
                ToastUtils.showShortToast(getString(R.string.toast_draft_recover_fail));
                draftBox.removeDraftByTag(draftTag, false);
                finish();
                return;
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
                ToastUtils.showShortToast(getString(R.string.toast_draft_recover_success));
            } else {
                onSectionCountChanged(0, 0);
                ToastUtils.showShortToast(getString(R.string.toast_draft_recover_fail));
            }
        }
        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        mSectionProgressBar.setProceedingSpeed(mRecordSpeed);
        mSectionProgressBar.setFirstPointTime(ConfigActivity.DEFAULT_MIN_RECORD_DURATION);
        mSectionProgressBar.setTotalTime(mRecordSetting.getMaxRecordDuration());

        mRecordBtn.setOnClickListener(v -> {
            if (mSectionBegan) {
                mShortVideoRecorder.endSection();
            } else {
                if (!mShortVideoRecorder.beginSection()) {
                    ToastUtils.showShortToast("无法开始视频段录制");
                }
            }
        });
        GestureDetector mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mFocusIndicatorX = (int) e.getX() - mFocusIndicator.getWidth() / 2;
                mFocusIndicatorY = (int) e.getY() - mFocusIndicator.getHeight() / 2;
                mShortVideoRecorder.manualFocus(mFocusIndicator.getWidth(), mFocusIndicator.getHeight(), (int) e.getX(), (int) e.getY());
                return false;
            }
        });
        ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                updateZoom(detector);
                return true;
            }
        });
        preview.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getPointerCount() == 1) {
                mGestureDetector.onTouchEvent(motionEvent);
            } else {
                mScaleGestureDetector.onTouchEvent(motionEvent);
            }
            return true;
        });

        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = getScreenRotation(orientation);
                if (!mSectionProgressBar.isRecorded() && !mSectionBegan) {
                    mVideoEncodeSetting.setRotationInMetadata(rotation);
                }
            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    private void initView() {
        mSectionProgressBar = findViewById(R.id.record_progressbar);
        mRecordBtn = findViewById(R.id.record);
        mDeleteBtn = findViewById(R.id.delete);
        mConcatBtn = findViewById(R.id.concat);
        mSwitchCameraBtn = findViewById(R.id.switch_camera);
        mSwitchFlashBtn = findViewById(R.id.switch_flash);
        mFocusIndicator = findViewById(R.id.focus_indicator);
        mAdjustBrightnessSeekBar = findViewById(R.id.adjust_brightness);
        mRecordingPercentageView = findViewById(R.id.recording_percentage);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortVideoRecorder.cancelConcat());

        mShortVideoRecorder = new PLShortVideoRecorder();
        mShortVideoRecorder.setRecordStateListener(this);

        mRecordSpeed = RECORD_SPEED_ARRAY[2];
        mSpeedTextView = findViewById(R.id.normal_speed_text);

        mMuteSwitch = findViewById(R.id.mute_switch);
        mMuteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mShortVideoRecorder.mute(true);
                } else {
                    mShortVideoRecorder.mute(false);
                }
            }
        });
    }

    private void updateZoom(ScaleGestureDetector detector) {
        List<Float> zooms = mShortVideoRecorder.getZooms();
        int maxIndex = zooms.size() - 1;
        float delta = detector.getScaleFactor() - 1;
        int deltaIndex = (int) (maxIndex * delta);
        int curIndex = zoomIndex;
        zoomIndex += deltaIndex;
        if (zoomIndex < 0) {
            zoomIndex = 0;
        } else if (zoomIndex > maxIndex) {
            zoomIndex = maxIndex;
        }
        int bigIndex = Math.max(zoomIndex, curIndex);
        int smallIndex = Math.min(zoomIndex, curIndex);
        for (int i = smallIndex; i < bigIndex; i++) {
            mShortVideoRecorder.setZoom(zooms.get(i));
        }
    }

    public void setCameraSetting() {
        int previewSizeRatioPos = prefs.getInt(ConfigActivity.KEY_PREVIEW_SIZE_RATIO_POS, ConfigActivity.DEFAULT_PREVIEW_SIZE_RATIO_POS);
        PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO previewSizeRatio = RecordSettings.PREVIEW_SIZE_RATIO_ARRAY[previewSizeRatioPos];
        int previewSizeLevelPos = prefs.getInt(ConfigActivity.KEY_PREVIEW_SIZE_LEVEL_POS, ConfigActivity.DEFAULT_PREVIEW_SIZE_LEVEL_POS);
        PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL previewSizeLevel = RecordSettings.PREVIEW_SIZE_LEVEL_ARRAY[previewSizeLevelPos];

        mCameraSetting = new PLCameraSetting();
        PLCameraSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCameraSetting.setCameraId(facingId);
        mCameraSetting.setCameraPreviewSizeRatio(previewSizeRatio);
        mCameraSetting.setCameraPreviewSizeLevel(previewSizeLevel);
    }

    private void setVideoEncodeSetting() {
        int encodeSizeLevelPos = prefs.getInt(ConfigActivity.KEY_VIDEO_ENCODE_SIZE_LEVEL_POS, ConfigActivity.DEFAULT_VIDEO_ENCODE_SIZE_LEVEL_POS);
        PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL encodingSizeLevel = RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[encodeSizeLevelPos];
        int encodeBitrateLevelPos = prefs.getInt(ConfigActivity.KEY_VIDEO_ENCODE_BITRATE_LEVEL_POS, ConfigActivity.DEFAULT_VIDEO_ENCODE_BITRATE_LEVEL_POS);
        int encodeBitrateLevel = getResources().getIntArray(R.array.encode_bitrate_level)[encodeBitrateLevelPos];
        boolean isHWCodecEnable = prefs.getBoolean(ConfigActivity.KEY_VIDEO_ENCODE_HARDWARE_CODEC_ENABLE, ConfigActivity.DEFAULT_VIDEO_HARDWARE_ENCODE_ENABLE);

        mVideoEncodeSetting = new PLVideoEncodeSetting(this);
        mVideoEncodeSetting.setEncodingSizeLevel(encodingSizeLevel);
        mVideoEncodeSetting.setEncodingBitrate(encodeBitrateLevel);
        mVideoEncodeSetting.setHWCodecEnabled(isHWCodecEnable);
        mVideoEncodeSetting.setConstFrameRateEnabled(true);
    }

    private void setAudioEncodeSetting() {
        boolean isHWCodecEnabled = prefs.getBoolean(ConfigActivity.KEY_AUDIO_ENCODE_HARDWARE_CODEC_ENABLE, ConfigActivity.DEFAULT_AUDIO_HARDWARE_ENCODE_ENABLE);
        int audioChannelNumPos = prefs.getInt(ConfigActivity.KEY_AUDIO_CHANNEL_NUM_POS, ConfigActivity.DEFAULT_AUDIO_CHANNEL_NUM_POS);
        int audioChannelNum = getResources().getIntArray(R.array.audio_channel_num)[audioChannelNumPos];
        int audioEncodeBitrateLevelPos = prefs.getInt(ConfigActivity.KEY_AUDIO_ENCODE_BITRATE_LEVEL_POS, ConfigActivity.DEFAULT_AUDIO_ENCODE_BITRATE_LEVEL_POS);
        int audioEncodeBitrateLevel = getResources().getIntArray(R.array.encode_bitrate_level)[audioEncodeBitrateLevelPos];
        int audioSampleRate = prefs.getInt(ConfigActivity.KEY_AUDIO_SAMPLE_RATE, ConfigActivity.DEFAULT_AUDIO_SAMPLE_RATE);

        mAudioEncodeSetting = new PLAudioEncodeSetting();
        mAudioEncodeSetting.setHWCodecEnabled(isHWCodecEnabled);
        mAudioEncodeSetting.setChannels(audioChannelNum);
        mAudioEncodeSetting.setBitrate(audioEncodeBitrateLevel);
        mAudioEncodeSetting.setSampleRate(audioSampleRate);
    }

    private void setMicrophoneSetting() {
        int samplingSampleRate = prefs.getInt(ConfigActivity.KEY_SAMPLING_SAMPLE_RATE, ConfigActivity.DEFAULT_SAMPLING_SAMPLE_RATE);
        int channelNumPos = prefs.getInt(ConfigActivity.KEY_SAMPLING_CHANNEL_NUM_POS, ConfigActivity.DEFAULT_SAMPLING_CHANNEL_NUM_POS);
        int channelNum = RecordSettings.SAMPLING_CHANNEL_NUM_ARRAY[channelNumPos];
        int samplingFormatPos = prefs.getInt(ConfigActivity.KEY_SAMPLING_FORMAT_POS, ConfigActivity.DEFAULT_SAMPLING_FORMAT_POS);
        int samplingFormat = RecordSettings.SAMPLING_FORMAT_ARRAY[samplingFormatPos];
        int samplingSourcePos = prefs.getInt(ConfigActivity.KEY_SAMPLING_SOURCE_POS, ConfigActivity.DEFAULT_SAMPLING_SOURCE_POS);
        int samplingSource = RecordSettings.SAMPLING_SOURCE_ARRAY[samplingSourcePos];
        boolean bluetoothSCOEnable = prefs.getBoolean(ConfigActivity.KEY_SAMPLING_BLUETOOTH_SCO_ENABLE, ConfigActivity.DEFAULT_SAMPLING_BLUETOOTH_SCO_ENABLE);
        boolean ptsOptimizeEnabled = prefs.getBoolean(ConfigActivity.KEY_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE, ConfigActivity.DEFAULT_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE);
        boolean isNSEnabled = prefs.getBoolean(ConfigActivity.KEY_SAMPLING_NSE_ENABLE, ConfigActivity.DEFAULT_SAMPLING_NSE_ENABLE);
        boolean isAECEnabled = prefs.getBoolean(ConfigActivity.KEY_SAMPLING_AEC_ENABLE, ConfigActivity.DEFAULT_SAMPLING_AEC_ENABLE);

        mMicrophoneSetting = new PLMicrophoneSetting();
        mMicrophoneSetting.setSampleRate(samplingSampleRate);
        mMicrophoneSetting.setChannelConfig(channelNum);
        mMicrophoneSetting.setAudioFormat(samplingFormat);
        mMicrophoneSetting.setAudioSource(samplingSource);
        mMicrophoneSetting.setBluetoothSCOEnabled(bluetoothSCOEnable);
        mMicrophoneSetting.setPtsOptimizeEnabled(ptsOptimizeEnabled);
        mMicrophoneSetting.setNSEnabled(isNSEnabled);
        mMicrophoneSetting.setAECEnabled(isAECEnabled);
    }

    private void setWatermarkSetting() {
        int watermarkSettingTypePos = prefs.getInt(ConfigActivity.KEY_WATERMARK_SETTING_TYPE_POS, ConfigActivity.DEFAULT_WATERMARK_SETTING_TYPE_POS);
        int watermarkAlpha = prefs.getInt(ConfigActivity.KEY_WATERMARK_ALPHA, ConfigActivity.DEFAULT_WATERMARK_ALPHA);
        float watermarkHeight = prefs.getFloat(ConfigActivity.KEY_WATERMARK_HEIGHT, ConfigActivity.DEFAULT_WATERMARK_HEIGHT);
        float watermarkWidth = prefs.getFloat(ConfigActivity.KEY_WATERMARK_WIDTH, ConfigActivity.DEFAULT_WATERMARK_WIDTH);
        float watermarkPositionX = prefs.getFloat(ConfigActivity.KEY_WATERMARK_POSITION_X, ConfigActivity.DEFAULT_WATERMARK_POSITION_X);
        float watermarkPositionY = prefs.getFloat(ConfigActivity.KEY_WATERMARK_POSITION_Y, ConfigActivity.DEFAULT_WATERMARK_POSITION_Y);

        if (watermarkSettingTypePos == 0) {
            // 0 代表关闭水印
            return;
        } else if (watermarkSettingTypePos == 1) {
            mWatermarkSetting = new PLWatermarkSetting();
            mWatermarkSetting.setResourceId(R.drawable.qiniu_logo);
        } else if (watermarkSettingTypePos == 2) {
            mWatermarkSetting = new PLWatermarkSetting();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_flash_on);
            mWatermarkSetting.setBitmap(bitmap);
        }
        mWatermarkSetting.setSize(watermarkWidth, watermarkHeight);
        mWatermarkSetting.setPosition(watermarkPositionX, watermarkPositionY);
        mWatermarkSetting.setAlpha(watermarkAlpha);
    }

    private void setRecordSetting() {
        long maxRecordDuration = prefs.getLong(ConfigActivity.KEY_MAX_RECORD_DURATION, ConfigActivity.DEFAULT_MAX_RECORD_DURATION);
        boolean recordingSpeedVariableEnabled = prefs.getBoolean(
                ConfigActivity.KEY_RECORDING_SPEED_VARIABLE_ENABLED,
                ConfigActivity.DEFAULT_RECORDING_SPEED_VARIABLE_ENABLED);
        mRecordSetting = new PLRecordSetting();
        mRecordSetting.setMaxRecordDuration(maxRecordDuration);
        mRecordSetting.setRecordSpeedVariable(recordingSpeedVariableEnabled);
        mRecordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        mRecordSetting.setVideoFilepath(Config.RECORD_FILE_PATH);
    }

    private void setFaceBeautySetting() {
        mFaceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);
    }

    private void prepareAndSetShortVideoRecorder(GLSurfaceView preview) {
        mShortVideoRecorder.prepare(preview, mCameraSetting, mMicrophoneSetting, mVideoEncodeSetting,
                mAudioEncodeSetting, mFaceBeautySetting, mRecordSetting);
        boolean isMirrorEncode = prefs.getBoolean(ConfigActivity.KEY_MIRROR_ENCODE_ENABLE, ConfigActivity.DEFAULT_MIRROR_ENCODE_ENABLE);
        mShortVideoRecorder.setMirrorForEncode(isMirrorEncode);
        setWatermarkSetting();
        if (mWatermarkSetting != null) {
            mShortVideoRecorder.setWatermark(mWatermarkSetting);
        }
    }

    private int getScreenRotation(int orientation) {
        int screenRotation = 0;
        boolean isPortraitScreen = getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (orientation >= 315 || orientation < 45) {
            screenRotation = isPortraitScreen ? 0 : 90;
        } else if (orientation >= 45 && orientation < 135) {
            screenRotation = isPortraitScreen ? 90 : 180;
        } else if (orientation >= 135 && orientation < 225) {
            screenRotation = isPortraitScreen ? 180 : 270;
        } else if (orientation >= 225 && orientation < 315) {
            screenRotation = isPortraitScreen ? 270 : 0;
        }
        return screenRotation;
    }

    private void updateRecordingBtns(boolean isRecording) {
        mSwitchCameraBtn.setEnabled(!isRecording);
        mRecordBtn.setActivated(isRecording);
    }

    public void onScreenRotation(View v) {
        if (mDeleteBtn.isEnabled()) {
            ToastUtils.showShortToast("已经开始拍摄，无法旋转屏幕。");
        } else {
            setRequestedOrientation(
                    getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void onCaptureFrame(View v) {
        mShortVideoRecorder.captureFrame(capturedFrame -> {
            if (capturedFrame == null) {
                Log.e(TAG, "capture frame failed");
                return;
            }

            Log.i(TAG, "captured frame width: " + capturedFrame.getWidth() + " height: " + capturedFrame.getHeight() + " timestamp: " + capturedFrame.getTimestampMs());
            try {
                FileOutputStream fos = new FileOutputStream(Config.CAPTURED_FRAME_FILE_PATH);
                capturedFrame.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                runOnUiThread(() -> ToastUtils.showShortToast("截帧已保存到路径：" + Config.CAPTURED_FRAME_FILE_PATH));
            } catch (IOException e) {
                e.printStackTrace();
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
        mShortVideoRecorder.endSection();
        mShortVideoRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShortVideoRecorder.destroy(true);
        mOrientationListener.disable();
    }

    public void onClickDelete(View v) {
        if (!mShortVideoRecorder.deleteLastSection()) {
            ToastUtils.showShortToast("回删视频段失败");
        }
    }

    public void onClickConcat(View v) {
        mProcessingDialog.show();
        mProcessingDialog.setProgress(0);
        showChooseDialog();
    }

    public void onClickBrightness(View v) {
        boolean isVisible = mAdjustBrightnessSeekBar.getVisibility() == View.VISIBLE;
        mAdjustBrightnessSeekBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        if (mShortVideoRecorder != null) {
            mShortVideoRecorder.setAutoExposure(isVisible);
        }
    }

    public void onClickSwitchCamera(View v) {
        mShortVideoRecorder.switchCamera();
        mFocusIndicator.focusCancel();
    }

    public void onClickSwitchFlash(View v) {
        mFlashEnabled = !mFlashEnabled;
        mShortVideoRecorder.setFlashEnabled(mFlashEnabled);
        mSwitchFlashBtn.setActivated(mFlashEnabled);
    }

    public void onClickAddMixAudio(View v) {
        new AlertDialog.Builder(this)
                .setItems(new String[]{"循环", "单次"}, (dialog, which) -> {
                    mMusicLoop = which == 0;

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("audio/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 0);
                })
                .show();
    }

    public void onClickSaveToDraft(View v) {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setView(editText)
                .setTitle(getString(R.string.dlg_save_draft_title))
                .setPositiveButton(getString(R.string.dlg_save_draft_yes), (dialogInterface, i) -> {
                    ToastUtils.showShortToast(mShortVideoRecorder.saveToDraftBox(editText.getText().toString()) ?
                            getString(R.string.toast_draft_save_success) : getString(R.string.toast_draft_save_fail));
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mShortVideoRecorder.setMusicFile(selectedFilepath);
                mShortVideoRecorder.setMusicLoop(mMusicLoop);
            }
        }
    }

    @Override
    public void onReady() {
        mShortVideoRecorder.setFocusListener(this);
        runOnUiThread(() -> {
            mSwitchFlashBtn.setVisibility(mShortVideoRecorder.isFlashSupport() ? View.VISIBLE : View.GONE);
            mFlashEnabled = false;
            mSwitchFlashBtn.setActivated(mFlashEnabled);
            mRecordBtn.setEnabled(true);
            refreshSeekBar();
            ToastUtils.showShortToast("可以开始拍摄咯");
        });
    }

    @Override
    public void onError(final int code) {
        ToastUtils.toastErrorCode(code);
    }

    @Override
    public void onDurationTooShort() {
        mSectionProgressBar.removeLastBreakPoint();
        ToastUtils.showShortToast("该视频段太短了");
    }

    @Override
    public void onRecordStarted() {
        Log.i(TAG, "record start time: " + System.currentTimeMillis());
        mSectionBegan = true;
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
        runOnUiThread(() -> updateRecordingBtns(true));
    }

    @Override
    public void onRecordStopped() {
        Log.i(TAG, "record stop time: " + System.currentTimeMillis());
        mSectionBegan = false;
        runOnUiThread(() -> updateRecordingBtns(false));
    }

    @Override
    public void onSectionRecording(long sectionDurationMs, long videoDurationMs, int sectionCount) {
        Log.d(TAG, "sectionDurationMs: " + sectionDurationMs + "; videoDurationMs: " + videoDurationMs + "; sectionCount: " + sectionCount);
        updateRecordingPercentageView(videoDurationMs);
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        mSectionProgressBar.addBreakPointTime(totalDuration);
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
        updateRecordingPercentageView(totalDuration);
        onSectionCountChanged(sectionCount, totalDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        mSectionProgressBar.removeLastBreakPoint();
        updateRecordingPercentageView(totalDuration);
        onSectionCountChanged(sectionCount, totalDuration);
    }

    @Override
    public void onRecordCompleted() {
        ToastUtils.showShortToast("已达到拍摄总时长");
    }

    @Override
    public void onProgressUpdate(final float percentage) {
        runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        runOnUiThread(() -> {
            mProcessingDialog.dismiss();
            ToastUtils.showShortToast("拼接视频段失败: " + errorCode);
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    @Override
    public void onSaveVideoSuccess(final String filePath) {
        Log.i(TAG, "concat sections success filePath: " + filePath);
        MediaStoreUtils.storeVideo(VideoRecordActivity.this, new File(filePath), "video/mp4");
        runOnUiThread(() -> {
            mProcessingDialog.dismiss();
            if (mIsEditVideo) {
                VideoEditActivity.start(VideoRecordActivity.this, filePath);
            } else {
                int screenOrientation = (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == getRequestedOrientation()) ? 0 : 1;
                PlaybackActivity.start(VideoRecordActivity.this, filePath, screenOrientation);
            }
        });
    }

    private void updateRecordingPercentageView(long currentDuration) {
        runOnUiThread(() -> {
            int per = (int) (100 * currentDuration / mRecordSetting.getMaxRecordDuration());
            mRecordingPercentageView.setText((Math.min(per, 100)) + "%");
        });
    }

    private void refreshSeekBar() {
        final int max = mShortVideoRecorder.getMaxExposureCompensation();
        final int min = mShortVideoRecorder.getMinExposureCompensation();
        boolean brightnessAdjustAvailable = (max != 0 || min != 0);
        Log.e(TAG, "max/min exposure compensation: " + max + "/" + min + " brightness adjust available: " + brightnessAdjustAvailable);

        findViewById(R.id.brightness_panel).setVisibility(brightnessAdjustAvailable ? View.VISIBLE : View.GONE);
        mAdjustBrightnessSeekBar.setMax(max + Math.abs(min));
        mAdjustBrightnessSeekBar.setProgress(Math.abs(min));
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
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(() -> {
            mDeleteBtn.setEnabled(count > 0);
            mConcatBtn.setEnabled(totalTime >= ConfigActivity.DEFAULT_MIN_RECORD_DURATION);
        });
    }

    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.if_edit_video));
        builder.setPositiveButton(getString(R.string.dlg_yes), (dialog, which) -> {
            mIsEditVideo = true;
            mShortVideoRecorder.concatSections(VideoRecordActivity.this);
        });
        builder.setNegativeButton(getString(R.string.dlg_no), (dialog, which) -> {
            mIsEditVideo = false;
            mShortVideoRecorder.concatSections(VideoRecordActivity.this);
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    public void onSpeedClicked(View view) {
        if (!mVideoEncodeSetting.IsConstFrameRateEnabled() || !mRecordSetting.IsRecordSpeedVariable()) {
            if (mSectionProgressBar.isRecorded()) {
                ToastUtils.showShortToast("变帧率模式下，无法在拍摄中途修改拍摄倍数！");
                return;
            }
        }

        if (mSectionBegan) {
            ToastUtils.showShortToast("一段视频只能是固定的录制速度！");
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
            default:
                break;
        }

        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        mSectionProgressBar.setProceedingSpeed(mRecordSpeed);
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

    public void onClickWhiteBalance(View view) {
        String[] wbs = mShortVideoRecorder.getSupportedWhiteBalanceMode().toArray(new String[0]);
        new AlertDialog.Builder(this).setItems(wbs, (dialog, which) -> mShortVideoRecorder.setWhiteBalanceMode(wbs[which])).show();
    }
}
