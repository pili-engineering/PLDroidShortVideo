package com.qiniu.shortvideo.app.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.FURenderer;
import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.pili.droid.shortvideo.PLCaptureFrameListener;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.FilterItemAdapter;
import com.qiniu.shortvideo.app.faceunity.BeautyControlView;
import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.MediaUtils;
import com.qiniu.shortvideo.app.utils.RecordSettings;
import com.qiniu.shortvideo.app.utils.Utils;
import com.qiniu.shortvideo.app.utils.ViewOperator;
import com.qiniu.shortvideo.app.view.ListBottomView;
import com.qiniu.shortvideo.app.view.SectionProgressBar;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraPreviewListener;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLFocusListener;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * 视频录制模块，实现了基础的短视频录制、添加音乐等功能，同时加入了相芯美颜
 */
public class VideoRecordActivity extends AppCompatActivity implements
        PLRecordStateListener,
        PLVideoSaveListener,
        PLVideoFilterListener,
        PLCameraPreviewListener,
        PLFocusListener {
    private static final String TAG = "VideoRecordActivity";

    public static final String PREVIEW_SIZE_RATIO = "PreviewSizeRatio";
    public static final String PREVIEW_SIZE_LEVEL = "PreviewSizeLevel";
    public static final String ENCODING_MODE = "EncodingMode";
    public static final String ENCODING_SIZE_LEVEL = "EncodingSizeLevel";
    public static final String ENCODING_BITRATE_LEVEL = "EncodingBitrateLevel";
    public static final String AUDIO_CHANNEL_NUM = "AudioChannelNum";

    private static final int FLING_MIN_DISTANCE = 350;
    private static final int CHOOSE_MUSIC_REQUEST_CODE = 0;

    private static final int RECORD = 0;
    private static final int CAPTURE = 1;
    private static final int FLING_MIN_DISTANCE_SWITCH_MODE = 20;// 移动最小距离

    private View mDecorView;
    private Button mRecordBtn;
    private Button mConcatBtn;
    private TextView mSwitchCameraBtn;
    private TextView mSwitchFlashBtn;
    private ImageButton mDeleteBtn;
    private TextView mSpeedTextView;
    private Group mRecordBtns;
    // 模式切换相关
    private TextView mRecordModeBtn;
    private TextView mCaptureModeBtn;
    /** 模式按键切换动画 */
    private ValueAnimator valueAnimator;
    /** 录制按键模式 */
    private RelativeLayout mRecordModeLayout;
    private int mRecordMode = RECORD;
    private float mPosX, mCurPosX;
    private float mOffset = Float.MIN_VALUE;

    private int mSectionCount;

    private GLSurfaceView mPreview;
    private PLShortVideoRecorder mShortVideoRecorder;
    private SectionProgressBar mSectionProgressBar;

    private ListBottomView mFilterBottomView;
    private FilterItemAdapter mFilterItemAdapter;
    private boolean mIsSelectingFilter;
    private String mCurrentFilter;

    /** 特效描述信息控件 */
    private TextView mEffectDescription;
    private TextView mCurrentDescriptionText;

    private PLCameraSetting mCameraSetting;
    private PLMicrophoneSetting mMicrophoneSetting;
    private PLRecordSetting mRecordSetting;
    private PLVideoEncodeSetting mVideoEncodeSetting;
    private PLAudioEncodeSetting mAudioEncodeSetting;
    private PLFaceBeautySetting mFaceBeautySetting;

    private GestureDetector mGestureDetector;

    private boolean mSectionBegan;
    private boolean mFlashEnabled = false;
    private double mRecordSpeed = 1;
    private long mSectionBeginTSMs;

    private Stack<Long> mDurationRecordStack = new Stack();
    private Stack<Double> mDurationVideoStack = new Stack();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        mDecorView = getWindow().getDecorView();
        mPreview = findViewById(R.id.preview);
        mSectionProgressBar = findViewById(R.id.section_progress_bar);
        mRecordBtn = findViewById(R.id.record_btn);
        mConcatBtn = findViewById(R.id.next_btn);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_btn);
        mSwitchFlashBtn = findViewById(R.id.flash_light_btn);
        mDeleteBtn = findViewById(R.id.delete_section_btn);
        mSpeedTextView = findViewById(R.id.normal_speed_btn);
        mFaceUnityControlView = findViewById(R.id.face_unity_panel);
        TextView speedText = findViewById(R.id.normal_speed_btn);
        speedText.setTextColor(getResources().getColor(R.color.colorAccent));
        mRecordBtns = findViewById(R.id.record_btn_group);
        mEffectDescription = findViewById(R.id.effect_description);
        mRecordModeLayout = findViewById(R.id.record_mode_layout);
        mRecordModeBtn = findViewById(R.id.record_mode_video);
        mCaptureModeBtn = findViewById(R.id.record_mode_picture);

        mRecordModeBtn.setOnTouchListener(onModeBarTouchListener);
        mCaptureModeBtn.setOnTouchListener(onModeBarTouchListener);

        mShortVideoRecorder = new PLShortVideoRecorder();

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
        mVideoEncodeSetting.setConstFrameRateEnabled(true);

        mAudioEncodeSetting = new PLAudioEncodeSetting();
        mAudioEncodeSetting.setHWCodecEnabled(encodingModePos == 0);
        mAudioEncodeSetting.setChannels(RecordSettings.AUDIO_CHANNEL_NUM_ARRAY[audioChannelNumPos]);

        mRecordSetting = new PLRecordSetting();
        mRecordSetting.setMaxRecordDuration(RecordSettings.DEFAULT_MAX_RECORD_DURATION);
        mRecordSetting.setRecordSpeedVariable(true);
        mRecordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        mRecordSetting.setVideoFilepath(Config.RECORD_FILE_PATH);

        mFaceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);

        mShortVideoRecorder.prepare(mPreview, mCameraSetting, mMicrophoneSetting, mVideoEncodeSetting,
                mAudioEncodeSetting, null, mRecordSetting);
        mShortVideoRecorder.setRecordStateListener(this);
        mShortVideoRecorder.setFocusListener(this);
        mShortVideoRecorder.setVideoFilterListener(this);
        mShortVideoRecorder.setCameraPreviewListener(this);

        mSectionProgressBar.setFirstPointTime(RecordSettings.DEFAULT_MIN_RECORD_DURATION);

        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        mSectionProgressBar.setProceedingSpeed(mRecordSpeed);
        mSectionProgressBar.setTotalTime(this, mRecordSetting.getMaxRecordDuration());

        initBuiltInFilters();

        // init faceUnity engine
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mInputProp = getCameraOrientation(mCameraId);
        mFURenderer = new FURenderer
                .Builder(this)
                .inputPropOrientation(mInputProp)
                .build();

        mFaceUnityEffectDescription = findViewById(R.id.face_unity_effect_description);
        mFaceUnityControlView = findViewById(R.id.face_unity_panel);
        mFaceUnityControlView.setOnFUControlListener(mFURenderer);
        mFaceUnityControlView.setOnDescriptionShowListener(new BeautyControlView.OnDescriptionShowListener() {
            @Override
            public void onDescriptionShowListener(int str) {
                showDescription(str, 1500);
            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mRecordMode == CAPTURE) {
                    mShortVideoRecorder.captureFrame(new PLCaptureFrameListener() {
                        @Override
                        public void onFrameCaptured(PLVideoFrame plVideoFrame) {
                            if (plVideoFrame == null) {
                                Log.e(TAG, "capture frame failed");
                                return;
                            }

                            Log.i(TAG, "captured frame width: " + plVideoFrame.getWidth() + " height: " + plVideoFrame.getHeight() + " timestamp: " + plVideoFrame.getTimestampMs());
                            try {
                                FileOutputStream fos = new FileOutputStream(Config.CAPTURED_FRAME_FILE_PATH);
                                plVideoFrame.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fos);
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
                } else {
                    if (mSectionBegan) {
                        endSectionInternal();
                    } else {
                        beginSectionInternal();
                    }
                }
            }
        });

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
                    mFilterItemAdapter.changeToNextFilter();
                    return true;
                } else if (e1.getX() - e2.getX() < -120) {
                    mFilterItemAdapter.changeToLastFilter();
                    return true;
                }
                return false;
            }
        });

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mFaceUnityControlView.getVisibility() == View.VISIBLE) {
                    ViewOperator.startDisappearAnimY(mFaceUnityControlView);
                    mFaceUnityControlView.setVisibility(View.GONE);
                    mRecordBtns.setVisibility(View.VISIBLE);
                    return true;
                }
                if (mIsSelectingFilter) {
                    ViewOperator.startDisappearAnimY(mFilterBottomView);
                    mFilterBottomView.setVisibility(View.GONE);
                    mRecordBtns.setVisibility(View.VISIBLE);
                    mIsSelectingFilter = false;
                }
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 19 && Utils.checkDeviceHasNavigationBar(this)) {
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            mDecorView.setSystemUiVisibility(flag);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mRecordBtn.setEnabled(false);
        mShortVideoRecorder.resume();
        mShortVideoRecorder.setBuiltinFilter(mCurrentFilter);
        mFaceUnityControlView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mSectionBegan) {
            endSectionInternal();
        }
        mShortVideoRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShortVideoRecorder.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_MUSIC_REQUEST_CODE) {
            AudioFile audioFile = (AudioFile) data.getSerializableExtra(ChooseMusicActivity.SELECTED_MUSIC_FILE);
            long startTime = data.getLongExtra(ChooseMusicActivity.START_TIME, 0);
            if (audioFile != null) {
                mShortVideoRecorder.setMusicFile(audioFile.getFilePath());
                mShortVideoRecorder.setMusicPosition((int) startTime);
            } else {
                mShortVideoRecorder.setMusicFile(null);
            }
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickDeleteLastSection(View v) {
        if (!mShortVideoRecorder.deleteLastSection()) {
            ToastUtils.s(this, "回删视频段失败");
        }
    }

    public void onClickSwitchFlash(View v) {
        if (!mShortVideoRecorder.isFlashSupport()) {
            return;
        }
        mFlashEnabled = !mFlashEnabled;
        mShortVideoRecorder.setFlashEnabled(mFlashEnabled);
        mSwitchFlashBtn.setActivated(mFlashEnabled);
        Drawable drawable= getResources().getDrawable(mFlashEnabled ? R.mipmap.qn_flash_on : R.mipmap.qn_flash_off);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSwitchFlashBtn.setCompoundDrawables(null, drawable, null, null);
    }

    public void onClickSwitchCamera(View v) {
        mShortVideoRecorder.switchCamera();
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            Drawable drawable= getResources().getDrawable(R.mipmap.qn_flash_off);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mSwitchFlashBtn.setCompoundDrawables(null, drawable, null, null);
        }
        mInputProp = getCameraOrientation(mCameraId);
        Log.d("mInputProp", mInputProp + "");
        mFURenderer.onCameraChange(getCameraType(mCameraId), getCameraOrientation(mCameraId), mInputProp);
    }

    public void onClickChooseMusic(View v) {
        if (mSectionCount > 0) {
            ToastUtils.s(this, getString(R.string.can_not_add_music_tips));
            return;
        }
        Intent intent = new Intent(VideoRecordActivity.this, ChooseMusicActivity.class);
        intent.putExtra("videoDurationMs", mRecordSetting.getMaxRecordDuration());
        startActivityForResult(intent, CHOOSE_MUSIC_REQUEST_CODE);
    }

    public void onClickConcatSections(View v) {
        if (mSectionBegan) {
            ToastUtils.s(VideoRecordActivity.this, "当前正在拍摄，无法拼接！");
            return;
        }
        mShortVideoRecorder.concatSections(VideoRecordActivity.this);
    }

    public void onClickStickers(View v) {
        if (mFaceUnityControlView.getVisibility() == View.GONE) {
            mFaceUnityControlView.setVisibility(View.VISIBLE);
            mRecordBtns.setVisibility(View.INVISIBLE);
            ViewOperator.startAppearAnimY(mFaceUnityControlView);
        } else {
            ViewOperator.startDisappearAnimY(mFaceUnityControlView);
            mFaceUnityControlView.setVisibility(View.GONE);
            mRecordBtns.setVisibility(View.VISIBLE);
        }
    }

    public void onClickFilterSelect(View v) {
        mRecordBtns.setVisibility(View.INVISIBLE);
        mFilterBottomView.setVisibility(View.VISIBLE);
        ViewOperator.startAppearAnimY(mFilterBottomView);
        mIsSelectingFilter = true;
    }

    public void onSpeedClicked(View view) {
        if (mRecordMode == CAPTURE) {
            ToastUtils.s(this, "拍照模式下无法修改拍摄倍数！");
            return;
        }
        if (!mVideoEncodeSetting.IsConstFrameRateEnabled() || !mRecordSetting.IsRecordSpeedVariable()) {
            if (mSectionProgressBar.isRecorded()) {
                ToastUtils.s(this, "变帧率模式下，无法在拍摄中途修改拍摄倍数！");
                return;
            }
        }

        if (mSpeedTextView != null) {
            mSpeedTextView.setTextColor(Color.WHITE);
        }

        TextView textView = (TextView) view;
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        mSpeedTextView = textView;

        switch (view.getId()) {
            case R.id.super_slow_speed_btn:
                mRecordSpeed = 0.25;
                break;
            case R.id.slow_speed_btn:
                mRecordSpeed = 0.5;
                break;
            case R.id.normal_speed_btn:
                mRecordSpeed = 1;
                break;
            case R.id.fast_speed_btn:
                mRecordSpeed = 2;
                break;
            case R.id.super_fast_speed_btn:
                mRecordSpeed = 4;
                break;
        }

        mShortVideoRecorder.setRecordSpeed(mRecordSpeed);
        if (mRecordSetting.IsRecordSpeedVariable() && mVideoEncodeSetting.IsConstFrameRateEnabled()) {
            mSectionProgressBar.setProceedingSpeed(mRecordSpeed);
            mRecordSetting.setMaxRecordDuration(RecordSettings.DEFAULT_MAX_RECORD_DURATION);
            mSectionProgressBar.setFirstPointTime(RecordSettings.DEFAULT_MIN_RECORD_DURATION);
        } else {
            mRecordSetting.setMaxRecordDuration((long) (RecordSettings.DEFAULT_MAX_RECORD_DURATION * mRecordSpeed));
            mSectionProgressBar.setFirstPointTime((long) (RecordSettings.DEFAULT_MIN_RECORD_DURATION * mRecordSpeed));
        }

        mSectionProgressBar.setTotalTime(this, mRecordSetting.getMaxRecordDuration());
    }

    private void initBuiltInFilters() {
        mFilterBottomView = findViewById(R.id.filter_select_view);
        mFilterItemAdapter = new FilterItemAdapter(this,
                new ArrayList<PLBuiltinFilter>(Arrays.asList(mShortVideoRecorder.getBuiltinFilterList())),
                new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.built_in_filters))));
        mFilterItemAdapter.setOnFilterSelectListener(new FilterItemAdapter.OnFilterSelectListener() {
            @Override
            public void onFilterSelected(String filterName, String description) {
                mCurrentFilter = filterName;
                mShortVideoRecorder.setBuiltinFilter(filterName);
                showDescription(description, 1500, false);
            }
        });
        mFilterBottomView.init(mFilterItemAdapter);
    }

    private PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        mSectionCount = count;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeleteBtn.setEnabled(count > 0);
                mDeleteBtn.setImageResource(count > 0 ? R.mipmap.qn_delete_section_active : R.mipmap.qn_delete_section_inactive);
                mConcatBtn.setEnabled(totalTime >= (RecordSettings.DEFAULT_MIN_RECORD_DURATION));
                mRecordModeLayout.setVisibility(count > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private void updateRecordingBtns(boolean isRecording) {
        mSwitchCameraBtn.setEnabled(!isRecording);
        mRecordBtn.setBackgroundResource(isRecording ? R.mipmap.qn_shooting : R.mipmap.qn_video);
    }

    private void beginSectionInternal() {
        if (mShortVideoRecorder.beginSection()) {
            mSectionBegan = true;
            mSectionBeginTSMs = System.currentTimeMillis();
            mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
            updateRecordingBtns(true);
        } else {
            ToastUtils.s(VideoRecordActivity.this, "无法开始视频段录制");
        }
    }

    private void endSectionInternal() {
        long sectionRecordDurationMs = System.currentTimeMillis() - mSectionBeginTSMs;
        long totalRecordDurationMs = sectionRecordDurationMs + (mDurationRecordStack.isEmpty() ? 0 : mDurationRecordStack.peek().longValue());
        double sectionVideoDurationMs = sectionRecordDurationMs / mRecordSpeed;
        double totalVideoDurationMs = sectionVideoDurationMs + (mDurationVideoStack.isEmpty() ? 0 : mDurationVideoStack.peek().doubleValue());
        mDurationRecordStack.push(new Long(totalRecordDurationMs));
        mDurationVideoStack.push(new Double(totalVideoDurationMs));
        if (mRecordSetting.IsRecordSpeedVariable()) {
            mSectionProgressBar.addBreakPointTime((long) totalVideoDurationMs);
        } else {
            mSectionProgressBar.addBreakPointTime(totalRecordDurationMs);
        }

        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
        mShortVideoRecorder.endSection();
        mSectionBegan = false;
    }

    private View.OnTouchListener onModeBarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPosX = event.getX();
                    mCurPosX = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mCurPosX = event.getX();
                    // 滑动效果处理
                    if (mCurPosX - mPosX > 0 && (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE_SWITCH_MODE)) {
                        //向左滑动
                        if(mRecordMode == CAPTURE) {
                            switchCameraModeButton(RECORD);
                        }
                        return false;
                    } else if (mCurPosX - mPosX < 0 && (Math.abs(mCurPosX - mPosX) > FLING_MIN_DISTANCE_SWITCH_MODE)) {
                        //向右滑动
                        if(mRecordMode == RECORD)
                        {
                            switchCameraModeButton(CAPTURE);
                        }
                        return false;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    // 点击效果处理
                    if(Math.abs(mCurPosX - mPosX) < FLING_MIN_DISTANCE_SWITCH_MODE || mCurPosX == 0){
                        switch (v.getId()){
                            // 拍照模式
                            case R.id.record_mode_picture:
                                switchCameraModeButton(CAPTURE);
                                break;
                            // 点击录制模式
                            case R.id.record_mode_video:
                                switchCameraModeButton(RECORD);
                                break;
                        }
                        return false;
                    }
            }
            return false;
        }
    };

    /**
     * 切换摄像模式按键
     * @param index
     */
    private void switchCameraModeButton(int index)
    {
        if(valueAnimator != null && valueAnimator.isRunning() || mRecordMode == index) return;

        // 设置文字颜色
        mRecordModeBtn.setTextColor(index == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.alpha_white_66));
        mCaptureModeBtn.setTextColor(index == 1 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.alpha_white_66));

        final float[] Xs = getModeButtonWidth();
        float offSet = 0;
        if (mOffset == Float.MIN_VALUE) {
            mOffset = mCaptureModeBtn.getX() + (float) mCaptureModeBtn.getMeasuredWidth() / 2
                    - (mRecordModeBtn.getX() + (float) mRecordModeBtn.getMeasuredWidth() / 2);
        }
        if(mRecordMode == 0 && index == 1) {
            offSet = -mOffset;
        } else if(mRecordMode == 1 && index == 0) {
            offSet = mOffset;
        }

        // 切换动画
        valueAnimator = ValueAnimator.ofFloat(0, offSet);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float offSet = (float) animation.getAnimatedValue();
                mRecordModeBtn.setX(Xs[0] + offSet);
                mCaptureModeBtn.setX(Xs[1] + offSet);
            }
        });
        valueAnimator.start();

        // 录制按键背景
        mRecordBtn.setBackground(getResources().getDrawable(index == CAPTURE ? R.mipmap.qn_photo : R.mipmap.qn_video));
        mRecordMode = index;
    }

    /**
     * 获取底部拍摄模式按键宽度
     */
    private float[] getModeButtonWidth()
    {
        float[] xs = new float[2];
        xs[0] = mRecordModeBtn.getX();
        xs[1] = mCaptureModeBtn.getX();
        return xs;
    }

    @Override
    public void onManualFocusStart(boolean b) {

    }

    @Override
    public void onManualFocusStop(boolean b) {

    }

    @Override
    public void onManualFocusCancel() {

    }

    @Override
    public void onAutoFocusStart() {

    }

    @Override
    public void onAutoFocusStop() {

    }

    @Override
    public void onReady() {
        Log.i(TAG, "onReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordBtn.setEnabled(true);
                ToastUtils.s(VideoRecordActivity.this, "可以开始拍摄咯");
            }
        });
    }

    @Override
    public void onError(int i) {
        Log.i(TAG, "onError : " + i);
    }

    @Override
    public void onDurationTooShort() {

    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onSectionRecording(long sectionDurationMs, long videoDurationMs, int sectionCount) {

    }

    @Override
    public void onRecordStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecordingBtns(false);
            }
        });
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        double videoSectionDuration = mDurationVideoStack.isEmpty() ? 0 : mDurationVideoStack.peek().doubleValue();
        if ((videoSectionDuration + incDuration / mRecordSpeed) >= mRecordSetting.getMaxRecordDuration()) {
            videoSectionDuration = mRecordSetting.getMaxRecordDuration();
        }
        Log.d(TAG, "videoSectionDuration: " + videoSectionDuration + "; incDuration: " + incDuration);
        onSectionCountChanged(sectionCount, (long) videoSectionDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        mSectionProgressBar.removeLastBreakPoint();
        if (!mDurationVideoStack.isEmpty()) {
            mDurationVideoStack.pop();
        }
        if (!mDurationRecordStack.isEmpty()) {
            mDurationRecordStack.pop();
        }
        double currentDuration = mDurationVideoStack.isEmpty() ? 0 : mDurationVideoStack.peek().doubleValue();
        onSectionCountChanged(sectionCount, (long) currentDuration);
    }

    @Override
    public void onRecordCompleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionBegan = false;
                mSectionProgressBar.addBreakPointTime(mRecordSetting.getMaxRecordDuration());
                mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
                ToastUtils.s(VideoRecordActivity.this, "已达到拍摄总时长");
            }
        });
    }

    @Override
    public void onSaveVideoSuccess(String s) {
        MediaUtils.storeVideo(this, new File(s), Config.MIME_TYPE_VIDEO);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VideoEditActivity.start(VideoRecordActivity.this, Config.RECORD_FILE_PATH);
            }
        });
    }

    @Override
    public void onSaveVideoFailed(int i) {

    }

    @Override
    public void onSaveVideoCanceled() {

    }

    @Override
    public void onProgressUpdate(float v) {

    }

    @Override
    public boolean onPreviewFrame(byte[] data, int width, int height, int rotation, int fmt, long tsInNanoTime) {
        mCameraData = data;
        return false;
    }

    @Override
    public void onSurfaceCreated() {
        mFURenderer.loadItems();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onSurfaceDestroy() {
        mFURenderer.destroyItems();
        mCameraData = null;
    }

    @Override
    public int onDrawFrame(int texId, int texWidth, int texHeight, long timeStampNs, float[] transformMatrix) {
        if (mCameraData != null) {
            return mFURenderer.onDrawFrameByFBO(mCameraData, texId, texWidth, texHeight);
        }
        return texId;
    }


    //----- FaceUnity SDK  相关 -----//

    private FURenderer mFURenderer;
    private int mCameraId;
    private int mInputProp;
    private BeautyControlView mFaceUnityControlView;

    private TextView mFaceUnityEffectDescription;

    // 原始的相机数据
    private byte[] mCameraData;

    public int getCameraOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Log.d("orientation", info.orientation + "");
        return info.orientation;
    }

    public int getCameraType(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Log.d("facing", info.facing + "");
        return info.facing;
    }

    private Runnable effectDescriptionHide = new Runnable() {
        @Override
        public void run() {
            mCurrentDescriptionText.setText("");
            mCurrentDescriptionText.setVisibility(View.INVISIBLE);
        }
    };

    private void showDescription(int str, int time) {
        if (str == 0) {
            return;
        }
        String filterName = getString(str);
        showDescription(filterName, time, true);
    }

    private void showDescription(String description, int time, boolean isFaceUnity) {
        SpannableString descriptionText;
        if (isFaceUnity) {
            descriptionText = new SpannableString(description);
        } else {
            descriptionText = new SpannableString(String.format("%s%n<<左右滑动切换滤镜>>", description));
            descriptionText.setSpan(new AbsoluteSizeSpan(30, true), 0, description.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            descriptionText.setSpan(new AbsoluteSizeSpan(14, true), description.length(), descriptionText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        mCurrentDescriptionText = isFaceUnity ? mFaceUnityEffectDescription : mEffectDescription;
        mCurrentDescriptionText.removeCallbacks(effectDescriptionHide);
        mCurrentDescriptionText.setVisibility(View.VISIBLE);
        mCurrentDescriptionText.setText(descriptionText);
        mCurrentDescriptionText.postDelayed(effectDescriptionHide, time);
    }
}
